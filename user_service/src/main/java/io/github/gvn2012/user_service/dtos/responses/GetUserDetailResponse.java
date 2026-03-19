package io.github.gvn2012.user_service.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserDetailResponse {
    private UserResponse userResponse;
    private Set<UserEmailResponse> userEmailResponse;
    private Set<UserPhoneResponse> userPhoneResponse;
    private UserProfileResponse userProfileResponse;
}
