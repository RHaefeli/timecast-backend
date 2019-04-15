package wodss.timecastbackend.persistence;

        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.data.repository.query.Param;
        import org.springframework.stereotype.Repository;
        import wodss.timecastbackend.domain.Employee;
        import wodss.timecastbackend.domain.Role;

        import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
        @Query("SELECT e FROM Employee e WHERE" +
                "(:role IS NULL OR :role = e.role)")
        List<Employee> findByQuery(@Param("role") Role role);
}
