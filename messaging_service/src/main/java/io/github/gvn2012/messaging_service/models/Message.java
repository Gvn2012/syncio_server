package io.github.gvn2012.messaging_service.models;

import io.github.gvn2012.messaging_service.models.enums.MessageStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    
    private boolean isEdited;
    private boolean isDeleted;
    
    // UserId -> DeleteTimestamp (Unidirectional deletion)
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
