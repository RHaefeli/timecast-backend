package wodss.timecastbackend.util;

public class PreconditionFailed extends Exception {
    public PreconditionFailed(String errorMessage) {
        super(errorMessage);
    }
}
