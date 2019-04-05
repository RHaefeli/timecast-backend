package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Allocation;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.persistence.AssignmentRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/allocations")
public class AllocationController {

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ModelMapper mapper;

    @GetMapping
    public @ResponseBody List<AllocationDTO> getAllAssignments(){
        List<AllocationDTO> allocationDTOS = assignmentRepository.findAll().stream().map(a -> mapper.allocationToAllocationDTO(a)).collect(Collectors.toList());
        return allocationDTOS;
    }

    @PostMapping
    public ResponseEntity<AllocationDTO> createAssignment(@RequestBody AllocationDTO allocationDto) {
        //TODO: Validation

        Project project = null;
        Optional<Project> oProject = projectRepository.findById(allocationDto.getProjectId());
        if(oProject.isPresent()){
            project = oProject.get();
        } else {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }

        Contract contract = null;
        Optional<Contract> oUser = contractRepository.findById(allocationDto.getContractId());
        if(oUser.isPresent()){
            contract = oUser.get();
        } else {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }

        Allocation allocation = new Allocation(project, contract, allocationDto.getPensumPercentage(), allocationDto.getStartDate(), allocationDto.getEndDate());
        allocation = assignmentRepository.save(allocation);
        return new ResponseEntity<>(allocationDto, HttpStatus.OK);
    }
}
