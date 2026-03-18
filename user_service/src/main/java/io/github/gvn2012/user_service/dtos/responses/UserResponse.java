package io.github.gvn2012.user_service.dtos.responses;


import io.github.gvn2012.user_service.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String locale;
    private String timezone;
}
