package wodss.timecastbackend.util;

public class TimecastInternalServerErrorException extends RuntimeException {
    public TimecastInternalServerErrorException(String errorMessage) {
        super(errorMessage);
    }

}