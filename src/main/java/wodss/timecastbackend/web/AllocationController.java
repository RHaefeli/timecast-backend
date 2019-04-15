package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.service.AllocationService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/allocations")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @GetMapping
    public @ResponseBody List<AllocationDTO> getAllAllocations(
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate toDate)
    throws Exception {
        return allocationService.findbyQuery(employeeId, projectId, fromDate, toDate);
    }

    @PostMapping
    public @ResponseBody AllocationDTO createAllocation(@RequestBody AllocationDTO allocationDto) throws Exception {
        return allocationService.createAllocation(allocationDto);
    }

    @GetMapping(value = "/{id}")
    public @ResponseBody AllocationDTO getContractById(@PathVariable long id)
            throws Exception { return allocationService.findById(id); }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteContract(@PathVariable long id)
            throws Exception {
        allocationService.deleteAllocation(id);
        return new ResponseEntity<String>("Ressource succesfully deleted", HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/{id}")
    public @ResponseBody AllocationDTO editAllocation(@PathVariable long id, @RequestBody AllocationDTO allocationDTO)
            throws Exception {
        return allocationService.editAllocation(id, allocationDTO);
    }
}
