package io.github.gvn2012.auth_service.exceptions;

import io.github.gvn2012.auth_service.dtos.APIResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<APIResource<?>> handleBaseException(BaseException e) {
        log.error("BaseException in Auth Service: code={}, message={}, status={}", 
            e.getCode(), e.getMessage(), e.getStatus());
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
        log.error("Unexpected error caught in Auth GlobalExceptionHandler: {}", e.getMessage(), e);
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
