package io.github.gvn2012.post_service.dtos.responses;

import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private UUID id;
    private UUID authorId;
    private UserSummaryResponse authorInfo;
    private String viewerReaction;
    private boolean isSharedByViewer;
    private UUID orgId;
    private String content;
    private String contentHtml;
    private String excerpt;
    private String language;
    private PostCategory postCategory;
    private PostStatus status;
    private PostVisibility visibility;
    
    private BigInteger viewCount;
    private Integer shareCount;
    private Integer reactionCount;
    private Integer commentCount;

    private PostModerationStatus moderationStatus;
    private Boolean isShared;
    private Boolean isPinned;
    private UUID parentPostId;
    
    private List<MediaAttachmentResponse> attachments;
    private List<UUID> mentions;
    private List<String> tags;
    private String metadata;

    private PostEventResponse event;
    private PostPollResponse poll;
    private PostTaskResponse task;
    private PostAnnouncementResponse announcement;
    
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
