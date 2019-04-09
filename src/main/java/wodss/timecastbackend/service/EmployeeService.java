package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper mapper){
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    public List<EmployeeDTO> getAllEmployees() {
        List<EmployeeDTO> employeeDTOS = employeeRepository.findAll().stream().map(dto -> mapper.employeeToEmployeeDTO(dto)).collect(Collectors.toList());
        return employeeDTOS;
    }

    public EmployeeDTO getEmployee(Long id) throws Exception{
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if(employeeOptional.isPresent()){
            return mapper.employeeToEmployeeDTO(employeeOptional.get());
        }
        throw new RessourceNotFoundException();
    }

    public Employee createEmployee(EmployeeDTO employeeDTO, String role) throws Exception{
        employeeDTO.outputDTODebug();
        Role r = checkIfRoleExists(role);
        checkStrings(employeeDTO.getFirstName(), employeeDTO.getLastName(), employeeDTO.getEmailAddress());

        Employee e = new Employee(
                employeeDTO.getLastName(),
                employeeDTO.getFirstName(),
                employeeDTO.getEmailAddress(),
                r
                );
        e = employeeRepository.save(e);
        return e;
    }

    public EmployeeDTO updateEmployee(Employee employeeUpdate, Long id) throws Exception {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            Employee e = employeeOptional.get();
            e.setFirstName(employeeUpdate.getFirstName());
            e.setLastName((employeeUpdate.getLastName()));
            e.setEmailAddress(employeeUpdate.getEmailAddress());
            //e.setRole(employeeUpdate.getRole());
            employeeRepository.save(e);

            return mapper.employeeToEmployeeDTO(e);

        }
        throw new RessourceNotFoundException();
    }

    public ResponseEntity<String> deleteEmployee(Long id) throws Exception{
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            Employee emp = employeeOptional.get();
            employeeRepository.delete(emp);
            return new ResponseEntity<String>(HttpStatus.OK);
        }
        throw new RessourceNotFoundException();
    }

    private Role checkIfRoleExists(String role) throws Exception{
        Optional<Role> oRole = Arrays.stream(Role.values())
                .filter(v -> role.toLowerCase().equals(v.getValue().toLowerCase()))
                .findFirst();

        if(oRole.isPresent()){
            System.out.println("Exists, role = " + oRole.get().getValue());
            return oRole.get();
        }
        else{

            throw new PreconditionFailedException("Invalid Role id was passed");
        }
    }
    private void checkStrings(String firstName, String lastName, String emailAddress) throws Exception{
        if(isNullOrEmpty(firstName)){
            throw new PreconditionFailedException("Invalid first name");
        }
        if(isNullOrEmpty(lastName)){
            throw new PreconditionFailedException("Invalid last name");
        }
        if(!isValid(emailAddress)){
            throw new PreconditionFailedException("invalid email");
        }
    }
    private boolean isNullOrEmpty(String s){
        return s.trim().isEmpty() || s == null;
    }

    private boolean isValid(String email)
    {
        //Source: https://www.geeksforgeeks.org/check-email-address-valid-not-java/
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}