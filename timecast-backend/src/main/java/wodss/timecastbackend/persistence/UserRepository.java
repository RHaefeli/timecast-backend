package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
