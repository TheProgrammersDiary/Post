package com.evalvis.post.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
public class ControllerException {
    private static final Logger log = LoggerFactory.getLogger(ControllerException.class);

    @ExceptionHandler
    public ResponseEntity<String> clientException(BadRequestException e) {
        return clientError(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> clientException(RestNotFoundException e) {
        return clientError(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> clientException(UnauthorizedException e) {
        return clientError(e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<String> badCredentialsException(BadCredentialsException e) {
        return clientError(e, "Bad credentials.", HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<String> clientError(Exception e, HttpStatus status) {
        return clientError(e, e.getMessage(), status);
    }

    private ResponseEntity<String> clientError(Exception e, String responseMessage, HttpStatus status) {
        String errorCode = UUID.randomUUID().toString();
        log.info("Client error code: {}, ", errorCode, e);
        return new ResponseEntity<>(responseMessage + " Error code: " + errorCode, status);
    }

    @ExceptionHandler
    public ResponseEntity<String> exception(Exception e) {
        String errorCode = UUID.randomUUID().toString();
        log.error("Error code: {}, ", errorCode, e);
        return new ResponseEntity<>("Something went wrong. Error code: " + errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
