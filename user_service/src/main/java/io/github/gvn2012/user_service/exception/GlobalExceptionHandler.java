package io.github.gvn2012.user_service.exception;

import io.github.gvn2012.user_service.dtos.APIResource;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
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
    public APIResource<?> handleBadRequest(BadRequestException e) {
        return APIResource.error(
                "BAD_REQUEST",
                e.getMessage(),
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public APIResource<?> handleException(Exception e) {
        return APIResource.error(
                "INTERNAL_ERROR",
                "Something went wrong",
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
        );
    }
}