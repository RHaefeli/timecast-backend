package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Contract;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
}
