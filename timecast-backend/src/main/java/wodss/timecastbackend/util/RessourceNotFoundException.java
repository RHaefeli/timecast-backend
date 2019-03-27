package wodss.timecastbackend.util;

public class RessourceNotFoundException extends Exception {
    public RessourceNotFoundException(String errorMessage) {
        super(errorMessage);
    }
    public RessourceNotFoundException() {
        super();
    }
}
