package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.format.annotation.DateTimeFormat;
import wodss.timecastbackend.util.DateSerializer;
import wodss.timecastbackend.validator.DateConstraint;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class ProjectDTO {
    @JsonProperty("id") private long id;
    @NotNull
    @NotBlank
    @JsonProperty("name") private String name;
    @NotNull
    @Min(0)
    @JsonProperty("projectManagerId") private long projectManagerId;
    @NotNull
    @Min(0)
    @JsonProperty("ftePercentage") private float ftePercentage;
    @NotNull
    @JsonProperty("startDate") private LocalDate startDate;
    @NotNull
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
