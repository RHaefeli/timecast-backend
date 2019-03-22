package wodss.timecastbackend.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import wodss.timecastbackend.domain.*;
import wodss.timecastbackend.dto.*;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ModelMapper {

    @Mapping(source = "role", target = "roleId")
    EmployeeDTO employeeToEmployeeDTO(Employee employee);
    default Long roleToLong(Role r){
        return r.getId();
    }

    ContractDTO contractToContractDTO(Contract contract);
    ProjectDTO projectToProjectDTO(Project p);
    RoleDTO roleToRoleDTO(Role r);

    @Mapping(source = "project", target = "projectId")
    @Mapping(source = "contract", target = "contractId")
    AllocationDTO allocationToAllocationDTO(Allocation a);
    default Long employeeToLong(Employee e){
        return e.getId();
    }
    default Long projectToLong(Project p){
        return p.getId();
    }
}
