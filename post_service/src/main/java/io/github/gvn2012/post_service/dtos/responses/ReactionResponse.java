package io.github.gvn2012.post_service.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReactionResponse {
    private UUID id;
    private UUID postId;
    private UUID commentId;
    private UUID userId;
    private String reactionCode;
    private LocalDateTime createdAt;
}
