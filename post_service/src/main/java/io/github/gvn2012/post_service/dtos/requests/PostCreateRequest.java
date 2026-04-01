package io.github.gvn2012.post_service.dtos.requests;

import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    private String content;
    private String contentHtml;
    private String excerpt;
    private String language;
    @Builder.Default
    private PostCategory postCategory = PostCategory.NORMAL;
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;
    private UUID orgId;
    private List<MediaAttachmentRequest> attachments;
    private List<UUID> mentions;
    private List<String> tags;
    private String metadata;
}
