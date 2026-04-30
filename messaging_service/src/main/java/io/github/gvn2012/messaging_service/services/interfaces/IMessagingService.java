package io.github.gvn2012.messaging_service.services.interfaces;

import io.github.gvn2012.messaging_service.dtos.ConversationResponse;
import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.dtos.MessageResponse;
import io.github.gvn2012.messaging_service.dtos.CallSignal;
import io.github.gvn2012.messaging_service.dtos.GroupCreateRequest;
import io.github.gvn2012.messaging_service.dtos.GroupMemberRequest;
import io.github.gvn2012.messaging_service.dtos.GroupUpdateRequest;
import io.github.gvn2012.messaging_service.dtos.GroupSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface IMessagingService {
    void processMessage(MessageRequest request);

    void persistCallLog(CallSignal signal, String userId);

    void editMessage(String messageId, String newContent, String userId);

    void deleteMessage(String messageId, String userId);

    void recallMessage(String messageId, String userId);

    void markAsDelivered(String messageId, String userId);

    void markAllAsDelivered(String userId);

    void markAsSeen(String conversationId, String userId);

    void deleteConversation(String conversationId, String userId);

    void createConversation(List<String> participantIds, String name, String type);

    void createGroupConversation(GroupCreateRequest request, String creatorId);

    void updateGroupConversation(GroupUpdateRequest request, String userId);

    void manageGroupMembers(GroupMemberRequest request, String adminId);

    void leaveGroupConversation(String conversationId, String userId);

    void broadcastTyping(String conversationId, String userId, boolean isTyping);

    void broadcastCallSignal(CallSignal signal, String userId);

    GroupSummaryResponse getGroupSummary(String conversationId, String userId);

    List<ConversationResponse> getConversations(String userId);

    List<MessageResponse> getMessageHistory(String conversationId, String userId, LocalDateTime before,
            int size);

    long getTotalUnreadCount(String userId);
}
