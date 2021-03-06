package wodss.timecastbackend.domain;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Allocation {

    public Allocation(){}

    public Allocation(Project project, Contract contract, int pensumPercentage, LocalDate startDate, LocalDate endDate) {
        this.project = project;
        this.contract = contract;
        this.pensumPercentage = pensumPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(optional = false)
    private Project project;
    @ManyToOne(optional = false)
    private Contract contract;
    @Max(100)
    @Min(0)
    private int pensumPercentage;
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

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public int getPensumPercentage() {
        return pensumPercentage;
    }

    public void setPensumPercentage(int pensumPercentage) {
        this.pensumPercentage = pensumPercentage;
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
