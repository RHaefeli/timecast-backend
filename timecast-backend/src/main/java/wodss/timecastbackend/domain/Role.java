package wodss.timecastbackend.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity @Table(name="roles")
public class Role {

    protected Role(){}

    public Role(String name, String description){
        this.name = name;
        this.description = description;
    }

    @Id
    private int id;
    @NotNull
    private String name;
    @NotNull
    private String description;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
