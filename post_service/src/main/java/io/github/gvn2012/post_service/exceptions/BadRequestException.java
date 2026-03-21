package io.github.gvn2012.post_service.exceptions;

import io.github.gvn2012.post_service.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }
}