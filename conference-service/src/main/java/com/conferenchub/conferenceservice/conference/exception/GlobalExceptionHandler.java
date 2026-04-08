package com.conferenchub.conferenceservice.conference.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // handle Keynote not found exception
    @ExceptionHandler(KeynoteNotFoundException.class)
    public ResponseEntity<ApiError> handleKeynoteNotFoundException(KeynoteNotFoundException ex) {
        log.error("Keynote not found: {}", ex.getMessage(), ex);
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Keynote not found",
                ex.getMessage(),
                Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // handle Conference not found exception
    @ExceptionHandler(ConferenceNotFoundException.class)
    public ResponseEntity<ApiError> handleConferenceNotFoundException(ConferenceNotFoundException ex) {
        log.error("Conference not found: {}", ex.getMessage(), ex);
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Conference not found",
                ex.getMessage(),
                Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // handle run time exception
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


    @ExceptionHandler(KeynoteServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleKeynoteServiceUnavailable(
            KeynoteServiceUnavailableException ex) {
        log.error("Keynote service unavailable: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Downstream Service Unavailable",
                ex.getMessage(),
                Instant.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

}
