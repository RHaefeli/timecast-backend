package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import wodss.timecastbackend.domain.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
