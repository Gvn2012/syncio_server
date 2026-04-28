package io.github.gvn2012.messaging_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "media_items")
public class MediaItem {
    @Id
    private String id;

    private String batchId;
    private String conversationId;

    private String fileName;
    private String contentType;
    private String mediaType;
    private String status;

    private String bucketName;
    private Map<String, Object> metadata;

    private String uploadUrl;
    private String downloadUrl;

    private Long size;
    private Integer duration;
    private String resolution;

    private Instant createdAt;
    private Instant updatedAt;
}
