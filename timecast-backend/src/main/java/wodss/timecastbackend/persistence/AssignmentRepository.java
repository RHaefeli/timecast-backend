package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Assignment;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
