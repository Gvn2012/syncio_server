package io.github.gvn2012.post_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
