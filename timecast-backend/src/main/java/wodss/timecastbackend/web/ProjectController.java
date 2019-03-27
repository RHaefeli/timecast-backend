package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.service.ProjectService;

import java.util.List;

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
    public ResponseEntity<ProjectDTO>getProject(@PathVariable Long id) throws Exception{
        return new ResponseEntity<ProjectDTO>(projectService.getProject(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDto) throws Exception{
        Project project = projectService.createProject(projectDto);

        projectDto.setId(project.getId());
        return new ResponseEntity<ProjectDTO>(projectDto, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProjectDTO> update(@RequestBody Project projectUpdate, @PathVariable Long id) throws Exception{
        return new ResponseEntity<ProjectDTO>(projectService.updateProject(projectUpdate,id ), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) throws Exception{
        return projectService.deleteProject(id);

    }

}
