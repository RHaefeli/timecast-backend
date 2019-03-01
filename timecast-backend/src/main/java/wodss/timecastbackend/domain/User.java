package wodss.timecastbackend.domain;

import javax.persistence.*;

@Entity @Table(name="users")
public class User {

    protected User(){}
    public User(int id, String lastName, String firstName, Role role, int employment){

    }
    @Id
    private int id;

    @Column(name="last_name")
    private String lastName;
    @Column(name="first_name")
    private String firstName;

    @OneToOne
    @JoinColumn(name="role_fk")
    private Role role;

    @Column(name="employment")
    private int employment;
}
