package wodss.timecastbackend.domain;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class Employee {

    public Employee(){}

    public Employee(String lastName, String firstName, String emailAddress, Role role) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.emailAddress = emailAddress;
        this.role = role;
        this.active = true;
    }

    @Id
    private long id;
    @NotNull
    private String lastName;
    @NotNull
    private String firstName;
    @NotNull
    private String emailAddress;
    @OneToOne(optional = false)
    private Role role;
    @NotNull
    private boolean active;

    public long getId() {
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

    public String getEmailAddress() { return emailAddress; }

    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
