package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Employee;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    @Query("SELECT c FROM Contract c WHERE" +
            ":employeeId IS NULL OR :employeeId = c.employee.id AND" +
            "(CAST(:fromDate AS date) IS NULL OR :fromDate <= c.endDate) AND" +
            "(CAST(:toDate AS date) IS NULL OR :toDate >= c.startDate)")
    List<Contract> findByQuery(
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
    @Query("SELECT CASE WHEN count(c)>0 THEN true ELSE false END from Contract c where :employeeId = c.employee.id")
    boolean existsByEmployeeId(@Param("employeeId") Long employeeId);
}