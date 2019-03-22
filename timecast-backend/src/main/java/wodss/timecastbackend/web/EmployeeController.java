package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.persistence.RoleRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper mapper; //This error is due to IntelliJ not being capable of intepreting the @Spring

    @GetMapping
    public @ResponseBody List<EmployeeDTO> getAllUsers() {
        List<EmployeeDTO> employeeDtos = employeeRepository.findAll().stream().map(u -> mapper.employeeToEmployeeDTO(u)).collect(Collectors.toList());
        return employeeDtos;
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createUser(@RequestBody EmployeeDTO employeeDto){
        //TODO: Validation

        Role role = null;
        Optional<Role> oRole = roleRepository.findById(employeeDto.getRoleId());
        if(oRole.isPresent()) {
            role = oRole.get();
        } else {
            return new ResponseEntity<EmployeeDTO>(HttpStatus.PRECONDITION_FAILED);
        }

        Employee employee = new Employee(employeeDto.getLastName(), employeeDto.getFirstName(), employeeDto.getEmailAddress(), role);
        employee = employeeRepository.save(employee);
        employeeDto.setId(employee.getId());
        return new ResponseEntity<EmployeeDTO>(employeeDto, HttpStatus.OK);
    }

}
