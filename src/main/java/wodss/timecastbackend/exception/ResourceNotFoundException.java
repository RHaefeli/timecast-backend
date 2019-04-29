package wodss.timecastbackend.exception;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
    }
    public ResourceNotFoundException() {
        super();
    }
}
