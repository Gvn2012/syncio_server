package io.github.gvn2012.search_service.dtos.responses;

import io.github.gvn2012.search_service.documents.PostIndex;
import io.github.gvn2012.search_service.documents.UserIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversalSearchResponse {
    private List<UserIndex> people;
    private List<PostIndex> posts;
    private long totalPeople;
    private long totalPosts;
    private long processingTimeMs;
}
