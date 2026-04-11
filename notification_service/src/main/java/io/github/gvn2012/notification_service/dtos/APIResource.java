package io.github.gvn2012.notification_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APIResource<T> {
    private boolean success;
    private String message;
    private T data;
    private HttpStatus status;
    private ErrorResponse error;

    public static <T> APIResource<T> ok(String message, T data) {
        return APIResource.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(HttpStatus.OK)
                .build();
    }

    public static <T> APIResource<T> error(String code, String message, HttpStatus status, T data) {
        return APIResource.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .status(status)
                .error(new ErrorResponse(code, message))
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}
