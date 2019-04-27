package wodss.timecastbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.dto.EmployeeDTO;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private ObjectMapper objectMapper = new ObjectMapper();
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public JwtUtil() {
        String publicKeyPEM = RsaUtil.getKey("classpath:keystore/public_key.pem");
        String privateKeyPEM = RsaUtil.getKey("classpath:keystore/private_key.pem");
        privateKey = RsaUtil.getPrivateKeyFromString(privateKeyPEM);
        publicKey = RsaUtil.getPublicKeyFromString(publicKeyPEM);
    }
    public EmployeeDTO parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(privateKey)
                    .parseClaimsJws(token).getBody();
            EmployeeDTO employeeDTO = objectMapper.convertValue(claims.get("employee"), EmployeeDTO.class);
            return employeeDTO;

        } catch (JwtException | ClassCastException e) {
            throw new InternalException(e.getMessage());
        }
    }

    public String generateToken(Employee employee) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
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
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .claim("employee", employeeMap)
                .compact();
    }
}
