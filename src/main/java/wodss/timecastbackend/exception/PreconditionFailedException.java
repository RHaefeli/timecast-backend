package wodss.timecastbackend.exception;

public class PreconditionFailedException extends Exception {
    public PreconditionFailedException(String errorMessage) {
        super(errorMessage);
    }

    public PreconditionFailedException() {
        super();
    }
}
