package io.github.gvn2012.relationship_service.dtos.responses;

import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
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
public class RelationshipResponse {
    private UUID id;
    private UUID sourceUserId;
    private UUID targetUserId;
    private RelationshipType relationshipType;
    private RelationshipStatus status;
    private Boolean isCloseFriend;
    private Boolean isFavorite;
    private String sourceNickname;
    private LocalDateTime createdAt;
}
