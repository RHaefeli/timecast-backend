package wodss.timecastbackend.domain;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Project {

    public Project(){}

    public Project(String name, Employee projectManager, LocalDate startDate, LocalDate endDate, float ftePercentage) { ;
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
    @ManyToOne(optional = false)
    private Employee projectManager;
    @Min(0)
    private float ftePercentage;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
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
    //TODO: What will be used? FTEs? Hours?
}
