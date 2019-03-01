package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import wodss.timecastbackend.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
