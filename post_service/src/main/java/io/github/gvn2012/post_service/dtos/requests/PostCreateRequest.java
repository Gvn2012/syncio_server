package io.github.gvn2012.post_service.dtos.requests;

import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 10000, message = "Content too long")
    private String content;

    private String contentHtml;

    @Size(max = 500, message = "Excerpt too long")
    private String excerpt;

    private String language;

    @NotNull(message = "Post category is required")
    @Builder.Default
    private PostCategory postCategory = PostCategory.NORMAL;

    @NotNull(message = "Visibility is required")
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;

    private UUID orgId;
    private List<MediaAttachmentRequest> attachments;
    private List<UUID> mentions;
    private List<String> tags;
    private String metadata;

    private PostEventRequest event;
    private PostPollRequest poll;
    private PostTaskRequest task;
    private PostAnnouncementRequest announcement;
}
