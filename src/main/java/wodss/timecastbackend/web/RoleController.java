package wodss.timecastbackend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.RoleDTO;
import wodss.timecastbackend.persistence.RoleRepository;
import wodss.timecastbackend.util.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper mapper;

    @GetMapping
    public @ResponseBody List<RoleDTO> getAllRoles() {
        List<RoleDTO> roleDtos = roleRepository.findAll().stream().map(r -> mapper.roleToRoleDTO(r)).collect(Collectors.toList());
        return roleDtos;
    }

    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDto) {
        //TODO: Validation

        Role role = new Role(roleDto.getName(), roleDto.getDescription());
        role = roleRepository.save(role);
        roleDto.setId(role.getId());
        return new ResponseEntity<RoleDTO>(roleDto, HttpStatus.OK);
    }
}
