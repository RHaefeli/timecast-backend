package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.service.EmployeeService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService empService){
        this.employeeService = empService;
    }

    @GetMapping
    public @ResponseBody List<EmployeeDTO> getAllUsers(
            @RequestParam(value = "role", required = false) String role)
        throws Exception{
        return employeeService.findByQuery(role);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<EmployeeDTO>getUser(@PathVariable Long id)  throws Exception{
        return new ResponseEntity<EmployeeDTO>(employeeService.getEmployee(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createUser(@Valid @RequestBody EmployeeDTO employeeDto,
                                                  @RequestParam(required = true) String role,
                                                  @RequestParam(required = true) String password,
                                                  HttpServletResponse response)  throws Exception{
        employeeDto = employeeService.createEmployee(employeeDto, role, password);
        return new ResponseEntity<EmployeeDTO>(employeeDto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<EmployeeDTO> update(@Valid @RequestBody Employee employeeUpdate, @PathVariable Long id)  throws Exception{
        return new ResponseEntity<EmployeeDTO>(employeeService.updateEmployee(employeeUpdate, id), HttpStatus.OK);

    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id)  throws Exception{
        employeeService.deleteEmployee(id);
        return new ResponseEntity<String>("Employee successfully anonymized (ADMINISTRATOR)", HttpStatus.NO_CONTENT);
    }


}
