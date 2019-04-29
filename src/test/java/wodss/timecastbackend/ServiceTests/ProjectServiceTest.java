package wodss.timecastbackend.ServiceTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import wodss.timecastbackend.domain.*;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.security.EmployeeSession;
import wodss.timecastbackend.service.ProjectService;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {
    @Mock
    ProjectRepository projectRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    AllocationRepository allocationRepository;
    @Mock
    ModelMapper mapper;
    @Mock
    EmployeeSession employeeSession;
    @InjectMocks
    ProjectService projectService;

    Project testProject1;
    ProjectDTO testProject1DTO;
    Employee testEmployee1;
    Employee testEmployee2;
    Contract testContract1;
    Contract testContract2;
    Allocation testAllocation1;
    Allocation testAllocation2;


    @Before
    public void setUp(){
        testEmployee1 = new Employee("Ziegler", "Fritz", "fritz.ziegler@mail.ch", Role.PROJECTMANAGER, null);
        testEmployee2 = new Employee("Mueller", "Max", "max.mueller@mail.ch", Role.DEVELOPER, null);
        testProject1 = new Project("Test Project 1", testEmployee1, LocalDate.now(), LocalDate.now().plusYears(1L), 100);
        testProject1DTO = new ProjectDTO(1L, 1L, testProject1.getName(), testProject1.getStartDate(), testProject1.getEndDate(), testProject1.getFtePercentage());

        testContract1 = new Contract(testEmployee1, 100, testProject1.getStartDate(), testProject1.getEndDate());
        testContract2 = new Contract(testEmployee2, 100, testProject1.getStartDate(), testProject1.getEndDate());

        testAllocation1 = new Allocation(testProject1, testContract1, 50, testProject1.getStartDate(), testProject1.getEndDate() );
        testAllocation2 = new Allocation(testProject1, testContract2, 50, testProject1.getStartDate(), testProject1.getEndDate() );


        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(employeeRepository.findById((long)2)).thenReturn(Optional.of(testEmployee2));

       // Mockito.when(projectRepository.findAll()).thenReturn(Arrays.asList(testProject1));
        Mockito.when(projectRepository.findById((long)1)).thenReturn(Optional.of(testProject1));

        Mockito.when(allocationRepository.findAll()).thenReturn(Arrays.asList(testAllocation1, testAllocation2));

        Employee admin = new Employee(
                "Mustermann", "Max", "admin@gmx.ch", Role.ADMINISTRATOR, "12345");

        Mockito.when(employeeSession.getEmployee()).thenReturn(admin);
    }

    /**
     * Generates a mock object which spies on the given project and mocks the getID method.
     * This is necessary, since certain checks in the ProjectService perform id comparisons and the id cannot be set from outside.
     * @param id the id the mock should return upon calling getId()
     * @param p the project which should be mocked
     * @return A mocked project object which returns the given id upon calling getID()
     */
    private Project generateMockProject(long id, Project p){
        Project project = Mockito.spy(p);
        Mockito.when(project.getId()).thenReturn(id);
        return project;
    }

    @Test
    public void testCreateProject(){
        Project createProject = new Project("Create Project", testEmployee1, LocalDate.now(), LocalDate.now().plusYears(1L), 1000);
        ProjectDTO createProjectDTO = new ProjectDTO(createProject.getId(), 1L, createProject.getName(), createProject.getStartDate(), createProject.getEndDate(), createProject.getFtePercentage());
        Mockito.when(projectRepository.save(Mockito.any(Project.class))).thenReturn(createProject);
        try{
            Project create = projectService.createProject(createProjectDTO);
            assertEquals(createProject.getName(), create.getName());
            assertEquals(createProject.getId(), create.getId());
        }
        catch (Exception e){
            fail("Failure saving project");
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateProjectWithInvalidEmployeeID(){
        Project createProject = new Project("Create Project", testEmployee1, LocalDate.now(), LocalDate.now().plusYears(1L), 1000);
        ProjectDTO createProjectDTO = new ProjectDTO(createProject.getId(), 999L, createProject.getName(), createProject.getStartDate(), createProject.getEndDate(), createProject.getFtePercentage());
        try{
            Project create = projectService.createProject(createProjectDTO);
            fail("Should have thrown an exception. Employee does not exist. Error in checkEmployee");
        }
        catch (Exception e){
            assertEquals(ResourceNotFoundException.class, e.getClass());
        }
    }

    @Test
    public void testCreateProjectWithEmployeeWhoIsNotAManager(){
        Project createProject = new Project("Create Project", testEmployee2, LocalDate.now(), LocalDate.now().plusYears(1L), 1000);
        ProjectDTO createProjectDTO = new ProjectDTO(createProject.getId(), 2, createProject.getName(), createProject.getStartDate(), createProject.getEndDate(), createProject.getFtePercentage());
        try{
            Project create = projectService.createProject(createProjectDTO);
            fail("Should have thrown an exception. Employee is not a project manager. Error in checkEmployee");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }

    @Test
    public void testCreateProjectWithCrossedDates(){
        Project createProject = new Project("Create Project", testEmployee2, LocalDate.now(), LocalDate.now().minusYears(1L), 1000);
        ProjectDTO createProjectDTO = new ProjectDTO(createProject.getId(), 1, createProject.getName(), createProject.getStartDate(), createProject.getEndDate(), createProject.getFtePercentage());
        try{
            Project create = projectService.createProject(createProjectDTO);
            fail("Should have thrown an exception. Start date should always be before or equal to end date. Error in checkDates");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }
    @Test
    public void testCreateProjectWithNegativeFTEs(){
        Project createProject = new Project("Create Project", testEmployee2, LocalDate.now(), LocalDate.now().minusYears(1L), -1000);
        ProjectDTO createProjectDTO = new ProjectDTO(createProject.getId(), 1, createProject.getName(), createProject.getStartDate(), createProject.getEndDate(), createProject.getFtePercentage());
        try{
            Project create = projectService.createProject(createProjectDTO);
            fail("Should have thrown an exception. FTEs must not be negative. Error in checkIfFTEIsPositive");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }

    @Test
    public void testCreateProjectWithoutName(){
        Project createProject = new Project("", testEmployee2, LocalDate.now(), LocalDate.now().minusYears(1L), -1000);
        ProjectDTO createProjectDTO = new ProjectDTO(createProject.getId(), 1, createProject.getName(), createProject.getStartDate(), createProject.getEndDate(), createProject.getFtePercentage());
        try{
            Project create = projectService.createProject(createProjectDTO);
            fail("Should have thrown an exception. Project should have a name. Error in checkString");
        }
        catch (Exception e){
            assertEquals(PreconditionFailedException.class, e.getClass());
        }
    }


    @Test
    public void testFindByID(){
        Optional<Project> projectOptional = Optional.of(testProject1);
        Mockito.when(projectRepository.findById((long)1)).thenReturn(projectOptional);
        Mockito.when(mapper.projectToProjectDTO(projectOptional.get())).thenReturn(testProject1DTO);
        try{
            ProjectDTO found = projectService.findById((long)1);
            assertEquals(testProject1DTO.getId(), found.getId());
        }
        catch(Exception e){
            fail("Failure finding project");
            e.printStackTrace();
        }
    }

    @Test
    public void testFindByIDWithNonexistantProject(){
        try{
            ProjectDTO found = projectService.findById(999L);
            fail("Should have thrown exception: Project does not exist.");
        }
        catch(Exception e){
            assertEquals(ResourceNotFoundException.class, e.getClass());

        }
    }


    @Test
    public void testEditProject(){
        Project editProject1 = new Project("New project name", testEmployee1, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().plusDays(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        Mockito.when(mapper.projectToProjectDTO(Mockito.any(Project.class))).thenReturn(editProjectDTO);
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);

            assertEquals(1L,edit.getId() );
            assertEquals(editProject1.getName(), edit.getName() );
        }
        catch(Exception ex){
            fail("Failure editing");
        }
    }

    @Test
    public void testEditProjectWithNonExistentID(){
        Project editProject1 = new Project("new Project Name", testEmployee1, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().plusDays(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 999L);
            fail("Should have thrown exception: Project must have name");
        }
        catch(Exception ex){
            assertEquals(ResourceNotFoundException.class, ex.getClass());
        }
    }

    @Test
    public void testEditProjectWithoutName(){
        Project editProject1 = new Project("", testEmployee1, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().plusDays(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);
            fail("Should have thrown exception: Project must have name. Error in checkStrings");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }
    @Test
    public void testEditProjectWithNonExistentEmployee(){
        Project editProject1 = new Project("new Project Name", testEmployee1, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().plusDays(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 999L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);
            fail("Should have thrown exception: Employee does not exist. Error in CheckEmployee");
        }
        catch(Exception ex){
            assertEquals(ResourceNotFoundException.class, ex.getClass());
        }
    }

    @Test
    public void testEditProjectWithEmployeeWhoIsNotProjectManager(){
        Project editProject1 = new Project("new Project Name", testEmployee2, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().plusDays(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 2L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);
            fail("Should have thrown exception: Employee must be project manager. Error in checkEmployee");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }

    @Test
    public void testEditProjectWithCrossedDates(){
        Project editProject1 = new Project("new Project Name", testEmployee1, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().minusYears(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);
            fail("Should have thrown exception: Dates are crossed. Error in checkDates");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }

    @Test
    public void testEditProjectWhereFTEIsTooSmallForFTESum(){
        Project editProject1 = new Project("New project name", testEmployee1, testProject1.getStartDate(), testProject1.getEndDate(), 50);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());

        testAllocation1.setProject(generateMockProject(1l, testProject1));
        testAllocation2.setProject(generateMockProject(1l, testProject1));
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);
            fail("Should have thrown exception: The sum of FTEs should be smaller than the new FTE limit foe the project. Error in CheckFTE");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }

    @Test
    public void testEditProjectWithWhereFTEIsNegative(){
        Project editProject1 = new Project("new Project Name", testEmployee1, testProject1.getStartDate(), testProject1.getEndDate(), -1);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);
            fail("Should have thrown exception: Dates are crossed. Error in checkDates");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }

    @Test
    public void testDeleteProjectWithNonexistentProject(){
        try{
            projectService.deleteProject(999L);
            fail("Should have thrown exception: No such project available");
        }
        catch(Exception e){
            assertEquals(ResourceNotFoundException.class, e.getClass());
        }
    }

    /*
    @Test
    public void testEditProjectWith___(){
        Project editProject1 = new Project("New project name", testEmployee1, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().plusDays(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        Mockito.when(mapper.projectToProjectDTO(Mockito.any(Project.class))).thenReturn(editProjectDTO);
        try{
            ProjectDTO edit = projectService.updateProject(editProjectDTO, 1L);
            fail("Should have thrown exception: ");
        }
        catch(Exception ex){
            assertEquals(PreconditionFailedException.class, ex.getClass());
        }
    }
    */

}
