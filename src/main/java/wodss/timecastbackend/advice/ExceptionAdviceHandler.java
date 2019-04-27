package wodss.timecastbackend.advice;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wodss.timecastbackend.util.BadRequestException;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

@ControllerAdvice
public class ExceptionAdviceHandler {
    @ExceptionHandler(PreconditionFailedException.class)
    public ResponseEntity<String> handlePreconditionFailedException(PreconditionFailedException e) {
        return new ResponseEntity<String>(
                "Precondition for the ressource failed", HttpStatus.PRECONDITION_FAILED);
    }
    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<String> handleRessourceNotFoundException(RessourceNotFoundException e) {
        return new ResponseEntity<String>("Ressource not found", HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        return new ResponseEntity<String>("Bad Request", HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException e) {
        return new ResponseEntity<String>(
                "Employee not found or invalid password", HttpStatus.PRECONDITION_FAILED);
    }
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }
    @ExceptionHandler({AuthenticationException.class, JwkException.class})
    public ResponseEntity<String> handleAuthenticationException(Exception e) {
        return new ResponseEntity<String>("Unauthenticated or invalid token", HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<String>("Uncaught or internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
