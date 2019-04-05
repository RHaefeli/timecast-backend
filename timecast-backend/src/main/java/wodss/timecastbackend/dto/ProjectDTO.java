package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDTO {
    @JsonProperty("id") private long id;
    @JsonProperty("name") private String name;
    @JsonProperty("projectManagerId") private long projectManagerId;
    @JsonProperty("ftePercentage") private float ftePercentage;
    @JsonProperty("startDate") private LocalDate startDate;
    @JsonProperty("endDate") private LocalDate endDate;

    public ProjectDTO(long id, long projectManagerId, String name, LocalDate startDate, LocalDate endDate, float ftePercentage) {
        this.id = id;
        this.name = name;
        this.projectManagerId = projectManagerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ftePercentage = ftePercentage;
    }

    public ProjectDTO() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getProjectManagerId() {
        return projectManagerId;
    }

    public void setProjectManagerId(long projectManagerId) {
        this.projectManagerId = projectManagerId;
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

    public float getFtePercentage() {
        return ftePercentage;
    }

    public void setFtePercentage(float ftePercentage) {
        this.ftePercentage = ftePercentage;
    }
}
