package wodss.timecastbackend.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;
import wodss.timecastbackend.domain.*;
import wodss.timecastbackend.dto.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ModelMapper {


    @Mapping(source = "role", target = "role")
    EmployeeDTO employeeToEmployeeDTO(Employee employee);
    default String roleToString(Role r){
        return r.getValue();
    }


    @Mapping(source = "employee", target = "employeeId")
    ContractDTO contractToContractDTO(Contract contract);
    default Long employeeToLong(Employee e) { return e.getId(); };

    @Mapping(source = "projectManager", target = "projectManagerId")
    ProjectDTO projectToProjectDTO(Project p);

    //RoleDTO roleToRoleDTO(Role r);

    @Mapping(source = "project", target = "projectId")
    @Mapping(source = "contract", target = "contractId")
    AllocationDTO allocationToAllocationDTO(Allocation a);
    default Long contractToLong(Contract c){
        return c.getId();
    }
    default Long projectToLong(Project p){
        return p.getId();
    }
}
