package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.ProjectDTO;
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
    private ModelMapper mapper;

    @GetMapping
    public @ResponseBody List<ProjectDTO> getAllProjects() {
        List<ProjectDTO> projectDtos = projectRepository.findAll().stream().map(p -> mapper.projectToProjectDTO(p)).collect(Collectors.toList());
        return projectDtos;
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDto) {
        //TODO: Validation

        Project project = new Project(projectDto.getName(), projectDto.getStartDate(), projectDto.getEndDate(), projectDto.getEstimatedEndDate(), projectDto.getFtes());
        project = projectRepository.save(project);
        projectDto.setId(project.getId());
        return new ResponseEntity<ProjectDTO>(projectDto, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProjectDTO> update(@RequestBody Project projectUpdate, @PathVariable Long id) {
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            Project p = projectOptional.get();
            p.setName(projectUpdate.getName());
            p.setStartDate(projectUpdate.getStartDate());
            p.setEndDate(projectUpdate.getEndDate());
            p.setEstimatedEndDate(projectUpdate.getEstimatedEndDate());
            p.setFtes(projectUpdate.getFtes());
            projectRepository.save(p);

            ProjectDTO dto = new ProjectDTO(p.getId(), p.getName(), p.getStartDate(), p.getEndDate(), p.getEstimatedEndDate(), p.getFtes());
            return new ResponseEntity<ProjectDTO>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<ProjectDTO>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            projectRepository.delete(project);
            return new ResponseEntity<String>(HttpStatus.OK);
        }
        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    }

}
