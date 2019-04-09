package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
