package wodss.timecastbackend.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class Project {

    protected Project(){}

    public Project(String name, LocalDateTime startDate, LocalDateTime endDate,  LocalDateTime estimatedEndDate, float ftes, Assignment assignment) { ;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.estimatedEndDate = estimatedEndDate;
        this.ftes = ftes;
        this.assignment = assignment;
    }

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

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL)
    private Assignment assignment;

    public int getId() {
        return id;
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
