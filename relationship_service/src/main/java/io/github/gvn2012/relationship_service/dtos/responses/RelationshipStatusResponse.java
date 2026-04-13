package io.github.gvn2012.relationship_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipStatusResponse {
    private boolean isFollowing;
    private boolean isFollowedBy;
    private boolean isFriend;
    private boolean isBlocking;
    private boolean isBlockedBy;
    private String friendRequestStatus;
    private UUID friendRequestId;
}
