package wodss.timecastbackend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wodss.timecastbackend.domain.Project;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p WHERE " +
            "(:projectManagerId IS NULL OR :projectManagerId = p.projectManager.id) AND" +
            "(CAST(:fromDate AS date) IS NULL OR :fromDate <= p.endDate) AND" +
            "(CAST(:toDate AS date) IS NULL OR :toDate >= p.startDate)")
    List<Project> findByQuery(
            @Param("projectManagerId") Long projectManagerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT CASE WHEN count(p)>0 THEN true ELSE false END from Project p where :projectManagerId = p.projectManager.id")
    boolean existsByProjectManagerId(@Param("projectManagerId") long id);
}
