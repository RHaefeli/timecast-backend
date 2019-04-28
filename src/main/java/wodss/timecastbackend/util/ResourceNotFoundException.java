package wodss.timecastbackend.util;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
    }
    public ResourceNotFoundException() {
        super();
    }
}
