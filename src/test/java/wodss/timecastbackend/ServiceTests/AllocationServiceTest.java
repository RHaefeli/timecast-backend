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
    AllocationDTO testAllocation1DTO;
    Contract testContract1;
    Project testProject1;

    @Before
    public void setUp(){
        testContract1 = new Contract(null, 100, LocalDate.now(), LocalDate.now().plusYears(1L));
        testProject1 = new Project("Test Project 1", null, LocalDate.now(), LocalDate.now().plusYears(1L),70);
        testAllocation1 = generateMockedAllocation(1l, testContract1, testProject1,50, testContract1.getStartDate(), testContract1.getEndDate());
        testAllocation1DTO = new AllocationDTO(1l, 1l, 1l, testAllocation1.getPensumPercentage(), testAllocation1.getStartDate(), testAllocation1.getEndDate());
        Mockito.when(allocationRepository.findAll()).thenReturn(Arrays.asList(testAllocation1));
        Mockito.when(allocationRepository.findById((long)1)).thenReturn(Optional.of(testAllocation1));

        Mockito.when(projectRepository.findAll()).thenReturn(Arrays.asList(testProject1));
        Mockito.when(projectRepository.findById((long)1)).thenReturn(Optional.of(testProject1));

        Mockito.when(contractRepository.findAll()).thenReturn(Arrays.asList(testContract1));
        Mockito.when(contractRepository.findById((long)1)).thenReturn(Optional.of(testContract1));
    }

    /**
     * Returns a mocked allocation object. This is necessary to give specific IDs to your allocations for testing purposes.
     * This is mostly relevant for checkIfAllocationIsValid(), as there are a lot of id comparisons happening.
     * @param id The id the mocked allocation will have
     * @param contract the contract the allocation is bound to
     * @param project the project the allocation is bound to
     * @param pensum the pensum percentage of the allocation
     * @param startDate the start date of the allocation
     * @param endDate the end date of the allocation
     * @return
     */
    private Allocation generateMockedAllocation(long id, Contract contract, Project project, int pensum, LocalDate startDate, LocalDate endDate ){
        Allocation a = Mockito.mock(Allocation.class);
        Mockito.when(a.getId()).thenReturn(id);
        Mockito.when(a.getContract()).thenReturn(contract);
        Mockito.when(a.getProject()).thenReturn(project);
        Mockito.when(a.getPensumPercentage()).thenReturn(pensum);
        Mockito.when(a.getStartDate()).thenReturn(startDate);
        Mockito.when(a.getEndDate()).thenReturn(endDate);
        return a;
    }

    @Test
    public void testCreateAllocation(){
        Allocation createAllocation = new Allocation(testProject1, testContract1, 20, testAllocation1.getStartDate(), testAllocation1.getEndDate());
        AllocationDTO createAllocationDTO = new AllocationDTO(1l, 1l, 1l, createAllocation.getPensumPercentage(), createAllocation.getStartDate(), createAllocation.getEndDate());

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
    public void testEditAllocation(){
        Allocation editProject1 = generateMockedAllocation(1l, testContract1, testProject1, testAllocation1.getPensumPercentage() - 10, testAllocation1.getStartDate(), testAllocation1.getEndDate());

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
