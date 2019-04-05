package wodss.timecastbackend.domain;

import java.util.List;

public enum Role{
    ADMINISTRATOR("administrator"),
    DEVELOPER("developer"),
    PROJECTMANAGER("projectmanager");

    private String value;

    Role(String s){
        this.value = s;
    }
    public String getValue(){
        return this.value;
    }

}

