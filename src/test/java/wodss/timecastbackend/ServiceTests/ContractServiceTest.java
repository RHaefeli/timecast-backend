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
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.ContractDTO;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.service.ContractService;
import wodss.timecastbackend.service.EmployeeService;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class ContractServiceTest {

    @Mock
    ContractRepository contractRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    ModelMapper mapper;

    @InjectMocks
    ContractService contractService;

    ContractDTO testContractDTO1;
    //Two contract are created to check boundries for start and end date.
    Contract testContract1;
    Contract testContract2;
    Employee testEmployee1;

    @Before
    public void setUp(){
        testEmployee1 = generateMockEmployee(new Employee("Ziegler", "Fritz", "fritz.ziegler@mail.ch", Role.DEVELOPER), 1l);
        testContract1 = generateMockContract(new Contract(testEmployee1, 100, LocalDate.now(), LocalDate.now().plusYears(1)), 1l);
        testContract2 = generateMockContract(new Contract(testEmployee1, 100, testContract1.getEndDate().plusYears(1L), testContract1.getEndDate().plusYears(2L)), 1l);
        testContractDTO1 = new ContractDTO(1L, 1L, testContract1.getPensumPercentage(), testContract1.getStartDate(), testContract1.getEndDate());

        Mockito.when(employeeRepository.findAll()).thenReturn(Arrays.asList(testEmployee1));
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));

        Mockito.when(contractRepository.findAll()).thenReturn(Arrays.asList(testContract1));
        Mockito.when(contractRepository.findById((long)1)).thenReturn(Optional.of(testContract1));
    }

    private Employee generateMockEmployee(Employee e, long id){
        Employee mock = Mockito.spy(e);
        Mockito.when(mock.getId()).thenReturn(id);
        return mock;
    }
    private Contract generateMockContract(Contract c, long id){
        Contract mock = Mockito.spy(c);
        Mockito.when(mock.getId()).thenReturn(id);
        return mock;
    }

    @Test
    public void testCreateContract(){
        ContractDTO createContractDTO = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().plusDays(1L), testContract2.getStartDate().minusDays(1));
        Contract createContract = new Contract(testEmployee1, createContractDTO.getPensumPercentage(), createContractDTO.getStartDate(), createContractDTO.getEndDate());
        Mockito.when(contractRepository.save(Mockito.any(Contract.class))).thenReturn(createContract);
        Mockito.when(mapper.contractToContractDTO(createContract)).thenReturn(createContractDTO);
        try{
            ContractDTO create = contractService.createContract(createContractDTO);
            assertEquals(createContractDTO.getEmployeeId(), create.getEmployeeId());
        }
        catch (Exception e){
            fail("Failure saving contract");
            e.printStackTrace();

        }
    }

    /*
    @Test
    public void testCreateContractWith___(){
        ContractDTO createContractDTO = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().plusDays(1L), testContract1.getEndDate().plusYears(1l));
        Contract createContract = new Contract(testEmployee1, createContractDTO.getPensumPercentage(), createContractDTO.getStartDate(), createContractDTO.getEndDate());

        try{
            ContractDTO create = contractService.createContract(createContractDTO);
            fail("Should have thrown exception:");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }
    */

    @Test
    public void testCreateContractWhereFuckYou(){
        ContractDTO createContractDTO = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().plusDays(1L), testContract1.getEndDate().plusYears(1l));
        Contract createContract = new Contract(testEmployee1, createContractDTO.getPensumPercentage(), createContractDTO.getStartDate(), createContractDTO.getEndDate());

        try{
            ContractDTO create = contractService.createContract(createContractDTO);
            fail("Should have thrown exception:");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testFindByID(){
        Optional<Contract> empOptional = Optional.of(testContract1);
        Mockito.when(contractRepository.findById((long)1)).thenReturn(empOptional);
        Mockito.when(mapper.contractToContractDTO(empOptional.get())).thenReturn(testContractDTO1);
        try{
            ContractDTO found = contractService.findById((long)1);
            assertEquals(testContractDTO1.getId(), found.getId());
        }
        catch(Exception e){
            fail("Failure finding contract");
            e.printStackTrace();
        }
    }

    @Test
    public void testEditContract(){
        Contract editContract1 = new Contract(testContract1.getEmployee(), testContract1.getPensumPercentage()-10, testContract1.getStartDate().plusDays(2), testContract1.getEndDate().minusDays(4));
        ContractDTO editContractDTO1 = new ContractDTO(1L, 1L, editContract1.getPensumPercentage(), editContract1.getStartDate(), editContract1.getEndDate());

        try{
            ContractDTO edit = contractService.editContract((long)1, editContractDTO1);

            assertEquals(editContractDTO1.getId(), edit.getId());
            assertEquals(editContractDTO1.getEmployeeId(), edit.getEmployeeId());
            assertEquals(editContractDTO1.getPensumPercentage(), edit.getPensumPercentage());
            assertEquals(editContractDTO1.getStartDate(), edit.getStartDate());
            assertEquals(editContractDTO1.getEndDate(), edit.getEndDate());
        }
        catch(Exception ex){
            fail("Failure editing");
        }
    }

    @Test
    public void testDeleteContractWithNonexistentContract(){
        try{
            contractService.deleteContract(999L);
            fail("Should have thrown exception: Contract does not exists");
        }
        catch(Exception e){
            assertEquals(RessourceNotFoundException.class, e.getClass());
        }
    }


}
