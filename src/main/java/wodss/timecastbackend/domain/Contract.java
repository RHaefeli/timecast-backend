package wodss.timecastbackend.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Contract {

    public Contract(){}

    public Contract(Employee employee, int pensumPercentage, LocalDate startDate, LocalDate endDate) {
        this.employee = employee;
        this.pensumPercentage = pensumPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Id
    private long id;
    @OneToOne(optional = false)
    private Employee employee;
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

    public void setId(long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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
