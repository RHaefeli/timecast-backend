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
import wodss.timecastbackend.security.EmployeeSession;
import wodss.timecastbackend.util.ForbiddenException;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.ResourceNotFoundException;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;
    private PasswordEncoder passwordEncoder;
    private EmployeeSession employeeSession;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper mapper, PasswordEncoder passwordEncoder,
                           EmployeeSession employeeSession) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.employeeSession = employeeSession;
    }

    /**
     * Find contracts with optional query parameter.
     * ALL: Access all employees.
     * @param sRole Get all employees with role
     * @return List of employee DTOs
     */
    public List<EmployeeDTO> findByQuery(String sRole) {

        //ALL
        Role role = convertStringToRoleEnum(sRole);
        return modelsToDTOs(employeeRepository.findByQuery(role));
    }

    public EmployeeDTO getEmployee(Long id) throws Exception{

        //ALL
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if(employeeOptional.isPresent()){
            return mapper.employeeToEmployeeDTO(employeeOptional.get());
        }
        throw new ResourceNotFoundException();
    }

    /**
     * Create a new employee. This service is available for anonymous user in purpouse to create new credentials.
     * ADMIN: Able to create employee with any role and active state.
     * EVERYONE: Able to create employee only with developer role and true active state
     *           (Query param role and active in DTO will be ignored).
     * @param employeeDTO Received and validated employee DTO object
     * @param role The role of the new employee
     * @param password The password of the new employee
     * @return Employee DTO of newly created employee
     * @throws Exception
     */
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO, String role, String password)
            throws PreconditionFailedException {
        String defRole = "developer";
        boolean defActive = true;
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee != null && currentEmployee.getRole() == Role.ADMINISTRATOR) {
            defRole = role;
            defActive = employeeDTO.getActive();
        }

        //EVERYONE
        Role r = checkIfRoleExists(defRole);
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
        e.setActive(defActive);
        e = employeeRepository.save(e);
        return mapper.employeeToEmployeeDTO(e);
    }

    /**
     * Update an employee.
     * ADMINISTRATOR: Able to update all employees.
     * PROJECTMANAGER, DEVELOPER: Not able to update contracts at all.
     * @param employeeDTO Validated employee DTO with updated fields
     * @param id Identifier of the requested employee
     * @return Employee DTO of the updated employee
     * @throws ForbiddenException Developer or Projectmanager tried to update an employee
     * @throws ResourceNotFoundException Employee with id does not exist
     * @throws PreconditionFailedException Email Address already exists
     */
    public EmployeeDTO updateEmployee(Employee employeeDTO, Long id) throws ForbiddenException,
            ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR) {
            Optional<Employee> employeeOptional = employeeRepository.findById(id);
            if (employeeOptional.isPresent()) {
                checkIfMailIsUnique(employeeDTO.getEmailAddress());
                Employee e = employeeOptional.get();
                e.setFirstName(employeeDTO.getFirstName());
                e.setLastName((employeeDTO.getLastName()));
                e.setEmailAddress(employeeDTO.getEmailAddress());
                employeeRepository.save(e);

                return mapper.employeeToEmployeeDTO(e);
            }
            throw new ResourceNotFoundException();
        }
        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to update the employee (PROJECTMANAGER, DEVELOPER)");
        }
    }

    /**
     * Delete an employee.
     * ADMIN: Able to delete employees.
     * PROJECTMANAGER, DEVELOPER: Not able to delete employees at all.
     * @param id Identifier of the requested employee to delete
     * @throws ForbiddenException Developer or projectmanager tried to delete an employee
     * @throws ResourceNotFoundException Employee with id does not exist
     */
    public void deleteEmployee(Long id) throws ForbiddenException, ResourceNotFoundException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR) {
            Optional<Employee> employeeOptional = employeeRepository.findById(id);
            if (employeeOptional.isPresent()) {
                Employee emp = employeeOptional.get();
                employeeRepository.delete(emp);
            }
            throw new ResourceNotFoundException();
        }

        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException("Missing permission to anonymize the employee (PROJECTMANAGER, DEVELOPER)");
        }
    }

    //Helper Methods

    private Role checkIfRoleExists(String role) throws PreconditionFailedException{
        Optional<Role> oRole = Arrays.stream(Role.values())
                .filter(v -> role.toLowerCase().equals(v.getValue().toLowerCase()))
                .findFirst();

        if(oRole.isPresent()){
            return oRole.get();
        }
        else{

            throw new PreconditionFailedException("Invalid Role id was passed");
        }
    }
    private void checkStrings(String firstName, String lastName, String emailAddress)
            throws PreconditionFailedException{
        if(isNullOrEmpty(firstName)){
            throw new PreconditionFailedException("Invalid first name");
        }
        if(isNullOrEmpty(lastName)){
            throw new PreconditionFailedException("Invalid last name");
        }
        if(!isValid(emailAddress)){
            throw new PreconditionFailedException("Invalid email");
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

    private void checkIfMailIsUnique(String emailAdress) throws PreconditionFailedException {
        if(employeeRepository.existsByEmailAddress(emailAdress))
            throw new PreconditionFailedException("Mail is not unique");
    }

    private List<EmployeeDTO> modelsToDTOs(List<Employee> employees) {
        return employees.stream().map(e -> mapper.employeeToEmployeeDTO(e)).collect(Collectors.toList());
    }

    private Role convertStringToRoleEnum(String sRole) {
        if(sRole != null)
            return Role.valueOf(sRole.toUpperCase());
        return null;
    }
}
