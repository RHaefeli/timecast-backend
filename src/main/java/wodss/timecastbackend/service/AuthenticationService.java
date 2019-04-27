package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.security.JwtUtil;
import wodss.timecastbackend.security.PdfEmployee;
import wodss.timecastbackend.security.SecurityConstants;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Component
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public String authenticate(String emailAddress, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(emailAddress, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        JwtUtil jwtUtil = new JwtUtil();
        PdfEmployee authEmployee = ((PdfEmployee) authentication.getPrincipal());
        Collection<? extends GrantedAuthority> roles = authEmployee.getAuthorities();
        byte[] signingKey = SecurityConstants.JWT_SECRET.getBytes();
        return generateToken(authEmployee.getEmployee());
    }

    public String generateToken(Employee employee) {
        JwtUtil jwtUtil = new JwtUtil();
        return jwtUtil.generateToken(employee);
    }
}
