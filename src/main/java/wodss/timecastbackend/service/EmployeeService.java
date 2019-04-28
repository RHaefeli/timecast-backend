package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper mapper, PasswordEncoder passwordEncoder){
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Uses a query to return all employees with a certain role.
     * @param sRole The role that the filtered employees must have in string form.
     * @return A filtered list of EmployeeDTOs where all employees have the specified role.
     * @throws Exception
     */
    public List<EmployeeDTO> findByQuery(String sRole) throws Exception {
        Role role = convertStringToRoleEnum(sRole);
        return modelsToDTOs(employeeRepository.findByQuery(role));
    }

    /**
     * Returns the employee with a given id in form of a DTO.
     * @param id the ID of the employee
     * @return the found employee in form of a DTO
     * @throws Exception Throws a RessourceNotFoundException if the employee was not found.
     */
    public EmployeeDTO getEmployee(Long id) throws Exception{
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if(employeeOptional.isPresent()){
            return mapper.employeeToEmployeeDTO(employeeOptional.get());
        }
        throw new RessourceNotFoundException();
    }


    public Employee createEmployee(EmployeeDTO employeeDTO, String role, String password) throws Exception{
        employeeDTO.outputDTODebug();
        Role r = checkIfRoleExists(role);
        checkStrings(employeeDTO.getFirstName(), employeeDTO.getLastName(), employeeDTO.getEmailAddress());
        checkIfMailIsUnique(employeeDTO.getEmailAddress());
        String pw = passwordEncoder.encode(password);
        Employee e = new Employee(
                employeeDTO.getLastName(),
                employeeDTO.getFirstName(),
                employeeDTO.getEmailAddress(),
                r,
                pw
                );
        e = employeeRepository.save(e);
        return e;
    }

    public EmployeeDTO updateEmployee(EmployeeDTO employeeUpdate, Long id) throws Exception {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            checkStrings(employeeUpdate.getFirstName(), employeeUpdate.getLastName(), employeeUpdate.getEmailAddress());
            Employee e = employeeOptional.get();
            e.setFirstName(employeeUpdate.getFirstName());
            e.setLastName((employeeUpdate.getLastName()));
            e.setEmailAddress(employeeUpdate.getEmailAddress());
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

    /**
     * Checks if the passed string can be translated to a valid role. (ADMINISTRATOR, DEVELOPER, PROJECTMANAGER)
     * If role was not found, a PreconditionFailedException is thrown.
     * @param role the role string
     * @return The found role, if available.
     * @throws Exception Throws a PreconditionFailedException if role is not found.
     */
    private Role checkIfRoleExists(String role) throws Exception{
        Optional<Role> oRole = Arrays.stream(Role.values())
                .filter(v -> role.toLowerCase().equals(v.getValue().toLowerCase()))
                .findFirst();

        if(oRole.isPresent()){
            return oRole.get();
        }
        else{
            throw new PreconditionFailedException("Invalid Role was passed");
        }
    }

    /**
     * Checks if the given strings for firstName, lastName and emailAdress are valid.
     * A string is valid if they are not null or empty and, in case of the emailAdress, pass through the regex.
     * @param firstName The first name that needs to be checked
     * @param lastName the last name that needs to be checked
     * @param emailAddress the email that needs to be checked.
     * @throws Exception Throws a PreconditionFailedException if any one of these tests fails.
     */
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

    /**
     * Checks if a string is null or Empty
     * @param s the string that needs to be checked.
     * @return true if the string is null or empty, false if it isn't.
     */
    private boolean isNullOrEmpty(String s){
        return s.trim().isEmpty() || s == null;
    }

    /**
     * Checks if the given string is a valid email address.
     * Source of the regex: https://www.geeksforgeeks.org/check-email-address-valid-not-java/
     * @param email the email string
     * @return true if the string is a valid email address, false if it is not.
     */
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

    /**
     * Checks if a given email address is unique in the repository.
     * @param emailAddress the email address string
     * @throws Exception Throws a PreconditionFailedException if the email was found in the repository.
     */
    private void checkIfMailIsUnique(String emailAddress) throws Exception {
        if(employeeRepository.existsByEmailAddress(emailAddress))
            throw new PreconditionFailedException();
    }

    /**
     * Takes a list of Employee objects and converts them to DTOs
     * @param employees the list of employees that needs to be converted into DTOs
     * @return a list which contains the given employees, converted into DTOs.
     */
    private List<EmployeeDTO> modelsToDTOs(List<Employee> employees) {
        return employees.stream().map(e -> mapper.employeeToEmployeeDTO(e)).collect(Collectors.toList());
    }

    private Role convertStringToRoleEnum(String sRole) {
        if(sRole != null)
            return Role.valueOf(sRole.toUpperCase());
        return null;
    }
}
