package wodss.timecastbackend.web;

import com.sun.media.jfxmedia.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.domain.User;
import wodss.timecastbackend.dto.UserDTO;
import wodss.timecastbackend.persistence.RoleRepository;
import wodss.timecastbackend.persistence.UserRepository;
import wodss.timecastbackend.util.ModelMapper;

import javax.xml.ws.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper mapper; //This error is due to IntelliJ not being capable of intepreting the @Spring

    @GetMapping
    public @ResponseBody List<UserDTO> getAllUsers() {
        List<UserDTO> userDtos = userRepository.findAll().stream().map(u -> mapper.userToUserDTO(u)).collect(Collectors.toList());
        return userDtos;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDto){
        //TODO: Validation

        Role role = null;
        Optional<Role> oRole = roleRepository.findById(userDto.getRoleId());
        if(oRole.isPresent()) {
            role = oRole.get();
        } else {
            return new ResponseEntity<UserDTO>(HttpStatus.PRECONDITION_FAILED);
        }

        User user = new User(userDto.getLastName(), userDto.getFirstName(), role, userDto.getEmployment());
        user = userRepository.save(user);
        userDto.setId(user.getId());
        return new ResponseEntity<UserDTO>(userDto, HttpStatus.OK);
    }

}
