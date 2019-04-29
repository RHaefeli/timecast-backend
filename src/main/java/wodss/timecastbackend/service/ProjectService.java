package wodss.timecastbackend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wodss.timecastbackend.domain.Allocation;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.exception.ForbiddenException;
import wodss.timecastbackend.exception.PreconditionFailedException;
import wodss.timecastbackend.exception.ResourceNotFoundException;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.security.EmployeeSession;
import wodss.timecastbackend.util.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final ModelMapper mapper;
    private final EmployeeSession employeeSession;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, EmployeeRepository employeeRepository,
                          AllocationRepository allocationRepository, ModelMapper mapper,
                          EmployeeSession employeeSession){
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.allocationRepository = allocationRepository;
        this.mapper = mapper;
        this.employeeSession = employeeSession;
    }

    /**
     * Find projects with optional query parameters. The qeury options do stack (AND association).
     * ADMINISTRATOR, PROJECTMANAGER: Access all projects. Gets informed with 404 if id in query
     *      *                         projectManagerId does not exist.
     * DEVELOPER: Access only involved project. Can only set query parameters projectManagerId from employees that
     *            are projectmanager in involved project. Gets 403 otherwise.
     * @param projectManagerId Get all projects with projectManagerId
     * @param fromDate Get all projects starting from date
*      *               (Contracts with start date before and end date after are included)
     * @param toDate Get all contracts until date
     *               (Contracts with start date before and end date after are included)
     * @return List of project DTOs
     * @throws ForbiddenException Developer tried to access foreign project or used projectManagerId that are not
     *                            involved in developers projects.
     * @throws ResourceNotFoundException Query parameter projectManagerId does not exist
     * @throws PreconditionFailedException Query parameter fromDate is after toDate
     */
    public List<ProjectDTO> findByQuery(Long projectManagerId, LocalDate fromDate, LocalDate toDate)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException{
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new PreconditionFailedException();
        Employee currentEmployee = employeeSession.getEmployee();
        List<Project> projects = null;

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER) {
            if(projectManagerId != null && !employeeRepository.existsById(projectManagerId))
                throw new ResourceNotFoundException("Project manager not found");
            projects = projectRepository.findByQuery(projectManagerId, fromDate, toDate);
        }


        //DEVELOPER
        else {
            //Checks if the employee tries to access other projectManagers via query parameters
            List<Long> involvedProjectManager = getInvolvedProjectManagerIds(currentEmployee);
            if(projectManagerId != null && !involvedProjectManager.contains(projectManagerId))
                throw new ForbiddenException(
                    "Missing permission to get the allocation (DEVELOPER: Other uninvolved employee or project)");

            projects = allocationRepository.findByQuery(currentEmployee.getId(), null, fromDate, toDate)
                    .stream()
                    .filter(a -> a.getContract().getEmployee().getId() == currentEmployee.getId())
                    .filter(a -> isActiveProject(a.getProject()))
                    .filter(a -> (projectManagerId == null) || (projectManagerId == a.getProject().getProjectManager().getId()))
                    .map(Allocation::getProject)
                    .collect(Collectors.toList());
        }
        return modelsToDTOs(projects);
    }

    /**
     * Find project by id.
     * ADMINISTRATOR, PROJECTMANAGER: Access to all contracts. Get informed if contract does not exists.
     * DEVELOPER: Access to only involved projects. Gets 403 if accesses to any other project (even if it does not
     *            exist).
     * @param id Identifier of the requested project
     * @return Allocation DTO of requested allocation
     * @throws ForbiddenException Developer tries to access foreign project
     * @throws ResourceNotFoundException Administrator or projectmanager access to non existing contract
     */
    public ProjectDTO findById(Long id) throws ForbiddenException, ResourceNotFoundException {
        Employee currentEmployee = employeeSession.getEmployee();
        Project project = null;

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER) {
            project = checkIfProjectExists(id);
        }

        //DEVELOPER
        else {
            try {
                project = checkIfProjectExists(id);
                List<Long> involvedProjectIds = getInvolvedProjects(currentEmployee)
                        .stream()
                        .map(Project::getId)
                        .collect(Collectors.toList());
                if(!involvedProjectIds.contains(id))
                    throw new Exception();
            } catch (Exception e) {
                throw new ForbiddenException("Missing permission to get the project (DEVELOPER: Not assigned project)");
            }
        }
        return mapper.projectToProjectDTO(project);

    }

    /**
     * Create a new project.
     * ADMIN: Able to create projects.
     * PROJECTMANAGER, DEVELOPER: Not able to create projects at all.
     * @param projectDto Received and validated project DTO object
     * @return Contract DTO of newly created contract
     * @throws ForbiddenException Developer or projectmanager tried to create new project
     * @throws ResourceNotFoundException projectManager employee referred to does not exist
     * @throws PreconditionFailedException fromDate is after toDate
     */
    @Transactional
    public Project createProject(ProjectDTO projectDto)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmploye = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmploye.getRole() == Role.ADMINISTRATOR) {
            checkDates(projectDto.getStartDate(), projectDto.getEndDate() );
			
			
            Project p = new Project(
                    checkString(projectDto.getName()),
                    checkIfEmployeeIsAProjectManager(checkEmployee(projectDto.getProjectManagerId())),
                    projectDto.getStartDate(),
                    projectDto.getEndDate(),
                    checkIfFTEIsPositive(projectDto.getFtePercentage()));
            p = projectRepository.save(p);
            return p;
        }

        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to create a project (PROJECTMANAGER, DEVELOPER)");
        }
    }



    /**
     * Update an project.
     * ADMINISTRATOR: Able to update all projects.
     * PROJECTMANAGER: Able to update  projects in which assigned as project manager.
     * DEVELOPER: Nt able to update allocations at all.
     * @param projectDTO Validated project DTO from request with updated fields
     * @return Project DTO of the updated project
     * @throws ForbiddenException Developer tried to update a project or projectmanager tried to update
     *                            foreign projects
     * @throws ResourceNotFoundException Employee referred to does not exist
     * @throws PreconditionFailedException start and end date overlap
     */
    @Transactional
    public ProjectDTO updateProject(ProjectDTO projectDTO, long id)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER)
        {
            checkDates(projectDTO.getStartDate(), projectDTO.getEndDate() );
            Project p = checkIfProjectExists(id);

            //PROJECTMANAGER
            if(currentEmployee.getRole() == Role.PROJECTMANAGER &&
                currentEmployee.getId() != p.getProjectManager().getId()) {
                throw new ForbiddenException(
                        "Missing permission to update the project " +
                        "(PROJECTMANAGER: Somebody else's project, DEVELOPER: All");
            }

            p.setName(checkString(projectDTO.getName()));
            p.setProjectManager(checkIfEmployeeIsAProjectManager(checkEmployee(projectDTO.getProjectManagerId())));
            p.setStartDate(projectDTO.getStartDate());
            p.setEndDate(projectDTO.getEndDate());
            p.setFtePercentage(checkIfNewFTEIsLargerThanSumOfAllocationFTEs(checkIfFTEIsPositive(projectDTO.getFtePercentage()), id));
            projectRepository.save(p);
            deleteFutureAllocations(p.getEndDate(), p.getId());
            return mapper.projectToProjectDTO(p);
        }

        //DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to update the project " +
                    "(PROJECTMANAGER: Somebody else's project, DEVELOPER: All");
        }
    }

    /**
     * Delete a project.
     * ADMIN: Able to delete projects.
     * PROJECTMANAGER, DEVELOPER: Not able to delete projects at all.
     * @param id Identifier of the requested contract to delete
     * @throws ForbiddenException Developer or projectmanager tried to delete a project
     * @throws ResourceNotFoundException Project with id does not exist
     */
    @Transactional
    public void deleteProject(Long id) throws ForbiddenException, ResourceNotFoundException{
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR) {
            Project project = checkIfProjectExists(id);
            projectRepository.delete(project);
        }

        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException("Missing permission to delete the project (PROJECTMANAGER, DEVELOPER)");
        }
    }


    /**
     * Checks if the repository contains an employee with the given id
     * @param projectId the id of the employee that needs to be checked.
     * @return The employee object if it was found in the repository
     * @throws Exception Throws a ResourceNotFoundException if the employee was not found.
     */

    private Project checkIfProjectExists(long projectId) throws ResourceNotFoundException {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if(projectOptional.isPresent()){
            return projectOptional.get();
        }
        throw new ResourceNotFoundException("Project not found");
    }

    /**
     * Check if the employee exists.
     * @param employeeID Identifier of the employee to check
     * @return Employee object
     * @throws ResourceNotFoundException Employee was not found
     */
    private Employee checkEmployee(long employeeID) throws ResourceNotFoundException, PreconditionFailedException{

        Optional<Employee> oProjectManager = employeeRepository.findById(employeeID);
        if(!oProjectManager.isPresent()){
            throw new ResourceNotFoundException("Project manager not found");
        }
        return oProjectManager.get();
    }

    /**
     * Checks if a given employee is a project manager
     * @param e the employee that needs to be checked
     * @return The employee if they are a project manager.
     * @throws Exception Throws a PreconditionFailedException if the employee is not a project manager.
     */
    private Employee checkIfEmployeeIsAProjectManager(Employee e) throws PreconditionFailedException{
        if(e.getRole().equals(Role.PROJECTMANAGER)){
            return e;
        }
        throw new PreconditionFailedException("The employee is not a project manager");
    }

    /**
     * Checks if the the start date lies before end date and if the end date lies after or is equal to the current date.
     * @param startDate the start date that needs to be checked
     * @param endDate the end date that needs to be checked
     * @throws Exception Throws a PreconditionFailedException if the dates are crossed or if the end date lies in the past.
     */

    
    private void checkDates(LocalDate startDate, LocalDate endDate) throws PreconditionFailedException{
        //TODO: Can start dates lie in the past?

        boolean startDateOverlapsEndDate = startDate.isAfter(endDate);
        boolean endDateIsBeforeNow = endDate.isBefore(LocalDate.now());
        if(startDateOverlapsEndDate){
            throw new PreconditionFailedException("The start date and end date must not overlap!");
        }
        if(endDateIsBeforeNow){
            throw new PreconditionFailedException("The end date cannot lie before the current date.");
        }
    }


    /**
     * Checks if the given float (FTE) is positive
     * @param FTE the float value that needs to be checked
     * @return the same float value if it is positive
     * @throws Exception Throws a PreconditionFailedException if the FTE is negative.
     */

    public float checkIfFTEIsPositive(float FTE) throws PreconditionFailedException {

        if(FTE < 0){
            throw new PreconditionFailedException("FTEs must not be negative");
        }
        return FTE;
    }


    /**
     * Checks if the given string is null or empty.
     * @param name the string that needs to be checked
     * @return the same string if it is not null and not empty
     * @throws Exception Throws a PreconditionFailedException if the string is either null or empty.
     */

    public String checkString(String name) throws PreconditionFailedException {

        if(isNullOrEmpty(name)){
            throw new PreconditionFailedException("String must not be empty");
        }
        return name;
    }


    /**
     * Checks if the given FTE limit is larger or equal to the sum of pensum percentages of all assigned allocation for this project.
     * @param FTE The FTE limit of the project
     * @param projectID the id of the project
     * @return the same FTE limit if the sum of pensum percentages of all assigned allocations if smaller or equal to the FTE limit.
     * @throws Exception throws a PreconditionFailedException if the sum of pensum percentages is larger than the FTE limit.
     */

    public float checkIfNewFTEIsLargerThanSumOfAllocationFTEs(float FTE, long projectID) throws PreconditionFailedException{

        int currentFTESum = allocationRepository.findAll().stream().filter(allocation ->
                allocation.getProject().getId() == projectID)
                .mapToInt(a -> a.getPensumPercentage())
                .sum();
        if(currentFTESum > FTE){
            throw new PreconditionFailedException("The FTE is smaller than the sum of FTEs across the allocations for this project");
        }
        return FTE;
    }

    /**
     * Deletes all allocations of the given project that start after the given end date and adjusts the end date of running allocations
     * This is used during editProject, if the end date is edited.
     * @param enddate the new end date of the project
     * @param projectID the id of the edited project
     */
    private void deleteFutureAllocations(LocalDate enddate, long projectID){
        //TODO: NOT tested in service tests. Needs to be tested in integration test.
        List<Allocation> affectedAllocations = allocationRepository.findAll().stream()
                .filter(allocation ->
                                allocation.getProject().getId() == projectID
                                && (allocation.getStartDate().isBefore(enddate))
                                && allocation.getEndDate().isAfter(enddate))
                .collect(Collectors.toList());

        List<Allocation> deleteAllocations = allocationRepository.findAll().stream()
                .filter(allocation ->
                        allocation.getProject().getId() == projectID
                        && allocation.getStartDate().isAfter(enddate))
                .collect(Collectors.toList());

        for(Allocation a : affectedAllocations){
            a.setEndDate(enddate);
        }
        for(Allocation a : deleteAllocations){
            allocationRepository.delete(a);
        }
    }

    private boolean isNullOrEmpty(String s){
        return s.trim().isEmpty() || s == null;
    }

    public List<ProjectDTO> modelsToDTOs(List<Project> projects) {
        return projects.stream().map(p -> mapper.projectToProjectDTO(p)).collect(Collectors.toList());
    }

    private boolean isActiveProject(Project project) {
        LocalDate now = LocalDate.now();
        return now.isAfter(project.getStartDate()) && now.isBefore(project.getEndDate());
    }

    private List<Long> getInvolvedProjectManagerIds(Employee employee) {
        return getInvolvedProjects(employee)
                .stream()
                .map(p -> p.getProjectManager().getId()).collect(Collectors.toList());
    }

    private List<Project> getInvolvedProjects(Employee employee){
        return allocationRepository.findByQuery(employee.getId(), null, null, null)
                .stream()
                .filter(a -> a.getContract().getEmployee().getId() == employee.getId())
                .filter(a -> isActiveProject(a.getProject()))
                .map(Allocation::getProject).collect(Collectors.toList());
    }
}
