package io.github.gvn2012.post_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentPagedResponse {
    private List<CommentResponse> comments;
    private long totalCount;
}
