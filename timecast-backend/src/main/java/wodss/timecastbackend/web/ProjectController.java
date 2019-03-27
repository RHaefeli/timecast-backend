package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ModelMapper mapper;

    @GetMapping
    public @ResponseBody List<ProjectDTO> getAllProjects() {
        List<ProjectDTO> projectDtos = projectRepository.findAll().stream().map(p -> mapper.projectToProjectDTO(p)).collect(Collectors.toList());
        return projectDtos;
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDto) {
        //TODO: Validation

        Employee projectManager = null;
        Optional<Employee> oProjectManager = employeeRepository.findById(projectDto.getProjectManagerId());
        if(oProjectManager.isPresent()) {
            projectManager = oProjectManager.get();
        } else {
            return new ResponseEntity<ProjectDTO>(HttpStatus.PRECONDITION_FAILED);
        }

        Project project = new Project(projectDto.getName(), projectManager, projectDto.getStartDate(), projectDto.getEndDate(), projectDto.getFtePercentage());
        project = projectRepository.save(project);
        projectDto.setId(project.getId());
        return new ResponseEntity<ProjectDTO>(projectDto, HttpStatus.OK);
    }
}
