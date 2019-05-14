package wodss.timecastbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.exception.TimecastInternalServerErrorException;

import java.security.Key;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Key key;

    public JwtUtil() {
        key = HsUtil.getKeyFromFile("classpath:keystore/secret.txt");
    }

    public EmployeeDTO parseToken(String token) throws JwtException {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token).getBody();
            EmployeeDTO employeeDTO = objectMapper.convertValue(claims.get("employee"), EmployeeDTO.class);
            return employeeDTO;

        } catch (JwtException e) {
            throw new JwtException(e.getMessage());
        } catch ( ClassCastException e) {
            throw new TimecastInternalServerErrorException(e.getMessage());
        }
    }

    public String generateToken(Employee employee) {
        Claims claims = Jwts.claims();
        claims.setIssuer("FHNW Wodss");
        claims.setSubject("Login token");
        claims.setIssuedAt(new Date());
        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();
        Map<String, Object> employeeMap = new HashMap<>();
        employeeMap.put("id", employee.getId());
        employeeMap.put("firstName", employee.getFirstName());
        employeeMap.put("lastName", employee.getLastName());
        employeeMap.put("emailAddress", employee.getEmailAddress());
        employeeMap.put("role", employee.getRole());
        employeeMap.put("active", employee.isActive());
        claims.setExpiration(new Date(t + (30 * 60000)));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .claim("employee", employeeMap)
                .compact();
    }
}
