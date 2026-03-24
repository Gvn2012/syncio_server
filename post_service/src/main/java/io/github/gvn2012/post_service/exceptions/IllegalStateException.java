package io.github.gvn2012.post_service.exceptions;

import org.springframework.http.HttpStatus;

public class IllegalStateException extends BaseException {

    public IllegalStateException(String message) {
        super(
                "IllegalStateException",
                message,
                HttpStatus.CONFLICT
        );
    }
}
