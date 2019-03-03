package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ProjectDTO {
    @JsonProperty("id") private long id;
    @JsonProperty("name") private String name;
    @JsonProperty("startDate") private LocalDateTime startDate;
    @JsonProperty("endDate") private LocalDateTime endDate;
    @JsonProperty("estimatedEndDate") private LocalDateTime estimatedEndDate;
    @JsonProperty("ftes") private float ftes;

    public ProjectDTO(long id, String name, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime estimatedEndDate, float ftes) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.estimatedEndDate = estimatedEndDate;
        this.ftes = ftes;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getEstimatedEndDate() {
        return estimatedEndDate;
    }

    public void setEstimatedEndDate(LocalDateTime estimatedEndDate) {
        this.estimatedEndDate = estimatedEndDate;
    }

    public float getFtes() {
        return ftes;
    }

    public void setFtes(float ftes) {
        this.ftes = ftes;
    }
}
