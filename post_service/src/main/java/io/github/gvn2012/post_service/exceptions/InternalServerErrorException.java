package io.github.gvn2012.post_service.exceptions;

import io.github.gvn2012.post_service.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends BaseException {
    public InternalServerErrorException(String message) {
        super("INTERNAL_SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
