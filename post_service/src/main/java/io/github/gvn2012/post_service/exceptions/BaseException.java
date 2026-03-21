package io.github.gvn2012.post_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public BaseException(String code, String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = code;
    }

}