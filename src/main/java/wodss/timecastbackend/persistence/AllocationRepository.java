package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Allocation;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    @Query("SELECT a FROM Allocation a WHERE (:employeeId IS NULL OR a.contract.employee.id = :employeeId) AND" +
            "(:projectId IS NULL OR a.project.id = :projectId) AND" +
            "(CAST(:fromDate AS date) IS NULL OR :fromDate < a.endDate) AND" +
            "(CAST(:toDate AS date) IS NULL OR :toDate > a.startDate)")
    List<Allocation> findByQuery(
            @Param("employeeId") Long employeeId,
            @Param("projectId") Long projectId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}