package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@JsonAutoDetect
public class EmployeeDTO {
    //TODO: Roles are only passes as string
    @JsonProperty("id") private Long id;
    @NotBlank
    @NotNull
    @JsonProperty("lastName") private String lastName;
    @NotBlank
    @NotNull
    @JsonProperty("firstName") private String firstName;
    @NotBlank
    @NotNull
    @JsonProperty("emailAddress") private String emailAddress;
    @JsonProperty("role") private String role;
    @JsonProperty("active") private boolean active;

    public EmployeeDTO(Long id, String lastName, String firstName, String emailAddress, String role, boolean active) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.emailAddress = emailAddress;
        this.role = role;
        this.active = active;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role.toUpperCase();
    }

    public boolean getActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public void outputDTODebug(){
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(emailAddress);
        System.out.println(role);
        System.out.println(active);
    }
}
