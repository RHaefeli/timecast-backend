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
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.service.AllocationService;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/allocations")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private ModelMapper mapper;

    @GetMapping
    public @ResponseBody List<AllocationDTO> getAllAllocations(){ return allocationService.findAll(); }

    @PostMapping
    public @ResponseBody AllocationDTO createAllocation(@RequestBody AllocationDTO allocationDto) throws Exception {
        return allocationService.createAllocation(allocationDto);
    }

    @GetMapping(value = "/{id}")
    public @ResponseBody AllocationDTO getContractById(@PathVariable long id) throws Exception { return allocationService.findById(id); }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteContract(@PathVariable long id) throws Exception {
        allocationService.deleteAllocation(id);
        return new ResponseEntity<String>("Ressource succesfully deleted", HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/{id}")
    public @ResponseBody AllocationDTO editAllocation(@PathVariable long id, @RequestBody AllocationDTO allocationDTO) throws Exception {
        return allocationService.editAllocation(id, allocationDTO);
    }
}
