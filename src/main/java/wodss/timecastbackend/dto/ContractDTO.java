package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class ContractDTO {
    @JsonProperty("id") private long id;
    @NotNull
    @Min(0)
    @JsonProperty("employeeId") private Long employeeId;
    @NotNull
    @Min(0)
    @Max(100)
    @JsonProperty("pensumPercentage") private int pensumPercentage;
    @NotNull
    @JsonProperty("startDate") private LocalDate startDate;
    @NotNull
    @JsonProperty("endDate") private LocalDate endDate;

    public ContractDTO(long id, Long projectId, Long employeeId, int pensumPercentage, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.pensumPercentage = pensumPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ContractDTO() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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
