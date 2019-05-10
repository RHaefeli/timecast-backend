package wodss.timecastbackend.domain;

public enum Role{
    ADMINISTRATOR("ADMINISTRATOR"),
    DEVELOPER("DEVELOPER"),
    PROJECTMANAGER("PROJECTMANAGER");

    private String value;

    Role(String s){
        this.value = s;
    }

    public String getValue(){
        return this.value;
    }
}

