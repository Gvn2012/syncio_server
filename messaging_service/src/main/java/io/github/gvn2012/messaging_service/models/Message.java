package io.github.gvn2012.messaging_service.models;

import io.github.gvn2012.messaging_service.models.enums.MessageStatusType;
import io.github.gvn2012.messaging_service.models.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {
    @Id
    private String id;

    @Indexed
    private String conversationId;

    private String senderId;
    private String content;
    private LocalDateTime timestamp;
    private LocalDateTime updatedAt;

    private MessageType type;
    private String mediaId;
    private String mediaUrl;
    private Long mediaSize;
    private String mediaContentType;

    @JsonProperty("isEdited")
    private boolean isEdited;
    @JsonProperty("isRecalled")
    private boolean isRecalled;

    private Map<String, LocalDateTime> deletedAtPerUser;

    private Map<String, StatusInfo> status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusInfo {
        private MessageStatusType status;
        private LocalDateTime updateTime;
    }
}
