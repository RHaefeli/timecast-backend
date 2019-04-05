package wodss.timecastbackend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, EmployeeRepository employeeRepository, ModelMapper mapper){
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    public List<ProjectDTO> getAllProjects() {
        List<ProjectDTO> projectDtos = projectRepository.findAll().stream().map(p -> mapper.projectToProjectDTO(p)).collect(Collectors.toList());
        return projectDtos;
    }

    public ProjectDTO getProject(Long id) throws Exception{
        Optional<Project> projectOptional = projectRepository.findById(id);
        if(projectOptional.isPresent()){
            return mapper.projectToProjectDTO(projectOptional.get());
        }
        throw new RessourceNotFoundException();
    }

    public Project createProject(ProjectDTO projectDto) throws Exception{

        Employee projectManager = checkEmployee(projectDto.getProjectManagerId());
        checkDates(projectDto.getStartDate(), projectDto.getEndDate() );

        Project p = new Project(
                projectDto.getName(),
                projectManager,
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getFtePercentage());
        p = projectRepository.save(p);
        return p;
    }

    public ProjectDTO updateProject(Project projectUpdate, Long id) throws Exception{
        checkDates(projectUpdate.getStartDate(), projectUpdate.getEndDate() );

        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            Project p = projectOptional.get();
            p.setName(projectUpdate.getName());
            p.setProjectManager(projectUpdate.getProjectManager());
            p.setStartDate(projectUpdate.getStartDate());
            p.setEndDate(projectUpdate.getEndDate());
            p.setFtePercentage(projectUpdate.getFtePercentage());
            projectRepository.save(p);

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
        if(oProjectManager.isPresent() && oProjectManager.get().getRole().equals(Role.PROJECTMANAGER)) {
            return oProjectManager.get();
        } else {
            throw new PreconditionFailedException();
        }
    }
    private void checkDates(LocalDate startDate, LocalDate endDate) throws Exception{
        //TODO: Can start dates lie in the past?
        boolean startDateOverlapsEndDate = startDate.isAfter(endDate);
        if(startDateOverlapsEndDate){
            throw new PreconditionFailedException("The start date and end date must not overlap!");
        }
    }
}
