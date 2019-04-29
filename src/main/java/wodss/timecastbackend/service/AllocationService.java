package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.*;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.security.EmployeeSession;
import wodss.timecastbackend.util.*;

import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Component
public class AllocationService {
    //TODO: Check if allocation lies outside of project dates.
    public static final String ERR_MSG_ALLOCATIONNOTFOUND = "Allocation not found.";
    public static final String ERR_MSG_PROJECTNOTFOUND = "Project not found";
    public static final String ERR_MSG_CONTRACTNOTFOUND = "Contract not found";
    public static final String ERR_MSG_PENSUMNEGATIVE = "Pensum percentage is negative.";
    public static final String ERR_MSG_DATESCROSSED = "Start date lies after end date.";
    public static final String ERR_MSG_PROJECT_FTE_EXCEEDED = "Project FTE has been exceeded.";
    public static final String ERR_MSG_STARTDATEOUTSIDEOFCONTRACT = "Start date of allocation lies outside of contract.";
    public static final String ERR_MSG_ENDDATEOUTSIDEOFCONTRACT = "End date of allocation lies outside of contract.";
    public static final String ERR_MSG_STARTDATEOUTSIDEOFPROJECT = "Start date of allocation lies outside of contract.";
    public static final String ERR_MSG_ENDDATEOUTSIDEOFPROJECT = "End date of allocation lies outside of contract.";
    public static final String ERR_MSG_CONTRACTLIMITEXCEEDED = "Pensum percentage of allocation exceeds contract limit";


    private AllocationRepository allocationRepository;
    private ContractRepository contractRepository;
    private ProjectRepository projectRepository;
    private ModelMapper mapper;
    private EmployeeSession employeeSession;

    @Autowired
    public AllocationService(AllocationRepository allocationRepository, ContractRepository contractRepository,
                             ProjectRepository projectRepository, ModelMapper mapper, EmployeeSession employeeSession) {
        this.allocationRepository = allocationRepository;
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.mapper = mapper;
        this.employeeSession = employeeSession;
    }

    /**
     * Find allocations with optional query parameters. The qeury options do stack (AND association).
     * ADMIN, PROJECTMANAGER: Access to all existing allocations. Gets informed with 404 if an id in query employeeId
     *                        or projectId do not exist.
     * DEVELOPER: Access only to own allocations of active project. Can only set query parameters employeeId to own id
     *            and projectID to projects in which the Developer is involved. Gets 403 otherwise.
     * @param employeeId Filter by employee associated to
     * @param projectId Filter by project associated to
     * @param fromDate Get all associations starting from date
     *                 (Allocation with start date before and end date after are included)
     * @param toDate Get all assocations until date
     *               (Allocations with start date before and end date after are included)
     * @return List of allocation DTOs
     * @throws PreconditionFailedException Query parameter fromDate is after toDate
     * @throws ResourceNotFoundException Query parameters employeeId or projectId do not exist
     * @throws ForbiddenException Developer tried to access foreign allocations and not involved projects
     */
    public List<AllocationDTO> findbyQuery(Long employeeId, Long projectId, LocalDate fromDate, LocalDate toDate)
            throws PreconditionFailedException, ResourceNotFoundException, ForbiddenException {
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new PreconditionFailedException();
        Employee currentEmployee = employeeSession.getEmployee();
        List<Allocation> allocations = null;

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER) {
            if((projectId != null && !projectRepository.existsById(projectId))){
                throw new ResourceNotFoundException("Employee or project not found");
            }
            allocations = allocationRepository.findByQuery(employeeId, projectId, fromDate, toDate);
        }

        //DEVELOPER
        else {
            //Checks if the employee tries to access other employees or projects via query parameters
            List<Long> involvedProjectIds = getInvolvedProjects(currentEmployee);
            if ((employeeId != null && employeeId != currentEmployee.getId()) ||
                    (projectId != null && !involvedProjectIds.contains(projectId))) {
                throw new ForbiddenException(
                        "Missing permission to get the allocation (DEVELOPER: Other uninvolved employee or project)");
            }

            //Filter active projects only
            allocations = allocationRepository
                    .findByQuery(currentEmployee.getId(), projectId, fromDate, toDate)
                    .stream()
                    .filter(a -> isActiveProject(a.getProject()))
                    .collect(Collectors.toList());
        }

