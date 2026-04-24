package io.github.gvn2012.post_service.dtos.responses;

import io.github.gvn2012.post_service.entities.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReactionGroupResponse {
    private ReactionType reactionType;
    private long count;
    private List<ReactorSummaryResponse> reactors;
}
