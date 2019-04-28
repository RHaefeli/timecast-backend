package wodss.timecastbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.persistence.EmployeeRepository;
import java.util.Optional;

@Component
public class EmployeeSession {

    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeSession(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public boolean isAuthenticated() {

        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !(SecurityContextHolder.getContext().getAuthentication()
                        instanceof AnonymousAuthenticationToken);
    }

    public Employee getEmployee() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Employee> oEmployee = employeeRepository.findByEmailAddress(name);
        if (!oEmployee.isPresent()) {
            return null;
        }
        return oEmployee.get();
    }
}
