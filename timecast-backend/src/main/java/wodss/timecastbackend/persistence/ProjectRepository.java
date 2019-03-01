package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import wodss.timecastbackend.domain.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
