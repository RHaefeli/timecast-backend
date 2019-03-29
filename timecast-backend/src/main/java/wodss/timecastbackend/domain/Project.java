package wodss.timecastbackend.domain;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Project {

    public Project(){}

    public Project(String name, Employee projectManager, LocalDateTime startDate, LocalDateTime endDate, float ftePercentage) { ;
        this.name = name;
        this.projectManager = projectManager;
        this.ftePercentage = ftePercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Id
    private long id;
    @NotNull
    private String name;
    @OneToOne(optional = false)
    private Employee projectManager;
    @Min(0)
    private float ftePercentage;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private LocalDateTime endDate;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Allocation> allocations;

    public long getId() {
        return id;
    }

    public Employee getProjectManager() { return projectManager; }

    public void setProjectManager(Employee projectManager) { this.projectManager = projectManager; }

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

    public float getFtePercentage() {
        return ftePercentage;
    }

    public void setFtePercentage(float ftePercentage) {
        this.ftePercentage = ftePercentage;
    }
    //TODO: What will be used? FTEs? Hours?
}
