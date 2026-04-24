package io.polaris.sebrae.exception;

import io.polaris.sebrae.dto.ApiErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.EntityNotFoundException;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiErrorDTO response = new ApiErrorDTO(
            "Validation failed", 
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            errors
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiErrorDTO response = new ApiErrorDTO("Invalid argument", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleEntityNotFoundException(EntityNotFoundException ex) {
        ApiErrorDTO response = new ApiErrorDTO("Resource not found", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> handleResponseStatusException(org.springframework.web.server.ResponseStatusException ex) {
        ApiErrorDTO response = new ApiErrorDTO(ex.getReason(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        ApiErrorDTO response = new ApiErrorDTO("Resource not found", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        ApiErrorDTO response = new ApiErrorDTO("Malformed JSON", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGenericException(Exception ex) {
        logger.error("Internal Server Error", ex);
        ApiErrorDTO response = new ApiErrorDTO("Internal error", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
