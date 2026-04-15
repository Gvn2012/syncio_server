package io.github.gvn2012.post_service.dtos.responses;

import io.github.gvn2012.post_service.entities.enums.AttachmentType;
import io.github.gvn2012.post_service.entities.enums.AttachmentUploadStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MediaAttachmentResponse {
    private UUID id;
    private String url;
    private String uploadUrl;
    private String caption;
    private String altText;
    private Byte position;
    private AttachmentType type;
    private String mimeType;
    private AttachmentUploadStatus uploadStatus;
    private Integer width;
    private Integer height;
    private Double duration;
    private LocalDateTime uploadedAt;
}
