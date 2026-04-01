package io.github.gvn2012.post_service.dtos.requests;

import io.github.gvn2012.post_service.entities.enums.PostVisibility;
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
public class PostUpdateRequest {
    private String content;
    private String contentHtml;
    private String excerpt;
    private String language;
    private PostVisibility visibility;
    private List<MediaAttachmentRequest> attachments;
    private List<UUID> mentions;
    private List<String> tags;
    private String metadata;
}
