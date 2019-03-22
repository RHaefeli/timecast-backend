package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class EmployeeDTO {
    @JsonProperty("id") private Long id;
    @JsonProperty("lastName") private String lastName;
    @JsonProperty("firstName") private String firstName;
    @JsonProperty("emailAddress") private String emailAddress;
    @JsonProperty("roleId") private Long roleId;

    public EmployeeDTO(Long id, String lastName, String firstName, String emailAddress, Long roleId, int employment) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.emailAddress = emailAddress;
        this.roleId = roleId;
    }

    public EmployeeDTO(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmailAddress() { return emailAddress; }

    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
