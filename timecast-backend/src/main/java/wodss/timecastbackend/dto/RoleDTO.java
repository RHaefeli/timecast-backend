package wodss.timecastbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoleDTO {
    @JsonProperty("id") private long id;
    @JsonProperty("name") private String name;
    @JsonProperty("description") private String description;

    public RoleDTO(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
