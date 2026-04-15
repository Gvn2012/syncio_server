package io.github.gvn2012.post_service.dtos.requests;

import io.github.gvn2012.post_service.entities.enums.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAttachmentRequest {
    private String externalId;
    private String url;
    private String caption;
    private String altText;
    private Byte position;
    private AttachmentType type;
    private String mimeType;
    private Long sizeBytes;
    private String fileName;
    private Integer width;
    private Integer height;
    private Double duration;
}
