package wodss.timecastbackend.domain;

import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class Project {

    @Id
    private int id;
    @NotNull
    private String name;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private LocalDateTime endDate;
    @NotNull
    private LocalDateTime estimatedEndDate;
    @Min(0)
    private float ftes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
    //TODO: What will be used? FTEs? Hours?
}
