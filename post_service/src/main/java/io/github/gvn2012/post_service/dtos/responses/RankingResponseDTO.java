package io.github.gvn2012.post_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankingResponseDTO {
    private UUID userId;
    private List<RankedPostDTO> rankedCandidates;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RankedPostDTO {
        private UUID postId;
        private Double score;
    }
}
