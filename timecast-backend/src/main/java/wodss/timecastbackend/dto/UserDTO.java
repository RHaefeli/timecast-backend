package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import wodss.timecastbackend.domain.User;

@JsonAutoDetect
public class UserDTO {
    @JsonProperty("id") private Long id;
    @JsonProperty("lastName") private String lastName;
    @JsonProperty("firstName") private String firstName;
    @JsonProperty("roleId") private Long roleId;
    @JsonProperty("employment") private int employment;

    public UserDTO(Long id, String lastName, String firstName, Long roleId, int employment) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.roleId = roleId;
        this.employment = employment;
    }

    public UserDTO(){}

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

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public int getEmployment() {
        return employment;
    }

    public void setEmployment(int employment) {
        this.employment = employment;
    }
}
