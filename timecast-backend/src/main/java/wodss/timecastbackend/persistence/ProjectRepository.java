package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
