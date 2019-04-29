package wodss.timecastbackend.exception;

public class BadRequestException extends Exception {
    public BadRequestException(String errorMessage) {
        super(errorMessage);
    }

    public BadRequestException() {
        super();
    }
}
