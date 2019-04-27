package wodss.timecastbackend.security;


import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;
import wodss.timecastbackend.dto.EmployeeDTO;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader(SecurityConstants.TOKEN_HEADER);

        if (StringUtils.isEmpty(header) || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
        } else {
            try {
                UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } catch (JwtException e) {
                filterChain.doFilter(request, response);
            }
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) throws JwtException {
        String authHeader = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (!StringUtils.isEmpty(authHeader)) {
            JwtUtil jwtUtil = new JwtUtil();
            String token = authHeader.substring(authHeader.indexOf(' ') + 1);

            EmployeeDTO parsedEmployee = jwtUtil.parseToken(token);

            String emailAdress = parsedEmployee.getEmailAddress();

            List<GrantedAuthority> role = Arrays.asList(
                    new SimpleGrantedAuthority(parsedEmployee.getRole().toString()));

            if (!StringUtils.isEmpty(emailAdress)) {
                return new UsernamePasswordAuthenticationToken(emailAdress, null, role);
            }
        }
        return null;
    }
}
