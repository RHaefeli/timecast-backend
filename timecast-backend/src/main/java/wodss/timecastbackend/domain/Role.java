package wodss.timecastbackend.domain;

import javax.persistence.*;

@Entity @Table(name="roles")
public class Role {

    protected Role(){}
    public Role(int id, String description){
        this.id = id; this.description = description;
    }
    @Id
    private int id;

    @Column(name="desc")
    private String description;

}
