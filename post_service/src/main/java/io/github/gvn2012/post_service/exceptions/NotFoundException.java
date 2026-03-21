package io.github.gvn2012.post_service.exceptions;

import io.github.gvn2012.post_service.exceptions.BaseException;
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
