package wodss.timecastbackend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Allocation;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.util.BadRequestException;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

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

    @Autowired
    public ProjectService(ProjectRepository projectRepository, EmployeeRepository employeeRepository, AllocationRepository allocationRepository, ModelMapper mapper){
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.allocationRepository = allocationRepository;
        this.mapper = mapper;
    }

    public List<ProjectDTO> findByQuery(Long projectManagerId, LocalDate fromDate, LocalDate toDate) throws Exception{
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new BadRequestException();
        return modelsToDTOs(projectRepository.findByQuery(projectManagerId, fromDate, toDate));
    }

    public ProjectDTO getProject(Long id) throws Exception{
        Optional<Project> projectOptional = projectRepository.findById(id);
        if(projectOptional.isPresent()){
            return mapper.projectToProjectDTO(projectOptional.get());
        }
        throw new RessourceNotFoundException();
    }

    public Project createProject(ProjectDTO projectDto) throws Exception{

        checkDates(projectDto.getStartDate(), projectDto.getEndDate() );

        checkString(projectDto.getName());
        Project p = new Project(
                checkString(projectDto.getName()),
                checkIfEmployeeIsAProjectManager(checkEmployee(projectDto.getProjectManagerId())),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                checkIfFTEIsPositive(projectDto.getFtePercentage()));
        p = projectRepository.save(p);
        return p;
    }

    public ProjectDTO updateProject(ProjectDTO projectUpdate, Long id) throws Exception{

        Optional<Project> projectOptional = projectRepository.findById(id);

        if (projectOptional.isPresent()) {
            checkDates(projectUpdate.getStartDate(), projectUpdate.getEndDate() );

            Project p = projectOptional.get();
            p.setName(checkString(projectUpdate.getName()));
            p.setProjectManager(checkIfEmployeeIsAProjectManager(checkEmployee(projectUpdate.getProjectManagerId())));
            p.setStartDate(projectUpdate.getStartDate());
            p.setEndDate(projectUpdate.getEndDate());
            p.setFtePercentage(checkIfNewFTEIsLargerThanSumOfAllocationFTEs(checkIfFTEIsPositive(projectUpdate.getFtePercentage()), id));

            projectRepository.save(p);
            deleteFutureAllocations(p.getEndDate(), p.getId());
            return mapper.projectToProjectDTO(p);
        }
        throw new RessourceNotFoundException();
    }

    public ResponseEntity<String> deleteProject(Long id) throws Exception{
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            projectRepository.delete(project);
            return new ResponseEntity<String>(HttpStatus.OK);
        }
        throw new RessourceNotFoundException();
    }

    /**
     * Checks if the repository contains an employee with the given id
     * @param employeeID the id of the employee that needs to be checked.
     * @return The employee object if it was found in the repository
     * @throws Exception Throws a ResourceNotFoundException if the employee was not found.
     */
    private Employee checkEmployee(long employeeID) throws Exception{

        Optional<Employee> oProjectManager = employeeRepository.findById(employeeID);
        if(!oProjectManager.isPresent()){
            throw new RessourceNotFoundException();
        }
        return oProjectManager.get();
    }

    /**
     * Checks if a given employee is a project manager
     * @param e the employee that needs to be checked
     * @return The employee if they are a project manager.
     * @throws Exception Throws a PreconditionFailedException if the employee is not a project manager.
     */
    private Employee checkIfEmployeeIsAProjectManager(Employee e) throws Exception{
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
    private void checkDates(LocalDate startDate, LocalDate endDate) throws Exception{
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
    private float checkIfFTEIsPositive(float FTE) throws Exception {
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
    private String checkString(String name) throws Exception{
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
    private float checkIfNewFTEIsLargerThanSumOfAllocationFTEs(float FTE, long projectID) throws Exception{
        //The stream filters for all allocations that belong to the given project and sums up their pensum percentage.
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
}
