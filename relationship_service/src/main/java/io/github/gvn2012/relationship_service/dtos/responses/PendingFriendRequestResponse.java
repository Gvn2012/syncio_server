package io.github.gvn2012.relationship_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingFriendRequestResponse {
    private UUID requestId;
    private UUID senderUserId;
    private UUID receiverUserId;
    private UUID otherUserId;
    private String direction;
    private String username;
    private String displayName;
    private String profilePictureUrl;
    private String message;
    private Boolean seen;
    private LocalDateTime createdAt;
}
