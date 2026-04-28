package io.github.gvn2012.messaging_service.dtos;

import java.util.List;

import io.github.gvn2012.messaging_service.models.MediaItem;
import io.github.gvn2012.messaging_service.models.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private String id;
    private String conversationId;
    private String batchId;
    private String senderId;
    private String content;
    private MessageType type;
    private String mediaId;
    private String mediaUrl;
    private Long mediaSize;
    private String mediaContentType;
    private List<MediaItem> mediaItems;
}
