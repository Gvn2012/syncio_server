package io.github.gvn2012.user_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserRegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private LocalDate dateBirth;
}
