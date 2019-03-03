package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class AssignmentDTO {
    @JsonProperty("id") private long id;
    @JsonProperty("projectId") private Long projectId;
    @JsonProperty("userId") private Long userId;
    @JsonProperty("employment") private int employment;
    @JsonProperty("startDate") private LocalDate startDate;
    @JsonProperty("endDate") private LocalDate endDate;

    public AssignmentDTO(long id, Long projectId, Long userId, int employment, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.employment = employment;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public AssignmentDTO() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getEmployment() {
        return employment;
    }

    public void setEmployment(int employment) {
        this.employment = employment;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
