package wodss.timecastbackend.persistence;

        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.stereotype.Repository;
        import wodss.timecastbackend.domain.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
