package wodss.timecastbackend.util;

public class BadRequestException extends Exception {
    public BadRequestException(String errorMessage) {
        super(errorMessage);
    }

    public BadRequestException() {
        super();
    }
}
