package io.github.gvn2012.user_service.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private LocalDate dateOfBirth;
    private String bio;
    private String location;
    private Map<String, String> contactInfo;
    private Short profileCompletedScore;
    private Set<UserProfilePictureResponse> userProfilePictureResponseList;
}

