package io.github.gvn2012.relationship_service.dtos.responses;

import io.github.gvn2012.relationship_service.entities.enums.RelationshipType;
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
public class RelationshipUserSummaryResponse {
    private UUID relationshipId;
    private UUID userId;
    private String username;
    private String displayName;
    private String profilePictureUrl;
    private RelationshipType relationshipType;
    private Integer mutualFriendsCount;
    private LocalDateTime createdAt;
}
