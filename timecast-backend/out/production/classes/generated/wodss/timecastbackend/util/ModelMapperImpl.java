package wodss.timecastbackend.util;

import javax.annotation.Generated;
import org.springframework.stereotype.Component;
import wodss.timecastbackend.domain.Allocation;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.dto.ContractDTO;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.dto.RoleDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2019-03-29T17:53:03+0100",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 1.8.0_144 (Oracle Corporation)"
)
@Component
public class ModelMapperImpl implements ModelMapper {

    @Override
    public EmployeeDTO employeeToEmployeeDTO(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setRoleId( roleToLong( employee.getRole() ) );
        employeeDTO.setId( employee.getId() );
        employeeDTO.setLastName( employee.getLastName() );
        employeeDTO.setFirstName( employee.getFirstName() );
        employeeDTO.setEmailAddress( employee.getEmailAddress() );
        employeeDTO.setActive( employee.isActive() );

        return employeeDTO;
    }

    @Override
    public ContractDTO contractToContractDTO(Contract contract) {
        if ( contract == null ) {
            return null;
        }

        ContractDTO contractDTO = new ContractDTO();

        contractDTO.setEmployeeId( employeeToLong( contract.getEmployee() ) );
        contractDTO.setId( contract.getId() );
        contractDTO.setPensumPercentage( contract.getPensumPercentage() );
        contractDTO.setStartDate( contract.getStartDate() );
        contractDTO.setEndDate( contract.getEndDate() );

        return contractDTO;
    }

    @Override
    public ProjectDTO projectToProjectDTO(Project p) {
        if ( p == null ) {
            return null;
        }

        ProjectDTO projectDTO = new ProjectDTO();

        projectDTO.setProjectManagerId( employeeToLong( p.getProjectManager() ) );
        projectDTO.setId( p.getId() );
        projectDTO.setName( p.getName() );
        projectDTO.setStartDate( p.getStartDate() );
        projectDTO.setEndDate( p.getEndDate() );
        projectDTO.setFtePercentage( p.getFtePercentage() );

        return projectDTO;
    }

    @Override
    public RoleDTO roleToRoleDTO(Role r) {
        if ( r == null ) {
            return null;
        }

        RoleDTO roleDTO = new RoleDTO();

        roleDTO.setId( r.getId() );
        roleDTO.setName( r.getName() );
        roleDTO.setDescription( r.getDescription() );

        return roleDTO;
    }

    @Override
    public AllocationDTO allocationToAllocationDTO(Allocation a) {
        if ( a == null ) {
            return null;
        }

        AllocationDTO allocationDTO = new AllocationDTO();

        allocationDTO.setContractId( contractToLong( a.getContract() ) );
        allocationDTO.setProjectId( projectToLong( a.getProject() ) );
        allocationDTO.setId( a.getId() );
        allocationDTO.setPensumPercentage( a.getPensumPercentage() );
        allocationDTO.setStartDate( a.getStartDate() );
        allocationDTO.setEndDate( a.getEndDate() );

        return allocationDTO;
    }
}
