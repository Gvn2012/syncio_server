package io.github.gvn2012.shared.kafka_events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadInitiatedEvent {
    private String mediaId;
    private String batchId;
    private String conversationId;
    private String uploadUrl;
    private String senderId;
    private String mediaType;
    private String fileName;
    private String contentType;
    private Long size;
    private Map<String, Object> metadata;
}
