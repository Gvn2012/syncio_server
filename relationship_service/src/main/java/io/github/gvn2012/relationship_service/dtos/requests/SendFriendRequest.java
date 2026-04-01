package io.github.gvn2012.relationship_service.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequest {
    @NotNull(message = "Target user ID is required")
    private UUID targetUserId;
    private String message;
}
