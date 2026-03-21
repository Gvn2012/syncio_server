package io.github.gvn2012.post_service.exceptions;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.exceptions.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResource<?>> handleException(Exception e) {
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