package io.github.gvn2012.messaging_service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.gvn2012.messaging_service.models.Message;
import io.github.gvn2012.messaging_service.models.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private MessageType type;
    private String mediaId;
    private String mediaUrl;
    private Long mediaSize;
    private String mediaContentType;
    private LocalDateTime timestamp;
    @JsonProperty("isEdited")
    private boolean isEdited;
    @JsonProperty("isRecalled")
    private boolean isRecalled;
    private Map<String, Message.StatusInfo> status;
}
