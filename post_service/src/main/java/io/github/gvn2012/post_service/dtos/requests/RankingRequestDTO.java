package io.github.gvn2012.post_service.dtos.requests;

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
public class RankingRequestDTO {
    private UUID userId;
    private List<PostFeatureDTO> candidates;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostFeatureDTO {
        private UUID postId;
        private UUID authorId;
        private Double authorAffinity;
        private Double velocityScore;
        private Double recencyHours;
        private String category;
        private Integer mediaCount;
    }
}
