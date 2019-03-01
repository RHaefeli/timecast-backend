package wodss.timecastbackend.domain;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity @Table(name="users")
public class User {

    protected User(){}

    public User(String lastName, String firstName, Role role, int employment) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.role = role;
        this.employment = employment;
    }

    @Id
    private int id;
    @NotNull
    private String lastName;
    @NotNull
    private String firstName;
    @OneToOne
    @NotNull
    private Role role;
    @Min(0)
    @Max(100)
    private int employment;

    public int getId() {
        return id;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getEmployment() {
        return employment;
    }

    public void setEmployment(int employment) {
        this.employment = employment;
    }
}
