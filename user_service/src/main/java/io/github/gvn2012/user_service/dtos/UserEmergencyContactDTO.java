package io.github.gvn2012.user_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmergencyContactDTO {
    private String contactName;
    private String relationship;
    private String phoneNumber;
    private String email;
    private Boolean isPrimary;
}
