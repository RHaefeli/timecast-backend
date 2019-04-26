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
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.ContractDTO;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
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
public class ProjectServiceTest {
    @Mock
    ProjectRepository projectRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    ModelMapper mapper;
    @InjectMocks
    ProjectService projectService;

    Project testProject1;
    ProjectDTO testProject1DTO;
    Employee testEmployee1;
    Employee testEmployee2;


    @Before
    public void setUp(){
        testEmployee1 = new Employee("Ziegler", "Fritz", "fritz.ziegler@mail.ch", Role.PROJECTMANAGER);
        testEmployee2 = new Employee("Mueller", "Max", "max.mueller@mail.ch", Role.DEVELOPER);
        testProject1 = new Project("Test Project 1", testEmployee1, LocalDate.now(), LocalDate.now().plusYears(1L), 1000);
        testProject1DTO = new ProjectDTO(1L, 1L, testProject1.getName(), testProject1.getStartDate(), testProject1.getEndDate(), testProject1.getFtePercentage());

        Mockito.when(employeeRepository.findAll()).thenReturn(Arrays.asList(testEmployee1, testEmployee2));
        Mockito.when(employeeRepository.findById((long)1)).thenReturn(Optional.of(testEmployee1));
        Mockito.when(employeeRepository.findById((long)2)).thenReturn(Optional.of(testEmployee2));

        Mockito.when(projectRepository.findAll()).thenReturn(Arrays.asList(testProject1));
        Mockito.when(projectRepository.findById((long)1)).thenReturn(Optional.of(testProject1));
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
    public void testFindByID(){
        Optional<Project> projectOptional = Optional.of(testProject1);
        Mockito.when(projectRepository.findById((long)1)).thenReturn(projectOptional);
        Mockito.when(mapper.projectToProjectDTO(projectOptional.get())).thenReturn(testProject1DTO);
        try{
            ProjectDTO found = projectService.getProject((long)1);
            assertEquals(testProject1DTO.getId(), found.getId());
        }
        catch(Exception e){
            fail("Failure finding project");
            e.printStackTrace();
        }
    }

    @Test
    public void testEditProject(){
        Project editProject1 = new Project("New project name", testEmployee1, testProject1.getStartDate().plusDays(1l), testProject1.getEndDate().plusDays(1l), 2000);
        ProjectDTO editProjectDTO = new ProjectDTO(1L, 1L, editProject1.getName(), editProject1.getStartDate(), editProject1.getEndDate(), editProject1.getFtePercentage());
        Mockito.when(mapper.projectToProjectDTO(Mockito.any(Project.class))).thenReturn(editProjectDTO);
        try{
            ProjectDTO edit = projectService.updateProject(editProject1, 1L);

            assertEquals(1L,edit.getId() );
            assertEquals(editProject1.getName(), edit.getName() );
        }
        catch(Exception ex){
            fail("Failure editing");
        }
    }

}
