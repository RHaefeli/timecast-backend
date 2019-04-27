package wodss.timecastbackend.web;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.security.JwtUtil;
import wodss.timecastbackend.service.AuthenticationService;
import wodss.timecastbackend.util.RessourceNotFoundException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/token")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    private EmployeeRepository employeeRepository;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    EmployeeRepository employeeRepository) {
        this.authenticationService = authenticationService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    private ResponseEntity<Map<String, String>> login(@RequestBody String requestJsonStr,
                                                      HttpSession session,
                                                      HttpServletResponse response) throws Exception {
        JSONObject requestJson = new JSONObject(requestJsonStr);
        String token = authenticationService.authenticate((String)requestJson.get("emailAddress"),
                (String)requestJson.get("password"));
        Map<String, String> responseData = new HashMap<>();
        responseData.put("token", token);
        return ResponseEntity.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseData);
    }

    @PutMapping
    private ResponseEntity<Map<String, String>> refreshToken(
            Principal principal) throws Exception {
        JwtUtil jwtUtil = new JwtUtil();
        Optional<Employee> oEmployee  = employeeRepository.findByEmailAddress(principal.getName());
        if(!oEmployee.isPresent())
            throw new RessourceNotFoundException();
        String token = authenticationService.generateToken(oEmployee.get());
        Map<String, String> responseData = new HashMap<>();
        responseData.put("token", token);
        return ResponseEntity.status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseData);
    }
}
