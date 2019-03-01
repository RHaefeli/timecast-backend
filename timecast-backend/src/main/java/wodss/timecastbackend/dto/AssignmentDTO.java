package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class AssignmentDTO {
    @JsonProperty("id") private long id;
    @JsonProperty("project") private Long project;
    @JsonProperty("user") private Long user;
    @JsonProperty("employment") private int employment;
    @JsonProperty("startDate") private LocalDate startDate;
    @JsonProperty("endDate") private LocalDate endDate;

    public AssignmentDTO(long id, Long project, Long user, int employment, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.project = project;
        this.user = user;
        this.employment = employment;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getProject() {
        return project;
    }

    public void setProject(Long project) {
        this.project = project;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
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
