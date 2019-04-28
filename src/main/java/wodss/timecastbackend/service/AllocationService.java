package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Allocation;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.util.*;

import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

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


    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelMapper mapper;

    public List<AllocationDTO> findbyQuery(Long employeeId, Long projectId, LocalDate fromDate, LocalDate toDate)
            throws Exception {
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new BadRequestException();
        return modelsToDTOs(allocationRepository.findByQuery(employeeId, projectId, fromDate, toDate));
    }

    public AllocationDTO findById(long id) throws Exception {
        Allocation allocation = checkIfAllocationExists(id);
        AllocationDTO allocationDTO = mapper.allocationToAllocationDTO(allocation);
        return allocationDTO;
    }

    public AllocationDTO createAllocation(AllocationDTO allocationDTO) throws Exception {
        Contract contract = checkIfContractExists(allocationDTO.getContractId());
        Project project = checkIfProjectExists(allocationDTO.getProjectId());
        checkIfAllocationIsValid(allocationDTO, project, contract);
        Allocation allocation = new Allocation(project, contract, allocationDTO.getPensumPercentage(), allocationDTO.getStartDate(), allocationDTO.getEndDate());
        allocation = allocationRepository.save(allocation);
        allocationDTO = mapper.allocationToAllocationDTO(allocation);
        return allocationDTO;
    }

    public void deleteAllocation(long id) throws Exception {
        Allocation allocation = checkIfAllocationExists(id);
        allocationRepository.delete(allocation);
    }

    public AllocationDTO editAllocation(long id, AllocationDTO allocationDTO) throws Exception {
        Allocation allocation = checkIfAllocationExists(id);
        Contract contract = checkIfContractExists(allocationDTO.getContractId());
        Project project = checkIfProjectExists(allocationDTO.getProjectId());
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

    private Allocation checkIfAllocationExists(long id) throws Exception {
        Optional<Allocation> oAllocation = allocationRepository.findById(id);
        if(oAllocation.isPresent())
            return oAllocation.get();
        else
            throw new RessourceNotFoundException(ERR_MSG_ALLOCATIONNOTFOUND);
    }

    private Contract checkIfContractExists(long id) throws Exception {
        Optional<Contract> oContract = contractRepository.findById(id);
        if(oContract.isPresent())
            return oContract.get();
        else
            throw new RessourceNotFoundException(ERR_MSG_CONTRACTNOTFOUND);
    }

    private Project checkIfProjectExists(long id) throws Exception {
        Optional<Project> oProject = projectRepository.findById(id);
        if(oProject.isPresent())
            return oProject.get();
        else
            throw new RessourceNotFoundException(ERR_MSG_PROJECTNOTFOUND);
    }

    private void checkIfAllocationIsValid(AllocationDTO allocationDTO, Project project, Contract contract) throws Exception{
        //Do all checks in one method.
        checkIfAllocationPensumPercentageIsNegative(allocationDTO.getPensumPercentage());
        checkDates(allocationDTO.getStartDate(), allocationDTO.getEndDate());
        checkIfAllocationExceedsFTEOfProject(project, allocationDTO.getPensumPercentage(), allocationDTO.getId());
        checkIfAllocationFitsInContract(allocationDTO.getStartDate() , allocationDTO.getEndDate(), contract);
        checkIfAllocationFitsInProject(allocationDTO.getStartDate(), allocationDTO.getEndDate(), project);
        checkIfAllocationExceedsContractLimit(contract, allocationDTO.getStartDate(), allocationDTO.getEndDate(), allocationDTO.getPensumPercentage());
    }

    private void checkIfAllocationPensumPercentageIsNegative(int allocationPensumPercentage) throws Exception{
        if(allocationPensumPercentage < 0) throw new PreconditionFailedException(ERR_MSG_PENSUMNEGATIVE);
    }

    private void checkDates(LocalDate startDate, LocalDate endDate) throws Exception{
        if(startDate.isAfter(endDate)){
            throw new PreconditionFailedException(ERR_MSG_DATESCROSSED);
        }
    }


    private void checkIfAllocationExceedsFTEOfProject(Project project, int allocationPensumPercentage, long allocationID) throws Exception{
        int FTEs = allocationRepository.findAll().stream()
                .filter(allocation -> (allocation.getProject().getId() == project.getId()) && (allocation.getId() != allocationID))
                .mapToInt(allocation -> allocation.getPensumPercentage())
                .sum();
        if(FTEs + allocationPensumPercentage > project.getFtePercentage()){
            throw new PreconditionFailedException(ERR_MSG_PROJECT_FTE_EXCEEDED);
        }
    }

    private void checkIfAllocationFitsInContract(LocalDate startDate, LocalDate endDate, Contract contract) throws Exception{
        boolean startDateLiesOutsideOfContract = startDate.isBefore(contract.getStartDate());
        boolean endDateLiesOutsideOfContract = endDate.isAfter(contract.getEndDate());
        if(startDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_STARTDATEOUTSIDEOFCONTRACT);
        if(endDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_ENDDATEOUTSIDEOFCONTRACT);
    }

    private void checkIfAllocationFitsInProject(LocalDate startDate, LocalDate endDate, Project project) throws Exception{
        boolean startDateLiesOutsideOfContract = startDate.isBefore(project.getStartDate());
        boolean endDateLiesOutsideOfContract = endDate.isAfter(project.getEndDate());
        if(startDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_STARTDATEOUTSIDEOFPROJECT);
        if(endDateLiesOutsideOfContract) throw new PreconditionFailedException(ERR_MSG_ENDDATEOUTSIDEOFPROJECT);
    }


     public List<AllocationDTO> modelsToDTOs(List<Allocation> allocations) {
        return allocations.stream().map(a -> mapper.allocationToAllocationDTO(a)).collect(Collectors.toList());
    }

    private void checkIfAllocationExceedsContractLimit(Contract contract, LocalDate startDate, LocalDate endDate, int allocationPensumPercentage) throws Exception {

        //TODO: Define an SQL statement in Repository interface
        List<Allocation> overlappingAllocations = allocationRepository.findAll().stream().filter(a ->
                a.getContract().getId() == contract.getId()
                && (
                        dateIsWithinRange(a, startDate)
                        || dateIsWithinRange(a, endDate)
                        || allocationContainedWithinDates(a, startDate, endDate)
                )
                ).collect(Collectors.toList());

        for(Allocation a : overlappingAllocations){
            int aa = getTotalPensumAtDate(a.getStartDate(), overlappingAllocations);
            int bb = getTotalPensumAtDate(a.getEndDate(), overlappingAllocations);
            if((getTotalPensumAtDate(a.getStartDate(), overlappingAllocations) + allocationPensumPercentage > contract.getPensumPercentage())
            || getTotalPensumAtDate(a.getEndDate(), overlappingAllocations) + allocationPensumPercentage > contract.getPensumPercentage()){
                System.out.println("aa: " + aa);
                System.out.println("bb " + bb);
                throw new PreconditionFailedException(ERR_MSG_CONTRACTLIMITEXCEEDED);
            }
        }
    }


    private int getTotalPensumAtDate(LocalDate date, List<Allocation> overlappingAllocations){
        //TODO: remove aa after debug
        int aa = overlappingAllocations.stream().filter(a ->
                (a.getStartDate().isBefore(date) || a.getStartDate().equals(date))
                        && (a.getEndDate().isAfter(date) || a.getEndDate().equals(date))
        ).mapToInt(a -> a.getPensumPercentage()).sum();
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

}
