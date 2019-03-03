package wodss.timecastbackend.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import wodss.timecastbackend.domain.Assignment;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.domain.User;
import wodss.timecastbackend.dto.AssignmentDTO;
import wodss.timecastbackend.dto.ProjectDTO;
import wodss.timecastbackend.dto.RoleDTO;
import wodss.timecastbackend.dto.UserDTO;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ModelMapper {

    @Mapping(source = "role", target = "roleId")
    UserDTO userToUserDTO(User user);

    default Long roleToLong(Role r){
        return r.getId();
    }

    ProjectDTO projectToProjectDTO(Project p);
    RoleDTO roleToRoleDTO(Role r);

    @Mapping(source = "project", target = "projectId")
    @Mapping(source = "user", target = "userId")
    AssignmentDTO assignmentToAssignmentDTO(Assignment a);

    default Long userToLong(User u){
        return u.getId();
    }

    default Long projectToLong(Project p){
        return p.getId();
    }
}
