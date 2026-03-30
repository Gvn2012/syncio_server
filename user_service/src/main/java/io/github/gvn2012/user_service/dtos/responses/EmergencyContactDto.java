package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmergencyContactDto {
    private String id;
    private String contactName;
    private String relationship;
    private String phoneNumber;
    private String email;
    private Boolean primary;
    private Integer priority;
}
