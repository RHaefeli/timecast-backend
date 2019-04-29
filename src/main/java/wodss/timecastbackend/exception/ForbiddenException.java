package wodss.timecastbackend.exception;

public class ForbiddenException extends Exception {

    public ForbiddenException(String errorMessage) {
        super(errorMessage);
    }

    public ForbiddenException() { super(); }
}
