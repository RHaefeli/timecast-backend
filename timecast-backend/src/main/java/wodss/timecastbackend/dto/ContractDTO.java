package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class ContractDTO {
    @JsonProperty("id") private long id;
    @JsonProperty("employeeId") private Long employeeId;
    @JsonProperty("pensumPercentage") private int pensumPercentage;
    @JsonProperty("startDate") private LocalDate startDate;
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

    public Long getUserId() {
        return employeeId;
    }

    public void setUserId(Long employeeId) {
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
