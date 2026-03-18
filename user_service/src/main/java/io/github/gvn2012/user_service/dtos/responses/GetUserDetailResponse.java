package io.github.gvn2012.user_service.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserDetailResponse {
    private UserResponse userResponse;
    private List<UserEmailResponse> userEmailResponse;
    private List<UserPhoneResponse> userPhoneResponse;
    private UserProfileResponse userProfileResponse;
}
