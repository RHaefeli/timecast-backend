package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.services.ProjectService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService ps){
        this.projectService = ps;
    }

    @GetMapping
    public @ResponseBody List<ProjectDTO> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<ProjectDTO>getProject(@PathVariable Long id){
        ProjectDTO dto = projectService.getProject(id);
        if(dto != null){
            return new ResponseEntity<ProjectDTO>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<ProjectDTO>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDto) {
        //TODO: Validation

        Project project = projectService.createProject(projectDto);
        projectDto.setId(project.getId());
        return new ResponseEntity<ProjectDTO>(projectDto, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProjectDTO> update(@RequestBody Project projectUpdate, @PathVariable Long id) {
        ProjectDTO dto = projectService.updateProject(projectUpdate,id );
        if(dto != null){

            return new ResponseEntity<ProjectDTO>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<ProjectDTO>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return projectService.deleteProject(id);

    }

}
