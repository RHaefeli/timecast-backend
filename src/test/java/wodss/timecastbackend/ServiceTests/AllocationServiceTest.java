package wodss.timecastbackend.ServiceTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.ui.ModelMap;
import wodss.timecastbackend.domain.*;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.dto.ContractDTO;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.service.AllocationService;
import wodss.timecastbackend.service.ContractService;
import wodss.timecastbackend.service.EmployeeService;
import wodss.timecastbackend.service.ProjectService;
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
public class AllocationServiceTest {
    @Mock
    AllocationRepository allocationRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    ContractRepository contractRepository;
    @Mock
    ModelMapper mapper;
    @InjectMocks
    AllocationService allocationService;

    Allocation testAllocation1;
    Allocation testAllocation2;
    AllocationDTO testAllocation1DTO;
    Contract testContract1;
    Contract testContract2;
    Project testProject1;
    Project testProject2;

    @Before
    public void setUp(){
        testContract1 = generateMockedContract(1l, new Contract(null, 100, LocalDate.now(), LocalDate.now().plusYears(1L)));
        testContract2 = generateMockedContract(2l, new Contract(null, 50, LocalDate.now().minusYears(1l), LocalDate.now().plusYears(2L)));
        testProject1 = generateMockedProject(1l, new Project("Test Project 1", null, LocalDate.now(), LocalDate.now().plusYears(1L),70));
        testProject2 = generateMockedProject(2l, new Project("Test Project 2", null, LocalDate.now().minusYears(1L), LocalDate.now().plusYears(2L),120));
        testAllocation1 = generateMockedAllocation(1l, new Allocation(testProject1, testContract1,50, testContract1.getStartDate(), testContract1.getEndDate()));
        testAllocation2 = generateMockedAllocation(2l, new Allocation(testProject2, testContract2,50, testContract2.getStartDate().plusMonths(1), testContract2.getEndDate().minusMonths(1)));
        testAllocation1DTO = new AllocationDTO(1l, 1l, 1l, testAllocation1.getPensumPercentage(), testAllocation1.getStartDate(), testAllocation1.getEndDate());

        doMockRepoSetup(allocationRepository, testAllocation1, testAllocation2);
        doMockRepoSetup(projectRepository, testProject1, testProject2);
        doMockRepoSetup(contractRepository, testContract1, testContract2);

    }

    private <T>void doMockRepoSetup(JpaRepository<T, Long> repository, T... entities){
        Mockito.when(repository.findAll()).thenReturn(Arrays.asList(entities));
        long count = 1;
        for(T entity : entities){
            Mockito.when(repository.findById(count++)).thenReturn(Optional.of(entity));
        }
    }

    /**
     * Returns a mocked allocation object. This is necessary to give specific IDs to your allocations for testing purposes.
     * This is mostly relevant for checkIfAllocationIsValid(), since it performs id comparisons.
     * @param id The id the mocked allocation will have
     * @return the mocked allocation
     */
    private Allocation generateMockedAllocation(long id, Allocation allocation){
        Allocation a = Mockito.spy(allocation);
        Mockito.when(a.getId()).thenReturn(id);
        return a;
    }

    private Project generateMockedProject(long id, Project project){
        Project p = Mockito.spy(project);
        Mockito.when(p.getId()).thenReturn(id);
        return p;
    }

    private Contract generateMockedContract(long id, Contract contract){
        Contract c = Mockito.spy(contract);
        Mockito.when(c.getId()).thenReturn(id);
        return c;
    }

