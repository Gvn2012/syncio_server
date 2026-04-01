package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.responses.ReactionResponse;
import io.github.gvn2012.post_service.entities.PostReaction;
import org.springframework.stereotype.Component;

@Component
public class ReactionMapper {

    public ReactionResponse toResponse(PostReaction reaction) {
        if (reaction == null) return null;
        return ReactionResponse.builder()
                .id(reaction.getId())
                .postId(reaction.getPost() != null ? reaction.getPost().getId() : null)
                .commentId(null) // Entity doesn't have comment link apparently, let's leave it null for now
                .userId(reaction.getUserId())
                .reactionCode(reaction.getReactionType() != null ? reaction.getReactionType().getCode() : null)
                .reactionEmoji(reaction.getReactionType() != null ? reaction.getReactionType().getEmoji() : null)
                .createdAt(reaction.getCreatedAt())
                .build();
    }
}
