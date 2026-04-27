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
public class UserProfileSummary {
    private UUID userId;
    private String username;
    private String displayName;
    private String profilePictureUrl;
    private String profilePicturePath;
}
