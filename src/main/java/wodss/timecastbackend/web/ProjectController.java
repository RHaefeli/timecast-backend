package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.service.ProjectService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService ps){
        this.projectService = ps;
    }


    @GetMapping
    public @ResponseBody List<ProjectDTO> getAllProjects (
            @RequestParam(value = "projectManagerId", required = false) Long projectManagerId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate toDate)
            throws  Exception {
        return projectService.findByQuery(projectManagerId, fromDate, toDate);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<ProjectDTO>getProject(@PathVariable Long id) throws Exception{
        return new ResponseEntity<ProjectDTO>(projectService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDto,
                                                    HttpServletResponse response) throws Exception{
        Project project = projectService.createProject(projectDto);
        projectDto.setId(project.getId());
        return new ResponseEntity<ProjectDTO>(projectDto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProjectDTO> update(@Valid @RequestBody ProjectDTO projectDto, @PathVariable Long id) throws Exception{
        return new ResponseEntity<ProjectDTO>(projectService.updateProject(projectDto, id), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) throws Exception{
        projectService.deleteProject(id);
        return new ResponseEntity<String>(
                "Project and allocations successfully deleted (ADMINISTRATOR)", HttpStatus.NO_CONTENT);
    }

}
