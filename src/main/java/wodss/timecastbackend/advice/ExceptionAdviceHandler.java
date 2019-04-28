package wodss.timecastbackend.advice;

import org.codehaus.jackson.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wodss.timecastbackend.util.BadRequestException;
import wodss.timecastbackend.util.ForbiddenException;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.ResourceNotFoundException;

@ControllerAdvice
public class ExceptionAdviceHandler {

    Logger logger = LoggerFactory.getLogger(ExceptionAdviceHandler.class);
    @ExceptionHandler({PreconditionFailedException.class, MethodArgumentNotValidException.class,
            JsonProcessingException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<String> handlePreconditionFailedException(Exception e) {
        logger.error(e.getMessage());
        return new ResponseEntity<String>(
                "Precondition for the ressource failed", HttpStatus.PRECONDITION_FAILED);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleRessourceNotFoundException(ResourceNotFoundException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<String>("Ressource not found", HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<String>("Bad Request", HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<String>(
                "Employee not found or invalid password", HttpStatus.PRECONDITION_FAILED);
    }
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }
    @ExceptionHandler({AuthenticationException.class, JwkException.class})
    public ResponseEntity<String> handleAuthenticationException(Exception e) {
        return new ResponseEntity<String>("Unauthenticated or invalid token", HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<String> handleFrobiddenException(ForbiddenException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.FORBIDDEN);
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error(e.getMessage());
        return new ResponseEntity<String>("Uncaught or internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
