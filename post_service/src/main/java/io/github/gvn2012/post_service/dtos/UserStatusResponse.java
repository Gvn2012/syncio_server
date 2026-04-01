package io.github.gvn2012.post_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusResponse {
    private Boolean active;
    private Boolean suspended;
    private Boolean banned;
    private Boolean softDeleted;
}
