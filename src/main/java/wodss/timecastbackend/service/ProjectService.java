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

    //TODO: Uneinheitlich. Rückgabetyp zu DTO wechseln?
    public Project createProject(ProjectDTO projectDto) throws Exception{

        //TODO: Can you assign a project manager if their contract(s) run out before the project ends?

        checkDates(projectDto.getStartDate(), projectDto.getEndDate() );

        checkString(projectDto.getName());
        //TODO: Check if FTE is positive. Check if name is not null or empty.
        Project p = new Project(
                checkString(projectDto.getName()),
                checkEmployee(projectDto.getProjectManagerId()),
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
            p.setProjectManager(checkEmployee(projectUpdate.getProjectManagerId()));
            p.setStartDate(projectUpdate.getStartDate());
            p.setEndDate(projectUpdate.getEndDate());
            p.setFtePercentage(checkIfNewFTEIsLargerThanSumOfAllocationFTEs(checkIfFTEIsPositive(projectUpdate.getFtePercentage()), id));

            projectRepository.save(p);
            checkForFutureAllocations(p.getEndDate(), p.getId());
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


    private Employee checkEmployee(long employeeID) throws Exception{

        Optional<Employee> oProjectManager = employeeRepository.findById(employeeID);
        if(!oProjectManager.isPresent()){
            throw new RessourceNotFoundException();
        }
        else if(!oProjectManager.get().getRole().equals(Role.PROJECTMANAGER)){
            throw new PreconditionFailedException("Chosen Employee is not a project manager!");
        }
        return oProjectManager.get();
    }
    private void checkDates(LocalDate startDate, LocalDate endDate) throws Exception{
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

    public float checkIfFTEIsPositive(float FTE) throws Exception {
        if(FTE < 0){
            throw new PreconditionFailedException("FTEs must not be negative");
        }
        return FTE;
    }

    public String checkString(String name) throws Exception{
        if(isNullOrEmpty(name)){
            throw new PreconditionFailedException("String must not be empty");
        }
        return name;
    }

    public float checkIfNewFTEIsLargerThanSumOfAllocationFTEs(float FTE, long projectID) throws Exception{
        int currentFTESum = allocationRepository.findAll().stream().filter(allocation ->
                allocation.getProject().getId() == projectID).mapToInt(a -> a.getPensumPercentage())
                .sum();
        if(currentFTESum > FTE){
            throw new PreconditionFailedException("The FTE is smaller than the sum of FTEs across the allocations for this project");
        }
        return FTE;
    }

    /**
     * Deletes all allocations of the given project that lie after the given end date and adjusts the enddate of running allocations
     * This is used during editProject, if the end date is edited. any future allocations must be deleted.
     * @param enddate
     */
    public void checkForFutureAllocations(LocalDate enddate, long projectID){
        //TODO: Define SQL queries for these.
        //TODO: NOT tested in service tests. Needs to be tested in integration test.
        List<Allocation> affectedAllocations = allocationRepository.findAll().stream().filter(allocation ->
                allocation.getProject().getId() == projectID
                        && (allocation.getStartDate().isBefore(enddate)) && allocation.getEndDate().isAfter(enddate)).collect(Collectors.toList());
        List<Allocation> deleteAllocations = allocationRepository.findAll().stream().filter(allocation ->
                allocation.getProject().getId() == projectID && allocation.getStartDate().isAfter(enddate)).collect(Collectors.toList());
        for(Allocation a : affectedAllocations){
            a.setEndDate(enddate);
        }
        for(Allocation a : deleteAllocations){
            allocationRepository.delete(a);
        }
    }

    public LocalDate checkIfEndDateLiesBeforeNow(LocalDate endDate) throws Exception{
        if(endDate.isBefore(LocalDate.now())){
            throw new PreconditionFailedException("The end date of the project cannot be before the current date.");
        }
        return endDate;
    }

    private boolean isNullOrEmpty(String s){
        return s.trim().isEmpty() || s == null;
    }

    public List<ProjectDTO> modelsToDTOs(List<Project> projects) {
        return projects.stream().map(p -> mapper.projectToProjectDTO(p)).collect(Collectors.toList());
    }
}
