package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Assignment;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.User;
import wodss.timecastbackend.dto.AssignmentDTO;
import wodss.timecastbackend.persistence.AssignmentRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.persistence.UserRepository;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper mapper;

    @GetMapping
    public @ResponseBody List<AssignmentDTO> getAllAssignments(){
        List<AssignmentDTO> assignmentDtos = assignmentRepository.findAll().stream().map(a -> mapper.assignmentToAssignmentDTO(a)).collect(Collectors.toList());
        return assignmentDtos;
    }

    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(@RequestBody AssignmentDTO assignmentDto) {
        //TODO: Validation

        Project project = null;
        Optional<Project> oProject = projectRepository.findById(assignmentDto.getProjectId());
        if(oProject.isPresent()){
            project = oProject.get();
        } else {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }

        User user = null;
        Optional<User> oUser = userRepository.findById(assignmentDto.getUserId());
        if(oUser.isPresent()){
            user = oUser.get();
        } else {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }

        Assignment assignment = new Assignment(project, user, assignmentDto.getEmployment(), assignmentDto.getStartDate(), assignmentDto.getEndDate());
        assignment = assignmentRepository.save(assignment);
        return new ResponseEntity<>(assignmentDto, HttpStatus.OK);
    }
}
