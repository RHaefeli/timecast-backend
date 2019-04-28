package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class AllocationDTO {
    @JsonProperty("id") private long id;
    @NotNull
    @Min(0)
    @JsonProperty("projectId") private Long projectId;
    @NotNull
    @JsonProperty("contractId") private Long contractId;
    @NotNull
    @Min(0)
    @Max(100)
    @JsonProperty("pensumPercentage") private int pensumPercentage;
    @NotNull
    @JsonProperty("startDate") private LocalDate startDate;
    @NotNull
    @JsonProperty("endDate") private LocalDate endDate;

    public AllocationDTO(long id, Long projectId, Long contractId, int pensumPercentage, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.projectId = projectId;
        this.contractId = contractId;
        this.pensumPercentage = pensumPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public AllocationDTO() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
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
