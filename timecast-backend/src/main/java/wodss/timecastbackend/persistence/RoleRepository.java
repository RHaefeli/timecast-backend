package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import wodss.timecastbackend.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
