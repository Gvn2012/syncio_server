package io.github.gvn2012.image_uploading_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageMediaUploadRequest {
    private String fileName;
    private String fileContentType;
    private Long size;
    private String conversationId;
    private String mediaType;
    private String batchId;
    private String senderId;
}
