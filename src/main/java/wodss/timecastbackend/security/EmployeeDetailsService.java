package wodss.timecastbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.persistence.EmployeeRepository;
import java.util.Optional;

@Service("userDetailsService")
public class EmployeeDetailsService  implements UserDetailsService {

    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        Optional<Employee> oEmployee = employeeRepository.findByEmailAddress(emailAddress);
        if (!oEmployee.isPresent())
            throw new UsernameNotFoundException("User not found.");
        return new PdfEmployee(oEmployee.get());
    }
}