    @Test
    public void testCreateAllocation(){
        Allocation createAllocation = new Allocation(testProject1, testContract1, 20, testAllocation1.getStartDate(), testAllocation1.getEndDate());
        AllocationDTO createAllocationDTO = new AllocationDTO(-1L, 1l, 1l, createAllocation.getPensumPercentage(), createAllocation.getStartDate(), createAllocation.getEndDate());

        Mockito.when(allocationRepository.save(Mockito.any(Allocation.class))).thenReturn(createAllocation);
        Mockito.when(mapper.allocationToAllocationDTO(createAllocation)).thenReturn(createAllocationDTO);

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            assertEquals(createAllocationDTO.getId(), create.getId());
        }
        catch (Exception e){
            fail("Failure saving allocation");
            e.printStackTrace();

        }
    }



    @Test
    public void testCreateAllocationWhereContractDoesNotExist(){
        Allocation createAllocation = new Allocation(testProject1, testContract1, 20, testAllocation1.getStartDate(), testAllocation1.getEndDate());
        AllocationDTO createAllocationDTO = new AllocationDTO(-1L, 1l, 999l, createAllocation.getPensumPercentage(), createAllocation.getStartDate(), createAllocation.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: Contract does not exist. Error in checkIfContractExists");
        }
        catch (Exception e){
            assertEquals(RessourceNotFoundException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWhereProjectIsNonExistent(){
        Allocation createAllocation = new Allocation(testProject1, testContract1, 20, testAllocation1.getStartDate(), testAllocation1.getEndDate());
        AllocationDTO createAllocationDTO = new AllocationDTO(-1L, 999l, 1l, createAllocation.getPensumPercentage(), createAllocation.getStartDate(), createAllocation.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: Project does not exist. Error in checkIfProjectExists");
        }
        catch (Exception e){
            assertEquals(RessourceNotFoundException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWherePensumPercentageIsNegative(){
        Allocation createAllocation = new Allocation(testProject1, testContract1, -1, testAllocation1.getStartDate(), testAllocation1.getEndDate());
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, createAllocation.getPensumPercentage(), createAllocation.getStartDate(), createAllocation.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception:");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWhereDatesAreCrossed(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, 20,  testAllocation1.getEndDate(), testAllocation1.getStartDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception:");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWhereFTEPercentageExceedsProjectFTE(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, 21, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: Pensum percentage of contract has been exceeded. Error in checkIfAllocationExceedsFTEOfProject");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWhereStartDateLiesOutsideOfContract(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, 20, testContract1.getStartDate().minusDays(1), testAllocation1.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: The startDate lies outside of the given contract. Error in checkIfAllocationFitsInContract");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWhereEndDateLiesOutsideOfContract(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, 20, testContract1.getStartDate(), testAllocation1.getEndDate().plusDays(1));

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: The startDate lies outside of the given contract. Error in checkIfAllocationFitsInContract");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWherePensumPercentageExceedsContractLimit_StartDateOverlaps(){
        //This test checks whether the overlap of the new allocation's start date with other allocations causes the contract to be exceeded.
        //Test1: the new allocation's start date equals the startdate of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO1 = new AllocationDTO(-1l, 2l, 2l, 1, testAllocation2.getStartDate(), testProject2.getEndDate());
        //Test2: the new allocation's start date lies one day after the start date of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO2 = new AllocationDTO(-1l, 2l, 2l, 1, testAllocation2.getStartDate().plusDays(1), testProject2.getEndDate());
        //Test3: the new allocation's start date lies one day before the end of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO3 = new AllocationDTO(-1l, 2l, 2l, 1, testAllocation2.getEndDate().minusDays(1), testProject2.getEndDate());
        //Test4: the new allocation's start date equals the end date of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO4 = new AllocationDTO(-1l, 2l, 2l, 1, testAllocation2.getEndDate(), testProject2.getEndDate());
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO1);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's start date equals the start date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO2);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's start date lies one day after the start date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO3);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's start date lies one day before the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO4);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's start date equals the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWherePensumPercentageExceedsContractLimit_EndDateOverlaps(){
        //This test checks whether the overlap of the new allocation's end date with other allocations causes the contract to be exceeded.
        //Test1: the new allocation's end date equals the startdate of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO1 = new AllocationDTO(-1l, 2l, 2l, 1, testProject2.getStartDate(), testAllocation2.getStartDate());
        //Test2: the new allocation's end date lies one day after the start date of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO2 = new AllocationDTO(-1l, 2l, 2l, 1, testProject2.getStartDate(), testAllocation2.getStartDate().plusDays(1));
        //Test3: the new allocation's end date lies one day before the end of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO3 = new AllocationDTO(-1l, 2l, 2l, 1, testProject2.getStartDate(), testAllocation2.getEndDate().minusDays(1));
        //Test4: the new allocation's end date equals the end date of another allocation, which causes an exceedance
        AllocationDTO createAllocationDTO4 = new AllocationDTO(-1l, 2l, 2l, 1, testProject2.getStartDate(), testAllocation2.getEndDate());
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO1);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's end date equals the start date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO2);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's end date lies one day after the start date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO3);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's end date lies one day before the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO4);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's end date equals the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    @Test
    public void testCreateAllocationWherePensumPercentageExceedsContractLimit_OtherAllocationContainedWithinDateRange(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 2l, 2l, 1, testAllocation2.getStartDate().minusDays(1), testAllocation2.getEndDate().plusDays(1l));

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(There is another allocation within the date range of the new allocationt, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }

    //TODO: Do finish tests for editAllocation. (Same as create tests)


        /*
    @Test
    public void testCreateAllocation__(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, 20, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception:");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());

        }
    }
    */

    @Test
    public void testFindByID(){
        Mockito.when(mapper.allocationToAllocationDTO(Mockito.any(Allocation.class))).thenReturn(testAllocation1DTO);
        try{
            AllocationDTO found = allocationService.findById((long)1);
            assertEquals(testAllocation1DTO.getId(), found.getId());
        }
        catch(Exception e){
            fail("Failure finding project");
            e.printStackTrace();
        }
    }
    @Test
    public void testFindByIDWhereAllocationIsNonexistent(){
        try{
            AllocationDTO found = allocationService.findById((long)999);
            fail("Should have thrown exception: allocation with this id does not exist. Error in findById()");
        }
        catch(Exception e){
            assertEquals(RessourceNotFoundException.class, e.getClass());
        }
    }



    @Test
    public void testEditAllocation(){
        Allocation editProject1 = generateMockedAllocation(1l, new Allocation(testProject1,testContract1, testAllocation1.getPensumPercentage() - 10, testAllocation1.getStartDate(), testAllocation1.getEndDate()));

        AllocationDTO editProjectDTO = new AllocationDTO(1l, 1l, 1l, editProject1.getPensumPercentage(), editProject1.getStartDate(), editProject1.getEndDate());
        Mockito.when(mapper.allocationToAllocationDTO(Mockito.any(Allocation.class))).thenReturn(editProjectDTO);

        try{
            AllocationDTO edit = allocationService.editAllocation(1l, editProjectDTO);
            assertEquals(editProjectDTO.getId(),edit.getId() );

        }
        catch(Exception ex){
            fail("Failure editing");
        }
    }

}
