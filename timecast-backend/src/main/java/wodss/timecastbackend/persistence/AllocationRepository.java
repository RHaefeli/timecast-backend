package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Allocation;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
}
