package io.github.gvn2012.post_service.exceptions;

import io.github.gvn2012.post_service.dtos.APIResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public APIResource<?> handleBaseException(BaseException e) {
        return APIResource.error(
                e.getCode(),
                e.getMessage(),
                e.getStatus(),
                e.getMessage()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<APIResource<?>> handleBadRequest(BadRequestException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        APIResource.error(
                                "BAD_REQUEST",
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST,
                                e.getMessage()
                        )
                );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResource<?>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(
                        APIResource.error(
                                "DATA_INTEGRITY_VIOLATION",
                                e.getMessage(),
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                e.getMessage()
                        )
                );
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<APIResource<?>> handleNotFoundException(NotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        APIResource.error(
                                "NOT_FOUND",
                                e.getMessage(),
                                HttpStatus.NOT_FOUND,
                                e.getMessage()
                        )
                );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<APIResource<?>> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        APIResource.error(
                                "IllegalStateException",
                                e.getMessage(),
                                HttpStatus.CONFLICT,
                                e.getMessage()
                        )
                );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<APIResource<?>> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        APIResource.error(
                                "FORBIDDEN",
                                e.getMessage(),
                                HttpStatus.FORBIDDEN,
                                e.getMessage()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResource<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        APIResource.error(
                                "VALIDATION_FAILED",
                                "Input validation failed",
                                HttpStatus.BAD_REQUEST,
                                errors.toString()
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResource<?>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        APIResource.error(
                                "INTERNAL_ERROR",
                                "Something went wrong",
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                e.getMessage()
                        )
                );
    }
}