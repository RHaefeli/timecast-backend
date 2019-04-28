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
    //Two contracts are created to check boundaries for start and end date.
    Contract testContract1;
    Contract testContract2;

    Employee testEmployee1;

    @Before
    public void setUp(){
        testEmployee1 = generateMockEmployee(new Employee("Ziegler", "Fritz", "fritz.ziegler@mail.ch", Role.DEVELOPER), 1l);
        testContract1 = generateMockContract(new Contract(testEmployee1, 100, LocalDate.now(), LocalDate.now().plusYears(1)), 1l);
        testContract2 = generateMockContract(new Contract(testEmployee1, 100, testContract1.getEndDate().plusYears(1L), testContract1.getEndDate().plusYears(2L)), 2l);
        testContractDTO1 = new ContractDTO(1L, 1L, testContract1.getPensumPercentage(), testContract1.getStartDate(), testContract1.getEndDate());

        //Mockito.when(employeeRepository.findAll()).thenReturn(Arrays.asList(testEmployee1));
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));

        Mockito.when(contractRepository.findAll()).thenReturn(Arrays.asList(testContract1, testContract2));
        Mockito.when(contractRepository.findById((long)1)).thenReturn(Optional.of(testContract1));
        Mockito.when(contractRepository.findById((long)2)).thenReturn(Optional.of(testContract2));
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
    //CheckDates boundary tests
    @Test
    public void testCreateContractWhereStartDateOverlapsWithOtherContract(){
        //BoundaryTest1: StartDate equals StartDate of another contract (in this case, testContract1)
        ContractDTO createContractDTO1 = new ContractDTO(-1L, 1L, 100, testContract1.getStartDate().plusDays(1L),testContract2.getStartDate().minusDays(1));
        //BoundaryTest2: StartDate lies 1 day after startdate of another contract (testContract1)
        ContractDTO createContractDTO2 = new ContractDTO(-1L, 1L, 100, testContract1.getStartDate().plusDays(1L),testContract2.getStartDate().minusDays(1));
        //BoundaryTest3: StartDate lies 1 day before enddate of another contract (testContract1)
        ContractDTO createContractDTO3 = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().minusDays(1L),testContract2.getStartDate().minusDays(1));
        //BoundaryTest4: StartDate equals enddate of another contract (testContract1)
        ContractDTO createContractDTO4 = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate(),testContract2.getStartDate().minusDays(1));

        try{
            ContractDTO create = contractService.createContract(createContractDTO1);
            fail("Should have thrown exception: StartDate equals startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.createContract(createContractDTO2);
            fail("Should have thrown exception: StartDate lies 1 day after startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.createContract(createContractDTO3);
            fail("Should have thrown exception: StartDate lies 1 day before enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.createContract(createContractDTO4);
            fail("Should have thrown exception: StartDate equals enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }

    @Test
    public void testCreateContractWhereEndDateOverlapsWithOtherContract(){
        //BoundaryTest1: EndDate equals StartDate of another contract (in this case, testContract2)
        ContractDTO createContractDTO1 = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getStartDate());
        //BoundaryTest2: EndDate lies 1 day after startdate of another contract (testContract2)
        ContractDTO createContractDTO2 = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getStartDate().plusDays(1));
        //BoundaryTest3: EndDate lies 1 day before enddate of another contract (testContract2)
        ContractDTO createContractDTO3 = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getEndDate().minusDays(1));
        //BoundaryTest4: EndDate equals EndDate of another contract (testContract2)
        ContractDTO createContractDTO4 = new ContractDTO(-1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getEndDate());

        try{
            ContractDTO create = contractService.createContract(createContractDTO1);
            fail("Should have thrown exception: EndDate equals startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.createContract(createContractDTO2);
            fail("Should have thrown exception: Enddate lies 1 day after startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.createContract(createContractDTO3);
            fail("Should have thrown exception: Enddate lies 1 day before enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.createContract(createContractDTO4);
            fail("Should have thrown exception: EndDate equals enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }

    @Test
    public void testCreateContractWhereAnotherContractIsContainedWithinStartAndEndDate(){
        //BoundryTest1: StartDate is one day before StartDate of another contract and endDate is 1 day after endDate of another contract (testContract1=
        ContractDTO createContractDTO1 = new ContractDTO(-1L, 1L, 100, testContract1.getStartDate().minusDays(1L),testContract1.getEndDate().plusDays(1));

        try{
            ContractDTO create = contractService.createContract(createContractDTO1);
            fail("Should have thrown exception: There is another contract contained within the new contract's timeframe. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }

    @Test
    public void testCreateContractWithNonExistentEmployee(){
        ContractDTO createContractDTO = new ContractDTO(-1L, 999L, 100, testContract1.getEndDate().plusDays(1L), testContract1.getEndDate().plusYears(1l));

        try{
            ContractDTO create = contractService.createContract(createContractDTO);
            fail("Should have thrown exception: Employee does not exist. Error in checkEmployee");
        }
        catch (Exception e){
            assertEquals(RessourceNotFoundException.class, e.getClass());
        }
    }

    //PensumPercentage BoundryTests
    @Test
    public void testCreateContractWithInvalidPensumPercentage(){

        ContractDTO createContractDTO1 = new ContractDTO(-1L, 1L, -1, testContract1.getEndDate().plusDays(1L), testContract1.getEndDate().plusYears(1l));
        ContractDTO createContractDTO2 = new ContractDTO(-1L, 1L, 101, testContract1.getEndDate().plusDays(1L), testContract1.getEndDate().plusYears(1l));

        try{
            ContractDTO create = contractService.createContract(createContractDTO1);
            fail("Should have thrown exception: Pensum must not be negative. Error is checkPensumPercentage");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.createContract(createContractDTO2);
            fail("Should have thrown exception: Pensum must not be over 100");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

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
    public void testFindByIDWithNonExistentContract(){
        try{
            ContractDTO found = contractService.findById(999L);
            fail("Should have thrown exception: No contract with such an id existent. Error in FindByID");
        }
        catch(Exception e){
            assertEquals(RessourceNotFoundException.class, e.getClass());
        }
    }

    @Test
    public void testEditContract(){
        ContractDTO editContractDTO1 = new ContractDTO(1L, 1L, testContract1.getPensumPercentage()-10, testContract1.getStartDate().plusDays(2),testContract1.getEndDate().minusDays(4));

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

    /*
    @Test
    public void testEditContract____(){
        ContractDTO editContractDTO1 = new ContractDTO(1L, 1L, testContract1.getPensumPercentage()-10, testContract1.getStartDate().plusDays(2),testContract1.getEndDate().minusDays(4));

        try{
            ContractDTO edit = contractService.editContract((long)1, editContractDTO1);
            fail("Should have thrown exception:");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }
    */


    @Test
    public void testEditContractWithNonexistentID(){
        ContractDTO editContractDTO1 = new ContractDTO(1L, 1L, testContract1.getPensumPercentage()-10, testContract1.getStartDate().plusDays(2),testContract1.getEndDate().minusDays(4));

        try{
            ContractDTO edit = contractService.editContract((long)999, editContractDTO1);
            fail("Should have thrown exception: contract id does not exist. Error in checkIfContractExists");
        }
        catch(Exception ex){
            assertEquals(RessourceNotFoundException.class, ex.getClass());
        }
    }

    @Test
    public void testEditContractWhereEmployeeIsNonExistent(){
        ContractDTO editContractDTO1 = new ContractDTO(1L, 999L, 100, testContract1.getStartDate().plusDays(2),testContract1.getEndDate().minusDays(4));

        try{
            ContractDTO edit = contractService.editContract((long)1, editContractDTO1);
            fail("Should have thrown exception: Employee does not exist. Error in checkIfEmployeeExists.");
        }
        catch(Exception ex){
            assertEquals(RessourceNotFoundException.class, ex.getClass());
        }
    }

    @Test
    public void testEditContractWherePensumPercentageIsLessThanZero(){
        ContractDTO editContractDTO1 = new ContractDTO(1L, 1L, -1, testContract1.getStartDate().plusDays(2),testContract1.getEndDate().minusDays(4));

        try{
            ContractDTO edit = contractService.editContract((long)1, editContractDTO1);
            fail("Should have thrown exception: Pensum percentage cannot be less than 0. Error in checkPensumPercentage.");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }

    @Test
    public void testEditContractWherePensumPercentageIsOver100(){
        ContractDTO editContractDTO1 = new ContractDTO(1L, 1L, 101, testContract1.getStartDate().plusDays(2),testContract1.getEndDate().minusDays(4));

        try{
            ContractDTO edit = contractService.editContract((long)1, editContractDTO1);
            fail("Should have thrown exception: Pensum percentage cannot be over 100. Error in checkPensumPercentage.");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }

    @Test
    public void testEditContractWhereDatesAreCrossed(){
        ContractDTO editContractDTO1 = new ContractDTO(1L, 1L, testContract1.getPensumPercentage()-10, testContract1.getEndDate().minusDays(4), testContract1.getStartDate().plusDays(2));

        try{
            ContractDTO edit = contractService.editContract((long)1, editContractDTO1);
            fail("Should have thrown exception: StartDate must be before EndDate. Error in checkDates");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }

    @Test
    public void testEditContractWhereStartDateOverlapsWithOtherContract(){
        //For the boundary tests of StartDate overlaps, testContract2 will be edited, so that it overlaps with testContract1.
        //BoundaryTest1: StartDate equals StartDate of another contract (in this case, testContract1)
        ContractDTO createContractDTO1 = new ContractDTO(2L, 1L, 100, testContract1.getStartDate().plusDays(1L),testContract2.getStartDate().minusDays(1));
        //BoundaryTest2: StartDate lies 1 day after startdate of another contract (testContract1)
        ContractDTO createContractDTO2 = new ContractDTO(2L, 1L, 100, testContract1.getStartDate().plusDays(1L),testContract2.getStartDate().minusDays(1));
        //BoundaryTest3: StartDate lies 1 day before enddate of another contract (testContract1)
        ContractDTO createContractDTO3 = new ContractDTO(2L, 1L, 100, testContract1.getEndDate().minusDays(1L),testContract2.getStartDate().minusDays(1));
        //BoundaryTest4: StartDate equals enddate of another contract (testContract1)
        ContractDTO createContractDTO4 = new ContractDTO(2L, 1L, 100, testContract1.getEndDate(),testContract2.getStartDate().minusDays(1));

        try{
            ContractDTO create = contractService.editContract(createContractDTO1.getId(), createContractDTO1);
            fail("Should have thrown exception: StartDate equals startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.editContract(createContractDTO2.getId(), createContractDTO2);
            fail("Should have thrown exception: StartDate lies 1 day after startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.editContract(createContractDTO3.getId(), createContractDTO3);
            fail("Should have thrown exception: StartDate lies 1 day before enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.editContract(createContractDTO4.getId(), createContractDTO4);
            fail("Should have thrown exception: StartDate equals enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }

    @Test
    public void testEditContractWhereEndDateOverlapsWithOtherContract(){
        //For the boundary tests of end date overlaps, testContract1 will be edited so that it overlaps with testContract2
        //BoundaryTest1: EndDate equals StartDate of another contract (in this case, testContract2)
        ContractDTO createContractDTO1 = new ContractDTO(1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getStartDate());
        //BoundaryTest2: EndDate lies 1 day after startdate of another contract (testContract2)
        ContractDTO createContractDTO2 = new ContractDTO(1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getStartDate().plusDays(1));
        //BoundaryTest3: EndDate lies 1 day before enddate of another contract (testContract2)
        ContractDTO createContractDTO3 = new ContractDTO(1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getEndDate().minusDays(1));
        //BoundaryTest4: EndDate equals EndDate of another contract (testContract2)
        ContractDTO createContractDTO4 = new ContractDTO(1L, 1L, 100, testContract1.getEndDate().plusDays(1L),testContract2.getEndDate());

        try{
            ContractDTO create = contractService.editContract(createContractDTO1.getId(), createContractDTO1);
            fail("Should have thrown exception: EndDate equals startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.editContract(createContractDTO2.getId(), createContractDTO2);
            fail("Should have thrown exception: Enddate lies 1 day after startdate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.editContract(createContractDTO3.getId(), createContractDTO3);
            fail("Should have thrown exception: Enddate lies 1 day before enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            ContractDTO create = contractService.editContract(createContractDTO4.getId(), createContractDTO4);
            fail("Should have thrown exception: EndDate equals enddate of another contract. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }

    @Test
    public void testEditContractWhereAnotherContractIsContainedWithinStartAndEndDate(){
        //For this test, testContract1 will be edited so that testContract2 will be contained within the date range of testContract1.
        //BoundryTest1: StartDate is one day before StartDate of another contract and endDate is 1 day after endDate of another contract (testContract1=
        ContractDTO createContractDTO1 = new ContractDTO(1L, 1L, 100, testContract2.getStartDate().minusDays(1L),testContract2.getEndDate().plusDays(1));

        try{
            ContractDTO create = contractService.editContract(createContractDTO1.getId(), createContractDTO1);
            fail("Should have thrown exception: There is another contract contained within the new contract's time frame. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
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
