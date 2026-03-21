package io.github.gvn2012.post_service.exceptions;

import io.github.gvn2012.post_service.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class DataIntegrityViolationException extends BaseException {
    public DataIntegrityViolationException(String message) {
      super(
              "DATA_INTEGRITY_VIOLATION",
              message,
              HttpStatus.UNPROCESSABLE_ENTITY
      );

    }
}
