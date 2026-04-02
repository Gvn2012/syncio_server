package io.github.gvn2012.auth_service.exceptions;

import io.github.gvn2012.auth_service.dtos.APIResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<APIResource<?>> handleBaseException(BaseException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(
                        APIResource.error(
                                e.getCode(),
                                e.getMessage(),
                                e.getStatus(),
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
