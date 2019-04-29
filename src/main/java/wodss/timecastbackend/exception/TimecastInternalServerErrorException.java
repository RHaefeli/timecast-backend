package wodss.timecastbackend.exception;

public class TimecastInternalServerErrorException extends RuntimeException {
    public TimecastInternalServerErrorException(String errorMessage) {
        super(errorMessage);
    }

}