package wodss.timecastbackend.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Assignment {

    public Assignment(){}

    public Assignment(Project project, User user, int employment, LocalDate startDate, LocalDate endDate) {
        this.project = project;
        this.user = user;
        this.employment = employment;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Id
    private long id;
    @OneToOne(optional = false)
    private Project project;
    @OneToOne(optional = false)
    private User user;
    @Max(100)
    @Min(0)
    private int employment;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;

    public long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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
