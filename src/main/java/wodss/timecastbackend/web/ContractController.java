package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.dto.ContractDTO;
import wodss.timecastbackend.service.ContractService;
import wodss.timecastbackend.util.AuthentificationException;
import wodss.timecastbackend.util.PreconditionFailedException;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @GetMapping
    public @ResponseBody List<ContractDTO> getAllContracts(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate toDate)
            throws Exception {
        return contractService.findByQuery(fromDate, toDate);
    }

    @PostMapping
    public @ResponseBody ContractDTO createContract(@RequestBody ContractDTO contractDTO) throws Exception {
        return contractService.createContract(contractDTO);
    }

    @GetMapping(value="/{id}")
    public @ResponseBody ContractDTO getContractById(@PathVariable long id) throws Exception {
        return contractService.findById(id);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteContract(@PathVariable long id) throws Exception {
        contractService.deleteContract(id);
        return new ResponseEntity<String>("Ressource succesfully deleted", HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/{id}")
    public @ResponseBody ContractDTO editContract(@PathVariable long id, @RequestBody ContractDTO contractDTO) throws Exception {
        return contractService.editContract(id, contractDTO);
    }
}
