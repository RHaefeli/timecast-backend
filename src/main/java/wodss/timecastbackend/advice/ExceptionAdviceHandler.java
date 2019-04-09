package wodss.timecastbackend.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wodss.timecastbackend.util.BadRequestException;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

@ControllerAdvice
public class ExceptionAdviceHandler {
    @ExceptionHandler(PreconditionFailedException.class)
    public ResponseEntity<String> handlePreconditionFailedException(PreconditionFailedException e) {
        return new ResponseEntity<String>("Precondition for the ressource failed", HttpStatus.PRECONDITION_FAILED);
    }
    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<String> handleRessourceNotFoundException(RessourceNotFoundException e) {
        return new ResponseEntity<String>("Ressource not found", HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        return new ResponseEntity<String>("Bad Request", HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<String>("Uncaught or internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
