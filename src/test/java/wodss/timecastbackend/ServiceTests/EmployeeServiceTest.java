package wodss.timecastbackend.ServiceTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.service.EmployeeService;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceTest {
    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    ModelMapper mapper;



    @InjectMocks
    EmployeeService employeeService;

    EmployeeDTO testEmployee1DTO;
    Employee testEmployee1;
    Employee testEmployee2;
    Employee testEmployee3;


    @Before
    public void setUp(){
        testEmployee1 = new Employee("Ziegler", "Fritz", "fritz.ziegler@mail.ch", Role.DEVELOPER);
        testEmployee1DTO = new EmployeeDTO(null, "Ziegler", "Fritz", "fritz.ziegler@mail.ch", "DEVELOPER", true);
        testEmployee2 = new Employee("Mueller", "Hans", "hans.mueller@mail.ch", Role.ADMINISTRATOR);
        testEmployee3 = new Employee("Meier", "Peter", "peter.meier@mail.ch", Role.PROJECTMANAGER);

        Mockito.when(employeeRepository.findAll()).thenReturn(Arrays.asList(testEmployee1, testEmployee2, testEmployee3));
    }


    @Test
    public void testCreateEmployee(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "User", "New", "new.user@mail.ch","DEVELOPER",true );
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(new Employee(createEmployeeDTO1.getLastName(), createEmployeeDTO1.getFirstName(), createEmployeeDTO1.getEmailAddress(), Role.DEVELOPER));
        try{
            Employee created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole() );
            assert(createEmployeeDTO1.getFirstName().equals(created.getFirstName()));
        }
        catch(Exception e){
            fail("Failure saving employee");
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateEmployeeWithInvalidRole(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "User", "New", "new.user@mail.ch","InvalidRole",true );
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(new Employee(createEmployeeDTO1.getLastName(), createEmployeeDTO1.getFirstName(), createEmployeeDTO1.getEmailAddress(), Role.DEVELOPER));
        try{
            Employee created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole() );
            fail("The employee should not have been created. This role does not exist. Error in checkRoles");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testCreateEmployeeWithoutLasttName(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "", "New", "new.user@mail.ch","DEVELOPER",true );
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(new Employee(createEmployeeDTO1.getLastName(), createEmployeeDTO1.getFirstName(), createEmployeeDTO1.getEmailAddress(), Role.DEVELOPER));
        try{
            Employee created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole() );
            fail("The employee should not have been created. Last name must not be empty. Error in checkStrings (nullOrEmpty)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testCreateEmployeeWithoutFirstName(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "User", "", "new.user@mail.ch","DEVELOPER",true );
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(new Employee(createEmployeeDTO1.getLastName(), createEmployeeDTO1.getFirstName(), createEmployeeDTO1.getEmailAddress(), Role.DEVELOPER));
        try{
            Employee created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole() );
            fail("The employee should not have been created. First name must not be empty. Error in checkStrings (nullOrEmpty)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testCreateEmployeeWithInvalidEmail(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "User", "", "new.usermail.ch","DEVELOPER",true );
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(new Employee(createEmployeeDTO1.getLastName(), createEmployeeDTO1.getFirstName(), createEmployeeDTO1.getEmailAddress(), Role.DEVELOPER));
        try{
            Employee created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole() );
            fail("The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testFindByID(){
        Optional<Employee> empOptional = Optional.of(testEmployee1);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(empOptional);
        Mockito.when(mapper.employeeToEmployeeDTO(empOptional.get())).thenReturn(testEmployee1DTO);
        try{
            EmployeeDTO found = employeeService.getEmployee((long)1);
            assert(testEmployee1.getFirstName().equals(found.getFirstName()));
        }
        catch(Exception e){
            fail("Failure finding employee");
            e.printStackTrace();
        }
    }

    @Test
    public void testFindByIDWithNonexistentEmployee(){
        Optional<Employee> empOptional = Optional.of(testEmployee1);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(empOptional);
        Mockito.when(mapper.employeeToEmployeeDTO(empOptional.get())).thenReturn(testEmployee1DTO);
        try{
            EmployeeDTO found = employeeService.getEmployee((long)5);
            fail("User should not be found.");
        }
        catch(Exception e){
            assert(e.getClass() == RessourceNotFoundException.class);
        }
    }

    @Test
    public void testEditEmployee(){
        //Edit should still work even if roles in editEmployeeAreInvalid, since roles are ignored during edit
        Employee editEmployee1 = new Employee("Ziegler", "Moritz", "fritz.ziegler@mail.ch", null);
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "Ziegler", "Moritz", "fritz.ziegler@mail.ch", "", true);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(mapper.employeeToEmployeeDTO(Mockito.any(Employee.class))).thenReturn(editEmployeeDTO1);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployee1, (long)1);
            assertEquals(editEmployee1.getFirstName(), edit.getFirstName());
        }
        catch(Exception ex){
            fail("Failure editing");
        }
    }

    @Test
    public void testEditEmployeeWithoutLastName(){
        Employee editEmployee1 = new Employee(" ", "Moritz", "fritz.ziegler@mail.ch", null);
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "", "Moritz", "fritz.ziegler@mail.ch", "InvalidRole", true);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(mapper.employeeToEmployeeDTO(Mockito.any(Employee.class))).thenReturn(editEmployeeDTO1);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployee1, (long)1);
            fail("User should not have been edited. Last name must be filled out (Error in check Strings)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testEditEmployeeWithoutFirstName(){
        Employee editEmployee1 = new Employee("ziegler", "", "fritz.ziegler@mail.ch", null);
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "", "Moritz", "fritz.ziegler@mail.ch", "InvalidRole", true);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(mapper.employeeToEmployeeDTO(Mockito.any(Employee.class))).thenReturn(editEmployeeDTO1);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployee1, (long)1);
            fail("User should not have been edited. First name must be filled out (Error in check Strings)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testEditEmployeeWithInvalidEmail(){
        Employee editEmployee1 = new Employee("ziegler", "Fritz", "fritz.zieglermail.ch", null);
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "Fritz", "Moritz", "fritz.zieglermail.ch", "InvalidRole", true);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(mapper.employeeToEmployeeDTO(Mockito.any(Employee.class))).thenReturn(editEmployeeDTO1);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployee1, (long)1);
            fail("User should not have been edited. Email was not vaild. Error in check strings (isValid regex)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testEditEmployeeWithNonexistentUser(){
        Employee editEmployee1 = new Employee("ziegler", "Fritz", "fritz.ziegler@mail.ch", null);
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "Fritz", "Moritz", "fritz.ziegler@mail.ch", "InvalidRole", true);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(mapper.employeeToEmployeeDTO(Mockito.any(Employee.class))).thenReturn(editEmployeeDTO1);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployee1, (long)999);
            fail("User should not have been found");
        }
        catch(Exception e){
            assert(e.getClass() == RessourceNotFoundException.class);
        }
    }

    @Test
    public void testDeleteWithNonexistentUser(){
        try{
            employeeService.deleteEmployee(999l);
            fail("Should have thrown an exception");
        }
        catch(Exception e){
            assert(e.getClass() == RessourceNotFoundException.class);
        }
    }






}
