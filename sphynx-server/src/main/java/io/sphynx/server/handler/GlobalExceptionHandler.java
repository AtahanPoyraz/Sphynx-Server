package io.sphynx.server.handler;

import io.sphynx.server.dto.GenericResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException e
    ) {

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);

            }
        );

        logger.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GenericResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        errors
                        )
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<?>> handleJsonParseException(
            HttpMessageNotReadableException e
    ) {
        logger.error("Invalid JSON format", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GenericResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid JSON format for one of the fields. Please provide a valid object.",
                        null
                        )
                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GenericResponse<?>> handleEntityNotFoundException(
            EntityNotFoundException e
    ) {
        logger.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GenericResponse<>(
                                HttpStatus.NOT_FOUND.value(),
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        logger.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GenericResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage(),
                        null
                        )
                );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericResponse<?>> handleRuntimeException(
            RuntimeException e
    ) {
        logger.error("Runtime exception occurred", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GenericResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage(),
                        null
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<?>> handleExceptions(Exception e) {
        logger.error("An error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", null));
    }
}