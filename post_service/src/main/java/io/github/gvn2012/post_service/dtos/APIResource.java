package io.github.gvn2012.post_service.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class APIResource<T> {
    private boolean success;
    private String message;
    private T data;
    private HttpStatus status;
    private LocalDateTime timestamp;
    private ErrorResource error;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor
    @Data
    public static class ErrorResource {
        private String code;
        private String message;
        private String detail;

        public ErrorResource (String message) {
            this.message = message;
        }

        public ErrorResource (String message, String code) {
            this.message = message;
            this.code = code;
        }
    }

    private APIResource(){
        this.timestamp = LocalDateTime.now();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder <T> {
        private final APIResource<T> resource;

        private Builder(){
            resource = new APIResource<>();
        }

        public Builder<T> success(boolean success) {
            resource.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            resource.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            resource.data = data;
            return this;
        }

        public Builder<T> status(HttpStatus status) {
            resource.status = status;
            return this;
        }

        public Builder<T> error(ErrorResource error) {
            resource.error = error;
            return this;
        }

        public APIResource<T> build() {
            return resource;
        }
    }

    public static <T> APIResource <T> ok(String message, T data) {
        return APIResource.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(HttpStatus.OK)
                .build();
    }

    public static <T> APIResource <T> ok(String message, T data, HttpStatus httpStatus) {
        return APIResource.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(httpStatus)
                .build();
    }

    public static <T> APIResource <T> message(String message, HttpStatus status) {
        return APIResource.<T>builder()
                .success(true)
                .message(message)
                .status(status)
                .build();
    }

    public static <T> APIResource <T> error(String code, String message, HttpStatus status, String detail) {
        return APIResource.<T>builder()
                .success(false)
                .error(new ErrorResource(code, message, detail))
                .status(status)
                .build();
    }
}

