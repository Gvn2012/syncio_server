package io.github.gvn2012.messaging_service.services.interfaces;

import io.github.gvn2012.messaging_service.dtos.MessageRequest;

public interface IMessagingService {
    void processMessage(MessageRequest request);
    void markAsDelivered(String messageId, String userId);
    void markAsSeen(String conversationId, String userId);
}
