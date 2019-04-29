package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.security.EmployeeSession;
import wodss.timecastbackend.util.ForbiddenException;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final ProjectRepository projectRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeSession employeeSession;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper mapper, PasswordEncoder passwordEncoder,
                           AllocationRepository allocationRepository, ProjectRepository projectRepository,
                           EmployeeSession employeeSession) {
        this.employeeRepository = employeeRepository;
        this.allocationRepository = allocationRepository;
        this.projectRepository = projectRepository;
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

    /**
     * Returns the employee with a given id in form of a DTO.
     * @param id the ID of the employee
     * @return the found employee in form of a DTO
     * @throws Exception Throws a RessourceNotFoundException if the employee was not found.
     */
    public EmployeeDTO getEmployee(Long id) throws Exception{

        //ALL
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if(employeeOptional.isPresent()){
            return mapper.employeeToEmployeeDTO(employeeOptional.get());
        }
        throw new ResourceNotFoundException("Employee was not found.");
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
    @Transactional
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
        checkIfMailIsUnique(employeeDTO.getEmailAddress(), "");
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
    @Transactional
    public EmployeeDTO updateEmployee(EmployeeDTO employeeDTO, Long id) throws ForbiddenException,
            ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR) {
            Employee employee = checkEmployee(employeeDTO.getId());
            checkIfMailIsUnique(employeeDTO.getEmailAddress(), employeeOptional.get().getEmailAddress());
            checkStrings(employeeDTO.getFirstName(), employeeDTO.getLastName(), employeeDTO.getEmailAddress());
            employee.setFirstName(employeeDTO.getFirstName());
            employee.setLastName((employeeDTO.getLastName()));
            employee.setEmailAddress(employeeDTO.getEmailAddress());
            employeeRepository.save(employee);
            return mapper.employeeToEmployeeDTO(employee);
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
    @Transactional
    public void deleteEmployee(Long id) throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR) {
            Employee employee = checkEmployee(id);
            checkIfEmployeeIsAnActiveProjectManager(employee);
            checkIfEmployeeHasNoAllocations(employee);
            employeeRepository.delete(employee);
        }

        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException("Missing permission to anonymize the employee (PROJECTMANAGER, DEVELOPER)");
        }
    }


    /**
     * Check if the employee exists.
     * @param id Identifier of the employee to check
     * @return Employee object
     * @throws ResourceNotFoundException Employee was not found
     */
    private Employee checkEmployee(long id) throws ResourceNotFoundException {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            return employeeOptional.get();
        }
        throw new ResourceNotFoundException("Employee was not found");
    }

    /**
     * Checks if the passed string can be translated to a valid role. (ADMINISTRATOR, DEVELOPER, PROJECTMANAGER)
     * If role was not found, a PreconditionFailedException is thrown.
     * @param role the role string
     * @return The found role, if available.
     * @throws Exception Throws a PreconditionFailedException if role is not found.
     */
    private Role checkIfRoleExists(String role) throws PreconditionFailedException{

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
    private void checkIfMailIsUnique(String emailAddress, String previousEmail) throws PreconditionFailedException {
        if(!emailAddress.equals(previousEmail) && employeeRepository.existsByEmailAddress(emailAddress))
            throw new PreconditionFailedException("Mail is not unique");
    }

    /**
     * Takes a list of Employee objects and converts them to DTOs
     * @param employees the list of employees that needs to be converted into DTOs
     * @return a list which contains the given employees, converted into DTOs.
     */
    private List<EmployeeDTO> modelsToDTOs(List<Employee> employees) {
        return employees.stream().map(mapper::employeeToEmployeeDTO).collect(Collectors.toList());
    }

    private Role convertStringToRoleEnum(String sRole) {
        if(sRole != null)
            return Role.valueOf(sRole.toUpperCase());
        return null;
    }

    /**
     * Checks if the employee has no allocations.
     * @param employee Employee object to check the allocations
     * @throws PreconditionFailedException Employee object is referred indirectly by allocations.
     */
    private void checkIfEmployeeHasNoAllocations(Employee employee) throws PreconditionFailedException {
        if(allocationRepository.existsByEmployeeId(employee.getId()))
            throw new PreconditionFailedException("An allocation for employee exists");
    }

    /**
     * Checks if a given employee is a project manager
     * @param e the employee that needs to be checked
     * @return The employee if they are a project manager.
     * @throws Exception Throws a PreconditionFailedException if the employee is not a project manager.
     */
    private void checkIfEmployeeIsAnActiveProjectManager(Employee e) throws PreconditionFailedException {
        if(e.getRole() == Role.PROJECTMANAGER && employeeRepository.existsById(e.getId()))
            throw new PreconditionFailedException("The employee is an active project manager");
    }
}
