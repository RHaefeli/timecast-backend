package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.RoleRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.services.EmployeeService;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService empService){
        this.employeeService = empService;
    }


    @GetMapping
    public @ResponseBody List<EmployeeDTO> getAllUsers() {
        return employeeService.getAllEmployees();
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<EmployeeDTO>getUser(@PathVariable Long id){
        EmployeeDTO dto = employeeService.getEmployee(id);
        if(dto != null){
            return new ResponseEntity<EmployeeDTO>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<EmployeeDTO>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createUser(@RequestBody EmployeeDTO employeeDto){
        Employee e = employeeService.createEmployee(employeeDto);
        employeeDto.setId(e.getId());
        return new ResponseEntity<EmployeeDTO>(employeeDto, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<EmployeeDTO> update(@RequestBody Employee employeeUpdate, @PathVariable Long id) {
        EmployeeDTO dto = employeeService.updateEmployee(employeeUpdate, id);
        if(dto != null){
            return new ResponseEntity<EmployeeDTO>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<EmployeeDTO>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return employeeService.deleteEmployee(id);
    }


}
