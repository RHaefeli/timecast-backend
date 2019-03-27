package wodss.timecastbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.persistence.RoleRepository;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper mapper;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository, ModelMapper mapper){
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    public List<EmployeeDTO> getAllEmployees() {
        List<EmployeeDTO> employeeDTOS = employeeRepository.findAll().stream().map(dto -> mapper.employeeToEmployeeDTO(dto)).collect(Collectors.toList());
        return employeeDTOS;
    }

    public EmployeeDTO getEmployee(Long id){
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if(employeeOptional.isPresent()){
            return mapper.employeeToEmployeeDTO(employeeOptional.get());
        }
        return null;
    }

    public Employee createEmployee(EmployeeDTO employeeDTO){
        //TODO: Validation necessary? (for role id etc)
        Employee e = new Employee(
                employeeDTO.getLastName(),
                employeeDTO.getFirstName(),
                employeeDTO.getEmailAddress(),
                roleRepository.getOne(employeeDTO.getRoleId())
                );
        e = employeeRepository.save(e);
        return e;
    }

    public EmployeeDTO updateEmployee(Employee employeeUpdate, Long id){
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            Employee e = employeeOptional.get();
            e.setFirstName(employeeUpdate.getFirstName());
            e.setLastName((employeeUpdate.getLastName()));
            e.setEmailAddress(employeeUpdate.getEmailAddress());
            e.setRole(employeeUpdate.getRole());
            employeeRepository.save(e);

            return mapper.employeeToEmployeeDTO(e);

        }
        return null;
    }

    public ResponseEntity<String> deleteEmployee(Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            Employee emp = employeeOptional.get();
            employeeRepository.delete(emp);
            return new ResponseEntity<String>(HttpStatus.OK);
        }
        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    }

}