        return modelsToDTOs(allocations);
    }

    /**
     * Find an allocation by id.
     * ADMINISTRATOR, PROJECTMANAGER: Access to all allocations. Get informed if allocation does not exists.
     * DEVELOPER: Access to only own allocations. Gets 403 if accesses to any other allocation (even if it does not
     *            exist).
     * @param id Identifier of the requested allocation
     * @return Allocation DTO of requested allocation
     * @throws ForbiddenException Developer tries to access foreign allocations
     * @throws ResourceNotFoundException Administrator or projectmanager access to non existing allocation
     */
    public AllocationDTO findById(long id) throws ForbiddenException, ResourceNotFoundException {
        Employee currentEmployee = employeeSession.getEmployee();
        Allocation allocation = null;

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER)
            allocation = checkIfAllocationExists(id);

        //DEVELOPER
        else {
            try {
                allocation = checkIfAllocationExists(id);
                if(currentEmployee.getId() != allocation.getContract().getEmployee().getId() ||
                        !isActiveProject(allocation.getProject()))
                    throw new Exception();
            } catch (Exception e) {
                throw new ForbiddenException(
                        "Missing permission to get the allocation (DEVELOPER: Other uninvolved employee or project)");
            }
        }

        return  mapper.allocationToAllocationDTO(allocation);
    }

    /**
     * Create a new allocation.
     * ADMIN: Able to create allocations for any projects.
     * PROJECTMANAGER: Able to create allocations for projects in which assigned as project manager.
     * DEVELOPER: Is not able to create allocations at all.
     * @param allocationDTO Received and validated allocation DTO object
     * @return Allocation DTO of the newly created allocation
     * @throws ForbiddenException Developer tried to create an allocation or projectmanager tried to create allocations
     *                            for foreign projects
     * @throws ResourceNotFoundException Project or contract referred to do not exist
     * @throws PreconditionFailedException Project FTE or contract pensumPercentage exceeded, allocation date range is
     *                                     not within project date range.
     */
    public AllocationDTO createAllocation(AllocationDTO allocationDTO)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER) {
            Project project = checkIfProjectExists(allocationDTO.getProjectId());
            Contract contract = checkIfContractExists(allocationDTO.getContractId());

            //PROJECTMANAGER
            if(currentEmployee.getRole() == Role.PROJECTMANAGER &&
                    project.getProjectManager().getId() != currentEmployee.getId()) {
                throw new ForbiddenException(
                        "Missing permission to create an allocation " +
                                "(PROJECTMANAGER: Somebody's else's project, DEVELOPER: All)");
            }

            checkIfAllocationIsValid(allocationDTO, project, contract);
            Allocation allocation = new Allocation(project, contract, allocationDTO.getPensumPercentage(), allocationDTO.getStartDate(), allocationDTO.getEndDate());
            allocation = allocationRepository.save(allocation);
            allocationDTO = mapper.allocationToAllocationDTO(allocation);
            return allocationDTO;
        }

        //DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to create an allocation " +
                            "(PROJECTMANAGER: Somebody's else's project, DEVELOPER: All)");
        }
    }

    /**
     * Delete an allocation.
     * ADMIN: Able to delete allocations for any projects.
     * PROJECTMANAGER: Able to delete allocations for projects in which assigned as project manager.
     * DEVELOPER: Not able to delete allocations at all.
     * @param id Identifier of the requested allocation to delete
     * @throws ForbiddenException Developer tried to delete an allocation or projectmanager tried to delete allocations
*    *                            for foreign projects
     * @throws ResourceNotFoundException Allocation with id does not exist
     */
    public void deleteAllocation(long id) throws ForbiddenException, ResourceNotFoundException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRAOTR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER) {
            Allocation allocation = checkIfAllocationExists(id);

            //PROJECTMANAGER
            if(currentEmployee.getRole() == Role.PROJECTMANAGER &&
                    allocation.getProject().getProjectManager().getId() != currentEmployee.getId()) {
                throw new ForbiddenException(
                        "Missing permission to delete the allocation " +
                                "(PROJECTMANAGER: Somebody's else's project, DEVELOPER: ALL)");
            }
            allocationRepository.delete(allocation);
        }

        //DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to delete the allocation " +
                    "(PROJECTMANAGER: Somebody's else's project, DEVELOPER: ALL)");
        }
    }

    /**
     * Update an allocation.
     * ADMINISTRATOR: Able to update all allocations.
     * PROJECTMANAGER: Able to update allocations for projects in which assigned as project manager.
     * DEVELOPER: Not able to update allocations at all.
     * @param id Identifier of the requested allocation
     * @param allocationDTO Validated allocation DTO from request with updated fields
     * @return Allocation DTO of the updated allocation
     * @throws ForbiddenException Developer tried to update an allocation or projectmanager tried to update allocations
*    *                            for foreign projects
     * @throws ResourceNotFoundException Allocation with id does not exist, project or contract do not exist
     * @throws PreconditionFailedException Project FTE or contract pensumPercentage exceeded, allocation date range is
*    *                                     not within project date range.
     */
    public AllocationDTO updateAllocation(long id, AllocationDTO allocationDTO)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER) {
            Allocation allocation = checkIfAllocationExists(id);
            Project project = checkIfProjectExists(allocationDTO.getProjectId());

            //PROJECTMANAGER
            if(currentEmployee.getRole() == Role.PROJECTMANAGER &&
                    project.getProjectManager().getId() != currentEmployee.getId()) {
                throw new ForbiddenException(
                        "Missing permission to update the allocation " +
                                "(PROJECTMANAGER: Somebody's else's project, DEVELOPER: ALL");
            }

            Contract contract = checkIfContractExists(allocationDTO.getContractId());
            checkIfAllocationIsValid(allocationDTO, project, contract);
            allocation.setContract(contract);
            allocation.setProject(project);
            allocation.setPensumPercentage(allocationDTO.getPensumPercentage());
            allocation.setStartDate(allocationDTO.getStartDate());
            allocation.setEndDate(allocationDTO.getEndDate());
            allocationRepository.save(allocation);
            allocationDTO.setId(id);
            return allocationDTO;
        }

        //DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to update the allocation " +
                            "(PROJECTMANAGER: Somebody's else's project, DEVELOPER: ALL");
        }
    }

    /**
     * Checks if the repository contains an allocation with the given id.
     * @param id the id of the allocation
     * @return The allocation object of the given id if it was found in the repository
     * @throws Exception Throws a RessourceNotFoundException if the allocation was not found.
     */

    private Allocation checkIfAllocationExists(long id) throws ResourceNotFoundException {

        Optional<Allocation> oAllocation = allocationRepository.findById(id);
        if(oAllocation.isPresent())
            return oAllocation.get();
        else
            throw new ResourceNotFoundException(ERR_MSG_ALLOCATIONNOTFOUND);
    }


    /**
     * Checks if the repository contains a contract with the given id.
     * @param id the id of the contract
     * @return The contract object of the given id if it was found in the repository
     * @throws Exception Throws a RessourceNotFoundException if the contract was not found.
     */

    private Contract checkIfContractExists(long id) throws ResourceNotFoundException {

        Optional<Contract> oContract = contractRepository.findById(id);
        if(oContract.isPresent())
            return oContract.get();
        else
            throw new ResourceNotFoundException(ERR_MSG_CONTRACTNOTFOUND);
    }

    /**
     * Checks if the repository contains a project with the given id.
     * @param id the id of the project
     * @return The project object of the given id if it was found in the repository
     * @throws Exception Throws a RessourceNotFoundException if the project was not found.
     */

    private Project checkIfProjectExists(long id) throws ResourceNotFoundException {

        Optional<Project> oProject = projectRepository.findById(id);
        if(oProject.isPresent())
            return oProject.get();
        else
            throw new ResourceNotFoundException(ERR_MSG_PROJECTNOTFOUND);
    }


    /**
     * Performs several checks to see if the allocation can be created.
     * 1: Checks if the allocation pensum is negative
     * 2: Checks the dates of the
     * @param allocationDTO
     * @param project
     * @param contract
     * @throws Exception
     */
    private void checkIfAllocationIsValid(AllocationDTO allocationDTO, Project project, Contract contract) throws PreconditionFailedException{
        //Do all checks in one method.
        checkIfAllocationPensumPercentageIsNegative(allocationDTO.getPensumPercentage());
        checkDates(allocationDTO.getStartDate(), allocationDTO.getEndDate());
        checkIfAllocationExceedsFTEOfProject(project, allocationDTO.getPensumPercentage(), allocationDTO.getId());
        checkIfAllocationFitsInContract(allocationDTO.getStartDate() , allocationDTO.getEndDate(), contract);
        checkIfAllocationFitsInProject(allocationDTO.getStartDate(), allocationDTO.getEndDate(), project);
        checkIfAllocationExceedsContractLimit(contract, allocationDTO.getStartDate(), allocationDTO.getEndDate(), allocationDTO.getPensumPercentage(), allocationDTO.getId());
    }

    private void checkIfAllocationPensumPercentageIsNegative(int allocationPensumPercentage)
    		throws PreconditionFailedException {
        if(allocationPensumPercentage < 0) throw new PreconditionFailedException(ERR_MSG_PENSUMNEGATIVE);
    }

    private void checkDates(LocalDate startDate, LocalDate endDate) throws PreconditionFailedException{
        if(startDate.isAfter(endDate)){
            throw new PreconditionFailedException(ERR_MSG_DATESCROSSED);
        }
    }

    private void checkIfAllocationExceedsFTEOfProject(Project project, int allocationPensumPercentage, long allocationID)
            throws PreconditionFailedException{
        int FTEs = allocationRepository.findAll().stream()
                .filter(allocation -> (allocation.getProject().getId() == project.getId()) && (allocation.getId() != allocationID))
                .mapToInt(allocation -> allocation.getPensumPercentage())
                .sum();
        if(FTEs + allocationPensumPercentage > project.getFtePercentage()){
            throw new PreconditionFailedException(ERR_MSG_PROJECT_FTE_EXCEEDED);
       }
    }

    private void checkIfAllocationFitsInContract(LocalDate startDate, LocalDate endDate, Contract contract)
            throws PreconditionFailedException{
        boolean startDateLiesOutsideOfContract = startDate.isBefore(contract.getStartDate());
        boolean endDateLiesOutsideOfContract = endDate.isAfter(contract.getEndDate());
        if(startDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_STARTDATEOUTSIDEOFCONTRACT);
        if(endDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_ENDDATEOUTSIDEOFCONTRACT);
    }

    private void checkIfAllocationFitsInProject(LocalDate startDate, LocalDate endDate, Project project) throws PreconditionFailedException{
        boolean startDateLiesOutsideOfContract = startDate.isBefore(project.getStartDate());
        boolean endDateLiesOutsideOfContract = endDate.isAfter(project.getEndDate());
        if(startDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_STARTDATEOUTSIDEOFPROJECT);
        if(endDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_ENDDATEOUTSIDEOFPROJECT);
    }


     public List<AllocationDTO> modelsToDTOs(List<Allocation> allocations) {
        return allocations.stream().map(a -> mapper.allocationToAllocationDTO(a)).collect(Collectors.toList());
    }

    /**
     *
     * @param contract
     * @param startDate
     * @param endDate
     * @param allocationPensumPercentage
     * @throws Exception
     */

    private void checkIfAllocationExceedsContractLimit(Contract contract, LocalDate startDate, LocalDate endDate, int allocationPensumPercentage, long allocationID) throws PreconditionFailedException {


        //TODO: Define an SQL statement in Repository interface

        List<Allocation> overlappingAllocations = allocationRepository.findAll().stream().filter(a ->
                a.getId() != allocationID
                && a.getContract().getId() == contract.getId()
                && (
                        dateIsWithinRange(a, startDate)
                        || dateIsWithinRange(a, endDate)
                        || allocationContainedWithinDates(a, startDate, endDate)
                )
                ).collect(Collectors.toList());

        for(Allocation a : overlappingAllocations){
            if((getTotalPensumAtDate(a.getStartDate(), overlappingAllocations) + allocationPensumPercentage > contract.getPensumPercentage())
            || getTotalPensumAtDate(a.getEndDate(), overlappingAllocations) + allocationPensumPercentage > contract.getPensumPercentage()){
                throw new PreconditionFailedException(ERR_MSG_CONTRACTLIMITEXCEEDED);
            }
        }
    }


    private int getTotalPensumAtDate(LocalDate date, List<Allocation> overlappingAllocations){
        return overlappingAllocations.stream().filter(a ->
                (a.getStartDate().isBefore(date) || a.getStartDate().equals(date))
                && (a.getEndDate().isAfter(date) || a.getEndDate().equals(date))
                ).mapToInt(a -> a.getPensumPercentage()).sum();
        //TODO: Check behavior if sum is used on an empty list.
    }



    /**
     * Checks if the date lies within the date range of the passed allocation or equals either the start or end date.
     * @param a allocation which contains the date range
     * @param localDate the date that needs to be checked
     * @return true if the date lies within the range of the allocation
     */
    private boolean dateIsWithinRange(Allocation a, LocalDate localDate){
        return (a.getStartDate().isBefore(localDate) && a.getEndDate().isAfter(localDate))
            || (a.getStartDate().equals(localDate) || a.getEndDate().equals(localDate));
    }

    /**
     * Checks if the allocation lies in between the start and end date.
     * @param a the allocation
     * @param startDate start date of the new allocation
     * @param endDate end date of the new allocation
     * @return true if the allocation 'a' lies in between the start and end date.
     */
    private boolean allocationContainedWithinDates(Allocation a, LocalDate startDate, LocalDate endDate){
        return (a.getStartDate().isAfter(startDate) && a.getEndDate().isBefore(endDate));
    }

    private Role getRole() {
        String sRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        sRole = sRole.replace("[", "").replace("]", "");
        return Role.valueOf(sRole);
    }

    private List<Long> getInvolvedProjects(Employee employee) {
        List<Allocation> allocations = allocationRepository.findByQuery(
                employee.getId(), null, null, null);
        return allocations.stream()
                .filter(a -> isActiveProject(a.getProject()))
                .map(a -> mapper.allocationToAllocationDTO(a))
                .map(AllocationDTO::getProjectId).collect(Collectors.toList());
    }

    private boolean isActiveProject(Project project) {
        LocalDate now = LocalDate.now();
        return now.isAfter(project.getStartDate()) && now.isBefore(project.getEndDate());
    }
}
