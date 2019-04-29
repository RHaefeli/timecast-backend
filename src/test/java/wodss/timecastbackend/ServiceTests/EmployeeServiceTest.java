package wodss.timecastbackend.ServiceTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.security.EmployeeSession;
import wodss.timecastbackend.service.EmployeeService;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.ResourceNotFoundException;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceTest {
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    ModelMapper mapper;
    @Mock
    EmployeeSession employeeSession;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    EmployeeService employeeService;

    EmployeeDTO testEmployee1DTO;
    Employee testEmployee1;
    Employee testEmployee2;
    Employee testEmployee3;


    @Before
    public void setUp(){
        testEmployee1 = new Employee("Ziegler", "Fritz", "fritz.ziegler@mail.ch", Role.DEVELOPER, "");
        testEmployee1DTO = new EmployeeDTO(null, "Ziegler", "Fritz", "fritz.ziegler@mail.ch", "DEVELOPER", true);
        testEmployee2 = new Employee("Mueller", "Hans", "hans.mueller@mail.ch", Role.ADMINISTRATOR, "");
        testEmployee3 = new Employee("Meier", "Peter", "peter.meier@mail.ch", Role.PROJECTMANAGER, "");

        //Mockito.when(employeeRepository.findAll()).thenReturn(Arrays.asList(testEmployee1, testEmployee2, testEmployee3));
        Mockito.when(employeeRepository.findById(1l)).thenReturn(Optional.of(testEmployee1));

        Employee admin = new Employee(
                "Mustermann", "Max", "admin@gmx.ch", Role.ADMINISTRATOR, "12345");

        Mockito.when(employeeSession.getEmployee()).thenReturn(admin);
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("");
    }


    @Test
    public void testCreateEmployee(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(1l, "User", "New", "new.user@mail.ch","DEVELOPER",true );
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(new Employee(createEmployeeDTO1.getLastName(), createEmployeeDTO1.getFirstName(), createEmployeeDTO1.getEmailAddress(), Role.DEVELOPER, ""));
        Mockito.when(mapper.employeeToEmployeeDTO(Mockito.any(Employee.class))).thenReturn(createEmployeeDTO1);
        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole(), "");
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
        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole(), "");
            fail("The employee should not have been created. This role does not exist. Error in checkRoles");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testCreateEmployeeWithoutLasttName(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "", "New", "new.user@mail.ch","DEVELOPER",true );
        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole(), "");
            fail("The employee should not have been created. Last name must not be empty. Error in checkStrings (nullOrEmpty)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testCreateEmployeeWithoutFirstName(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "User", "", "new.user@mail.ch","DEVELOPER",true );
        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole() , "");
            fail("The employee should not have been created. First name must not be empty. Error in checkStrings (nullOrEmpty)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testCreateEmployeeWithInvalidEmail(){
        //Test1: No @ sign
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "User", "New", "new.usermail.ch","DEVELOPER",true );
        //Test2: no . before region identifier
        EmployeeDTO createEmployeeDTO2 = new EmployeeDTO(null, "User", "New", "newuser@mailch","DEVELOPER",true );
        //Test3: no name
        EmployeeDTO createEmployeeDTO3 = new EmployeeDTO(null, "User", "New", "@mail.ch","DEVELOPER",true );
        //test4: no name and no @ sign
        EmployeeDTO createEmployeeDTO4 = new EmployeeDTO(null, "User", "New", "mail.ch","DEVELOPER",true );
        //Test5: No name and no . before region identifier
        EmployeeDTO createEmployeeDTO5 = new EmployeeDTO(null, "User", "New", "@mailch","DEVELOPER",true );
        //Test6: only a string
        EmployeeDTO createEmployeeDTO6 = new EmployeeDTO(null, "User", "New", "mailch","DEVELOPER",true );
        //Test7: empty string
        EmployeeDTO createEmployeeDTO7 = new EmployeeDTO(null, "User", "New", " ","DEVELOPER",true );
        //Test8: email with invalid symbols
        EmployeeDTO createEmployeeDTO8 = new EmployeeDTO(null, "User", "New", "new%user@mail.ch","DEVELOPER",true );

        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO1,createEmployeeDTO1.getRole(), "");
            fail("1 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => There was no @ sign in the email)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO2,createEmployeeDTO1.getRole(), "");
            fail("2 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => no . before region identifier)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO3,createEmployeeDTO1.getRole(), "");
            fail("3 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => no name)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO4,createEmployeeDTO1.getRole(), "");
            fail("4 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => no name and no @ sign)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO5,createEmployeeDTO1.getRole(), "" );
            fail("5 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => No name and no . before region identifier)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO6,createEmployeeDTO1.getRole(), "");
            fail("6 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => only a string with no . or @)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO7,createEmployeeDTO1.getRole(), "");
            fail("7 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => Empty string)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.createEmployee(createEmployeeDTO8,createEmployeeDTO1.getRole(), "" );
            fail("8 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => email with invalid symbols)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testCreateEmployeeWhereEmailAlreadyExists(){
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(null, "User", "New", testEmployee1.getEmailAddress(),"DEVELOPER",true );
        Mockito.when(employeeRepository.existsByEmailAddress(testEmployee1.getEmailAddress())).thenReturn(true);
        try{
            employeeService.createEmployee(createEmployeeDTO1, createEmployeeDTO1.getRole(), "12345");
            fail("Should have thrown exception: Email already exists in repository. Error in checkIfEmailIsUnique");
        }
        catch(Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
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
        try{
            EmployeeDTO found = employeeService.getEmployee((long)5);
            fail("User should not be found.");
        }
        catch(Exception e){
            assert(e.getClass() == ResourceNotFoundException.class);
        }
    }

    @Test
    public void testEditEmployee(){
        //Edit should still work even if roles in editEmployeeAreInvalid, since roles are ignored during edit
        Employee editEmployee1 = new Employee("Ziegler", "Moritz", "fritz.ziegler@mail.ch", null, "");
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "Ziegler", "Moritz", "fritz.ziegler@mail.ch", "", true);
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(mapper.employeeToEmployeeDTO(Mockito.any(Employee.class))).thenReturn(editEmployeeDTO1);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployeeDTO1, (long)1);
            assertEquals(editEmployee1.getFirstName(), edit.getFirstName());
        }
        catch(Exception ex){
            fail("Failure editing");
        }
    }

    @Test
    public void testEditEmployeeWithoutLastName(){
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "", "Moritz", "fritz.ziegler@mail.ch", "InvalidRole", true);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployeeDTO1, (long)1);
            fail("User should not have been edited. Last name must be filled out (Error in check Strings)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testEditEmployeeWithoutFirstName(){
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "", "Moritz", "fritz.ziegler@mail.ch", "InvalidRole", true);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployeeDTO1, (long)1);
            fail("User should not have been edited. First name must be filled out (Error in check Strings)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }

    @Test
    public void testEditEmployeeWithInvalidEmail(){
        //Test1: No @ sign
        EmployeeDTO createEmployeeDTO1 = new EmployeeDTO(1l, "User", "New", "new.usermail.ch","DEVELOPER",true );
        //Test2: no . before region identifier
        EmployeeDTO createEmployeeDTO2 = new EmployeeDTO(1l, "User", "New", "newuser@mailch","DEVELOPER",true );
        //Test3: no name
        EmployeeDTO createEmployeeDTO3 = new EmployeeDTO(1l, "User", "New", "@mail.ch","DEVELOPER",true );
        //test4: no name and no @ sign
        EmployeeDTO createEmployeeDTO4 = new EmployeeDTO(1l, "User", "New", "mail.ch","DEVELOPER",true );
        //Test5: No name and no . before region identifier
        EmployeeDTO createEmployeeDTO5 = new EmployeeDTO(1l, "User", "New", "@mailch","DEVELOPER",true );
        //Test6: only a string
        EmployeeDTO createEmployeeDTO6 = new EmployeeDTO(1l, "User", "New", "mailch","DEVELOPER",true );
        //Test7: empty string
        EmployeeDTO createEmployeeDTO7 = new EmployeeDTO(1l, "User", "New", " ","DEVELOPER",true );
        //Test8: email with invalid symbols
        EmployeeDTO createEmployeeDTO8 = new EmployeeDTO(1l, "User", "New", "new%user@mail.ch","DEVELOPER",true );

        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO1, createEmployeeDTO1.getId());
            fail("1 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => There was no @ sign in the email)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO2, createEmployeeDTO2.getId() );
            fail("2 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => no . before region identifier)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO3,createEmployeeDTO3.getId() );
            fail("3 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => no name)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO4,createEmployeeDTO4.getId() );
            fail("4 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => no name and no @ sign)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO5,createEmployeeDTO5.getId() );
            fail("5 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => No name and no . before region identifier)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO6,createEmployeeDTO6.getId() );
            fail("6 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => only a string with no . or @)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO7,createEmployeeDTO7.getId() );
            fail("7 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => Empty string)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }

        try{
            EmployeeDTO created = employeeService.updateEmployee(createEmployeeDTO8,createEmployeeDTO8.getId() );
            fail("8 - The employee should not have been created. The email must be vaild. Error in checkStrings (isValid regex expression failed => email with invalid symbols)");
        }
        catch(Exception e){
            assert(e.getClass() == PreconditionFailedException.class);
        }
    }



    @Test
    public void testEditEmployeeWithNonexistentUser(){
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)999, "Fritz", "Moritz", "fritz.ziegler@mail.ch", "InvalidRole", true);
        try{
            EmployeeDTO edit = employeeService.updateEmployee(editEmployeeDTO1, editEmployeeDTO1.getId());
            fail("User should not have been found");
        }
        catch(Exception e){
            assert(e.getClass() == ResourceNotFoundException.class);
        }
    }

    @Test
    public void testDeleteWithNonexistentUser(){
        try{
            employeeService.deleteEmployee(999l);
            fail("Should have thrown an exception");
        }
        catch(Exception e){
            assert(e.getClass() == ResourceNotFoundException.class);
        }
    }






}
