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

import java.util.ArrayList;
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
    public void testCreateUser(){
        EmployeeDTO testEmployeeDTO1 = new EmployeeDTO(null, "User", "New", "new.user@mail.ch","DEVELOPER",true );
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(new Employee(testEmployeeDTO1.getLastName(), testEmployeeDTO1.getFirstName(), testEmployeeDTO1.getEmailAddress(), Role.DEVELOPER));
        try{
            Employee created = employeeService.createEmployee(testEmployeeDTO1,testEmployeeDTO1.getRole() );
            assert(testEmployeeDTO1.getFirstName().equals(created.getFirstName()));
        }
        catch(Exception e){
            fail("Failure saving employee");
            e.printStackTrace();
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
    public void testEditEmployee(){
        Employee editEmployee1 = new Employee("Ziegler", "Moritz", "fritz.ziegler@mail.ch", Role.DEVELOPER);
        EmployeeDTO editEmployeeDTO1 = new EmployeeDTO((long)1, "Ziegler", "Moritz", "fritz.ziegler@mail.ch", "DEVELOPER", true);
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
    //TODO: Grenzfälle von Employee testen bei create UND edit.



}
