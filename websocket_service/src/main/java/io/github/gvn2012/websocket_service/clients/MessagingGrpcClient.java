package io.github.gvn2012.websocket_service.clients;

import io.github.gvn2012.grpc.messaging.ConvIdRequest;
import io.github.gvn2012.grpc.messaging.EditMessageRequest;
import io.github.gvn2012.grpc.messaging.IdUserRequest;
import io.github.gvn2012.grpc.messaging.ConvUserRequest;
import io.github.gvn2012.grpc.messaging.CreateConversationRequest;
import io.github.gvn2012.grpc.messaging.CreateGroupRequest;
import io.github.gvn2012.grpc.messaging.ManageGroupMembersRequest;
import io.github.gvn2012.grpc.messaging.MessagingInternalServiceGrpc;
import io.github.gvn2012.grpc.messaging.PersistCallLogRequest;
import io.github.gvn2012.grpc.messaging.ProcessMessageRequest;
import io.github.gvn2012.grpc.messaging.TypingRequest;
import io.github.gvn2012.grpc.messaging.UpdateGroupRequest;
import io.github.gvn2012.grpc.messaging.UserIdRequest;
import io.github.gvn2012.grpc.messaging.ParticipantsResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MessagingGrpcClient {

    @GrpcClient("messaging_service")
    private MessagingInternalServiceGrpc.MessagingInternalServiceBlockingStub messagingStub;

    public void processMessage(ProcessMessageRequest request) {
        try {
            messagingStub.processMessage(request);
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.ProcessMessage failed", e);
            throw new RuntimeException("Failed to process message", e);
        }
    }

    public void editMessage(String messageId, String newContent, String userId) {
        try {
            messagingStub.editMessage(EditMessageRequest.newBuilder()
                    .setMessageId(messageId).setNewContent(newContent).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.EditMessage failed", e);
        }
    }

    public void deleteMessage(String messageId, String userId) {
        try {
            messagingStub.deleteMessage(IdUserRequest.newBuilder()
                    .setId(messageId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.DeleteMessage failed", e);
        }
    }

    public void recallMessage(String messageId, String userId) {
        try {
            messagingStub.recallMessage(IdUserRequest.newBuilder()
                    .setId(messageId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.RecallMessage failed", e);
        }
    }

    public void markAsDelivered(String messageId, String userId) {
        try {
            messagingStub.markAsDelivered(IdUserRequest.newBuilder()
                    .setId(messageId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.MarkAsDelivered failed", e);
        }
    }

    public void markAllAsDelivered(String userId) {
        try {
            messagingStub.markAllAsDelivered(UserIdRequest.newBuilder().setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.MarkAllAsDelivered failed", e);
        }
    }

    public void markAsSeen(String conversationId, String userId) {
        try {
            messagingStub.markAsSeen(ConvUserRequest.newBuilder()
                    .setConversationId(conversationId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.MarkAsSeen failed", e);
        }
    }

    public void deleteConversation(String conversationId, String userId) {
        try {
            messagingStub.deleteConversation(ConvUserRequest.newBuilder()
                    .setConversationId(conversationId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.DeleteConversation failed", e);
        }
    }

    public void createConversation(List<String> participantIds, String name, String type) {
        try {
            messagingStub.createConversation(CreateConversationRequest.newBuilder()
                    .addAllParticipantIds(participantIds).setName(name).setType(type).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.CreateConversation failed", e);
        }
    }

    public void createGroup(String name, String avatarUrl, List<String> memberIds, String creatorId) {
        try {
            CreateGroupRequest.Builder builder = CreateGroupRequest.newBuilder()
                    .setName(name).addAllMemberIds(memberIds).setCreatorId(creatorId);
            if (avatarUrl != null) builder.setAvatarUrl(avatarUrl);
            messagingStub.createGroup(builder.build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.CreateGroup failed", e);
        }
    }

    public void updateGroup(String conversationId, String name, String avatarUrl, String userId) {
        try {
            UpdateGroupRequest.Builder builder = UpdateGroupRequest.newBuilder()
                    .setConversationId(conversationId).setUserId(userId);
            if (name != null) builder.setName(name);
            if (avatarUrl != null) builder.setAvatarUrl(avatarUrl);
            messagingStub.updateGroup(builder.build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.UpdateGroup failed", e);
        }
    }

    public void manageGroupMembers(String conversationId, String action, List<String> userIds, String adminId) {
        try {
            messagingStub.manageGroupMembers(ManageGroupMembersRequest.newBuilder()
                    .setConversationId(conversationId).setAction(action)
                    .addAllUserIds(userIds).setAdminId(adminId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.ManageGroupMembers failed", e);
        }
    }

    public void leaveGroup(String conversationId, String userId) {
        try {
            messagingStub.leaveGroup(ConvUserRequest.newBuilder()
                    .setConversationId(conversationId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.LeaveGroup failed", e);
        }
    }

    public void broadcastTyping(String conversationId, String userId, boolean isTyping) {
        try {
            messagingStub.broadcastTyping(TypingRequest.newBuilder()
                    .setConversationId(conversationId).setUserId(userId).setIsTyping(isTyping).build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.BroadcastTyping failed", e);
        }
    }

    public List<String> getConversationParticipants(String conversationId) {
        try {
            ParticipantsResponse response = messagingStub.getConversationParticipants(
                    ConvIdRequest.newBuilder().setConversationId(conversationId).build());
            return response.getParticipantIdsList();
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.GetConversationParticipants failed", e);
            return List.of();
        }
    }

    public void persistCallLog(String type, String senderId, String recipientId, String callMode,
            String conversationId, String callId, Long callStartTime, String userId) {
        try {
            PersistCallLogRequest.Builder builder = PersistCallLogRequest.newBuilder()
                    .setUserId(userId);
            if (type != null) builder.setType(type);
            if (senderId != null) builder.setSenderId(senderId);
            if (recipientId != null) builder.setRecipientId(recipientId);
            if (callMode != null) builder.setCallMode(callMode);
            if (conversationId != null) builder.setConversationId(conversationId);
            if (callId != null) builder.setCallId(callId);
            if (callStartTime != null) builder.setCallStartTime(callStartTime);
            messagingStub.persistCallLog(builder.build());
        } catch (Exception e) {
            log.error("gRPC call to messaging_service.PersistCallLog failed", e);
        }
    }
}
