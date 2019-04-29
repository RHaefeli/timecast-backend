package wodss.timecastbackend.ServiceTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import wodss.timecastbackend.domain.*;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.service.AllocationService;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
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
    Allocation testAllocation3;
    Allocation testAllocation4;
    AllocationDTO testAllocation1DTO;
    Contract testContract1;
    Contract testContract2;
    Contract testContract3;
    Project testProject1;
    Project testProject2;
    Project testProject3;

    @Before
    public void setUp(){
        //testContract1 is the standard contract used for most tests
        testContract1 = generateMockedContract(1l, new Contract(null, 100, LocalDate.now(), LocalDate.now().plusYears(1L)));
        //TestContract2 is a contract with a lower pensum Percentage is lower than the one of testContract1. It is used to test cases where the allocations pensumPercentage would exceed the contract's limit, but not the one of the project.
        testContract2 = generateMockedContract(2l, new Contract(null, 50, LocalDate.now().minusYears(1l), LocalDate.now().plusYears(2L)));


        //TestProject1 is used for most of the standard tests.
        testProject1 = generateMockedProject(1l, new Project("Test Project 1", null, LocalDate.now(), LocalDate.now().plusYears(1L),70));
        //TestProject2 is a project with a higher FTE limit (higher than testContract2. It is used for test cased where the Project's FTE limit could hold a certain allocation but the contract cannot.
        testProject2 = generateMockedProject(2l, new Project("Test Project 2", null, LocalDate.now().minusYears(1L), LocalDate.now().plusYears(2L),120));

        testAllocation1 = generateMockedAllocation(1l, new Allocation(testProject1, testContract1,50, testContract1.getStartDate(), testContract1.getEndDate()));
        testAllocation2 = generateMockedAllocation(2l, new Allocation(testProject2, testContract2,50, testContract2.getStartDate().plusMonths(1), testContract2.getEndDate().minusMonths(1)));

        //the following testObjects are used for the test cases where Start/EndDate overlaps with another allocation which causes an exceedence.
        //TestContract3 is 2 years long. Normally, each allocation (allocation 3 and 4) are each 1 year long and do not interfere with one another.
        //Note that testAllocation4 ends 1 day before testContract3 runs out. This way, testAllocation3 can be edited, so that it contains testAllocation4 within its date range.
        testContract3 = generateMockedContract(3, new Contract(null, 50, LocalDate.now(), LocalDate.now().plusYears(2)));
        testProject3 = generateMockedProject(3, new Project("Test Project 3", null, testContract3.getStartDate(), testContract3.getEndDate(), 100));
        testAllocation3 = generateMockedAllocation(3, new Allocation(testProject3, testContract3, 50, testContract3.getStartDate(), testContract3.getStartDate().plusYears(1)));
        testAllocation4 = generateMockedAllocation(4, new Allocation(testProject3, testContract3, 50, testAllocation3.getStartDate().plusDays(1), testContract3.getEndDate().minusDays(1)));


        testAllocation1DTO = new AllocationDTO(1l, 1l, 1l, testAllocation1.getPensumPercentage(), testAllocation1.getStartDate(), testAllocation1.getEndDate());

        doMockRepoSetup(allocationRepository, testAllocation1, testAllocation2, testAllocation3, testAllocation4);
        doMockRepoSetup(projectRepository, testProject1, testProject2, testProject3);
        doMockRepoSetup(contractRepository, testContract1, testContract2, testContract3);

    }

    /**
     * Sets up a mocked repository by preparing the findAll() and the findById methods.
     * @param repository the mock repository which needs to be set up
     * @param entities the entities which should be contained within the repository
     * @param <T> The type contained within the repository
     */
    private <T>void doMockRepoSetup(JpaRepository<T, Long> repository, T... entities){
        Mockito.when(repository.findAll()).thenReturn(Arrays.asList(entities));
        //The id's start with 1 and are then gradually counted upwards in the order that they were passed via the parameters.
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

    /**
     * Generates a mock object which spies on the given project and mocks the getID method.
     * This is necessary, since certain checks perform id comparisons and the id cannot be set from outside.
     * @param id the id the mock should return upon calling getId()
     * @param project the project which should be mocked
     * @return A mocked project object which returns the given id upon calling getID()
     */
    private Project generateMockedProject(long id, Project project){
        Project p = Mockito.spy(project);
        Mockito.when(p.getId()).thenReturn(id);
        return p;
    }

    /**
     * Generates a mock object which spies on the given contract and mocks the getID method.
     * This is necessary, since certain checks perform id comparisons and the id cannot be set from outside.
     * @param id the id the mock should return upon calling getId()
     * @param contract the contract which should be mocked
     * @return A mocked contract object which returns the given id upon calling getID()
     */
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
            assertEquals(ResourceNotFoundException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTNOTFOUND, e.getMessage());

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
            assertEquals(ResourceNotFoundException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_PROJECTNOTFOUND, e.getMessage());
        }
    }

    @Test
    public void testCreateAllocationWherePensumPercentageIsNegative(){
        Allocation createAllocation = new Allocation(testProject1, testContract1, -1, testAllocation1.getStartDate(), testAllocation1.getEndDate());
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, createAllocation.getPensumPercentage(), createAllocation.getStartDate(), createAllocation.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: Pensum must not be negative. Error in checkIfPensumPercentageIsPositive");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_PENSUMNEGATIVE, e.getMessage());

        }
    }

    @Test
    public void testCreateAllocationWhereDatesAreCrossed(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 1l, 20,  testAllocation1.getEndDate(), testAllocation1.getStartDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: StartDate must lie before the endDate. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_DATESCROSSED, e.getMessage());
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
            assertEquals(AllocationService.ERR_MSG_PROJECT_FTE_EXCEEDED, e.getMessage());
        }
    }

    @Test
    public void testCreateAllocationWhereStartDateLiesOutsideOfContract(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 2l, 1l, 20, testContract1.getStartDate().minusDays(1), testAllocation1.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: The startDate lies outside of the given contract. Error in checkIfAllocationFitsInContract");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_STARTDATEOUTSIDEOFCONTRACT, e.getMessage());
        }
    }

    @Test
    public void testCreateAllocationWhereStartDateLiesOutsideOfProject(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 2l, 20, testProject1.getStartDate().minusDays(1), testProject1.getEndDate());

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: The startDate lies outside of the given project. Error in checkIfAllocationFitsInProject");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_STARTDATEOUTSIDEOFPROJECT, e.getMessage());

        }
    }

    @Test
    public void testCreateAllocationWhereEndDateLiesOutsideOfContract(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 2l, 1l, 20, testContract1.getStartDate(), testAllocation1.getEndDate().plusDays(1));

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: The startDate lies outside of the given contract. Error in checkIfAllocationFitsInContract");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_ENDDATEOUTSIDEOFCONTRACT, e.getMessage());
        }
    }

    @Test
    public void testCreateAllocationWhereEndDateLiesOutsideOfProject(){
        AllocationDTO createAllocationDTO = new AllocationDTO(-1l, 1l, 2l, 20, testProject1.getStartDate(), testProject1.getEndDate().plusDays(1));

        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO);
            fail("Should have thrown an exception: The endDate lies outside of the given project. Error in checkIfAllocationFitsInProject");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_ENDDATEOUTSIDEOFPROJECT, e.getMessage());

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
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO2);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's start date lies one day after the start date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO3);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's start date lies one day before the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO4);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's start date equals the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
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
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO2);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's end date lies one day after the start date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO3);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's end date lies one day before the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
        }
        try{
            AllocationDTO create = allocationService.createAllocation(createAllocationDTO4);
            fail("Should have thrown an exception: The pensum percentage of the allocation exceeds the limit of the contract.(The new contract's end date equals the end date of another contract, which causes an exceedance. Error in checkIfAllocationExeedsContractLimit");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
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
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, e.getMessage());
        }
    }


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
            fail("Failure finding Allocation");
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
            assertEquals(ResourceNotFoundException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_ALLOCATIONNOTFOUND, e.getMessage());
        }
    }



    @Test
    public void testEditAllocation(){
        Allocation editAllocation = generateMockedAllocation(1l, new Allocation(testProject1,testContract1, testAllocation1.getPensumPercentage() - 10, testAllocation1.getStartDate(), testAllocation1.getEndDate()));

        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 1l, editAllocation.getPensumPercentage(), editAllocation.getStartDate(), editAllocation.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            assertEquals(editAllocationDTO.getId(),edit.getId() );

        }
        catch(Exception ex){
            fail("Failure editing");
        }
    }

    @Test
    public void testEditAllocationWhereAllocationIsNonExistent(){
        AllocationDTO editAllocationDTO = new AllocationDTO(999l, 1l, 1l, 10, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: Allocation does not exist. Error in checkIfAllocationExists.");

        }
        catch(Exception ex){
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_ALLOCATIONNOTFOUND, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereContractDoesNotExist(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 999l, 10, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: Contract does not exist. Error in checkIfContractExists");

        }
        catch(Exception ex){
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTNOTFOUND, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereProjectIsNonExistent(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 999l, 1l, 10, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: Project does not exist. Error in checkIfProjectExists");

        }
        catch(Exception ex){
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_PROJECTNOTFOUND, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWherePensumPercentageIsNegative(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 1l, -1, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: Pensum percentage must not be negative. Error in checkIfPensumPercentageIsPositive");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_PENSUMNEGATIVE, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereDatesAreCrossed(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 1l, 10, testAllocation1.getEndDate(), testAllocation1.getStartDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: StartDate must lie before endDate. Error in checkDates");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_DATESCROSSED, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereDatesAreEqual(){
        //Allocations can only be 1 day long, so this should be ok.
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 1l, 10, testAllocation1.getStartDate(), testAllocation1.getStartDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);

        }
        catch(Exception ex){
            fail("Error editing allocation. Dates are allowed to be equal. Error in checkDates");
        }
    }

    @Test
    public void testEditAllocationFTEPercentageExceedsProjectFTE(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 1l, 71, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: The project FTE limit of testProject1 is set to 70, but the edit would have lead to an exceedance. Error in checkIfAllocationExceedsFTEOfProject");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_PROJECT_FTE_EXCEEDED, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereStartDateLiesOutsideOfContract(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 2l, 1l, 10, testContract1.getStartDate().minusDays(1), testContract1.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: Start date is outside of given contract. Error in checkIfAllocationFitsInContract");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_STARTDATEOUTSIDEOFCONTRACT, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereEndDateLiesOutsideOfContract(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 2l, 1l, 10, testContract1.getStartDate(), testContract1.getEndDate().plusDays(1));

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: EndDate is outside of given contract. Error in checkIfAllocationFitInContract");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_ENDDATEOUTSIDEOFCONTRACT, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereStartDateLiesOutsideOfProject(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 2l, 10, testProject1.getStartDate().minusDays(1), testProject1.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: Start date is outside of given Project. Error in checkIfAllocationFitInProject");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_STARTDATEOUTSIDEOFPROJECT, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWhereEndDateLiesOutsideOfProject(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 2l, 10, testProject1.getStartDate(), testProject1.getEndDate().plusDays(1));

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: EndDate is outside of given project. Error in checkIfAllocationFitInProject");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_ENDDATEOUTSIDEOFPROJECT, ex.getMessage());
        }
    }


    @Test
    public void testEditAllocationWherePensumPercentageExceedsContractLimit_StartDateOverlaps(){
        //For this testCase, testAllocation4 is edited so that it overlaps with testAllocation3, which causes an exceedence.

        //TestCase1: StartDate of TA4 equals startDate of TA3
        AllocationDTO editAllocationDTO1 = new AllocationDTO(4l, 3l, 3l, 50, testAllocation3.getStartDate(), testAllocation4.getEndDate());
        //TestCase1: StartDate of TA4 equals startDate of TA3
        AllocationDTO editAllocationDTO2 = new AllocationDTO(4l, 3l, 3l, 50, testAllocation3.getStartDate().plusDays(1), testAllocation4.getEndDate());
        //TestCase1: StartDate of TA4 equals startDate of TA3
        AllocationDTO editAllocationDTO3 = new AllocationDTO(4l, 3l, 3l, 50, testAllocation3.getEndDate().minusDays(1), testAllocation4.getEndDate());
        //TestCase1: StartDate of TA4 equals startDate of TA3
        AllocationDTO editAllocationDTO4 = new AllocationDTO(4l, 3l, 3l, 50, testAllocation3.getEndDate(), testAllocation4.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO1.getId(), editAllocationDTO1);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited start date equals start date of TA3, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO2.getId(), editAllocationDTO2);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited start date lies one day after start date of TA3, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO3.getId(), editAllocationDTO3);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited start date lies one day before end date of TA3, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO4.getId(), editAllocationDTO4);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited start date equals end date of TA3, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWherePensumPercentageExceedsContractLimit_EndDateOverlaps(){
        //For this testCase, testAllocation3 is edited so that it overlaps with testAllocation4, which causes an exceedence.

        //TestCase1: EndDate of TA3 equals startDate of TA4
        AllocationDTO editAllocationDTO1 = new AllocationDTO(3l, 3l, 3l, 50, testAllocation3.getStartDate(), testAllocation4.getStartDate());
        //TestCase1: EndDate of TA3 equals startDate of TA4
        AllocationDTO editAllocationDTO2 = new AllocationDTO(3l, 3l, 3l, 50, testAllocation3.getStartDate(), testAllocation4.getStartDate().plusDays(1));
        //TestCase1: EndDate of TA3 equals startDate of TA4
        AllocationDTO editAllocationDTO3 = new AllocationDTO(3l, 3l, 3l, 50, testAllocation3.getStartDate(), testAllocation4.getEndDate().minusDays(1));
        //TestCase1: EndDate of TA3 equals startDate of TA4
        AllocationDTO editAllocationDTO4 = new AllocationDTO(3l, 3l, 3l, 50, testAllocation3.getStartDate(), testAllocation4.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO1.getId(), editAllocationDTO1);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited end date equals start date of TA4, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO2.getId(), editAllocationDTO2);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited end date lies one day after start date of TA4, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO3.getId(), editAllocationDTO3);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited end date lies one day before end date of TA4, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO4.getId(), editAllocationDTO4);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Edited end date equals end date of TA4, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());
        }
    }

    @Test
    public void testEditAllocationWherePensumPercentageExceedsContractLimit_OtherAllocationContainedWithinDateRange(){
        //For this test case, testAllocation3 will be edited so that testAllocation4 is contained within TA3's date range.
        //(TA4 runs out 1 day before the contract ends, meaning that the end date of TA3 can simply be set to testContract3's end date.)
        AllocationDTO editAllocationDTO = new AllocationDTO(3l, 3l, 3l, 50, testAllocation3.getStartDate(), testContract3.getEndDate());

        try{
            AllocationDTO edit = allocationService.updateAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception: Pensum percentage of allocation exceeds contract limit. (Another allocation is contained within the edited allocation's date range, which causes exceedence. Error in checkIfAllocationExceedsContractLimit");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
            assertEquals(AllocationService.ERR_MSG_CONTRACTLIMITEXCEEDED, ex.getMessage());

        }
    }


    /*
    @Test
    public void testEditAllocation____(){
        AllocationDTO editAllocationDTO = new AllocationDTO(1l, 1l, 1l, 10, testAllocation1.getStartDate(), testAllocation1.getEndDate());

        try{
            AllocationDTO edit = allocationService.editAllocation(editAllocationDTO.getId(), editAllocationDTO);
            fail("Should have thrown exception:");

        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }
    */

    @Test
    public void testDeleteAllocationWhereAllocationIsNonExistent(){
        try{
            allocationService.deleteAllocation(999L);
            fail("Should have thrown exception: Allocation does not exist.");
        }
        catch(Exception e){
            assertEquals(ResourceNotFoundException.class, e.getClass());
            assertEquals(AllocationService.ERR_MSG_ALLOCATIONNOTFOUND, e.getMessage());
        }
    }
}
