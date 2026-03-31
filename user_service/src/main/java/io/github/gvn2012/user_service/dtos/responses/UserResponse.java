package io.github.gvn2012.user_service.dtos.responses;

import io.github.gvn2012.user_service.entities.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String middleName;
    private Gender gender;
    private String locale;
    private String timezone;
    private Boolean active;
    private Boolean suspended;
    private Boolean banned;
    private Boolean mustChangePassword;
}
