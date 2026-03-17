package io.github.gvn2012.user_service.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends BaseException {
    public InternalServerErrorException(String message) {
        super("INTERNAL_SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
