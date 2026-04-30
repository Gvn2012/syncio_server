package io.github.gvn2012.messaging_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantPreview {
    private String userId;
    private String displayName;
    private String profilePictureUrl;
}
