package io.github.gvn2012.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(
                "NOT_FOUND",
                message,
                HttpStatus.NOT_FOUND
        );

    }
}
