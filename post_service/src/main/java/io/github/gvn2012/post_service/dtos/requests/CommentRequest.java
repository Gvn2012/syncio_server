package io.github.gvn2012.post_service.dtos.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CommentRequest {
    private String content;
    private UUID parentCommentId;
}
