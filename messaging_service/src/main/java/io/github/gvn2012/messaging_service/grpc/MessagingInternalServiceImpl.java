package io.github.gvn2012.messaging_service.grpc;

import io.github.gvn2012.grpc.messaging.ConvIdRequest;
import io.github.gvn2012.grpc.messaging.ConvUserRequest;
import io.github.gvn2012.grpc.messaging.CreateConversationRequest;
import io.github.gvn2012.grpc.messaging.CreateGroupRequest;
import io.github.gvn2012.grpc.messaging.EditMessageRequest;
import io.github.gvn2012.grpc.messaging.EmptyMsgResponse;
import io.github.gvn2012.grpc.messaging.IdUserRequest;
import io.github.gvn2012.grpc.messaging.ManageGroupMembersRequest;
import io.github.gvn2012.grpc.messaging.MessagingInternalServiceGrpc;
import io.github.gvn2012.grpc.messaging.ParticipantsResponse;
import io.github.gvn2012.grpc.messaging.PersistCallLogRequest;
import io.github.gvn2012.grpc.messaging.ProcessMessageRequest;
import io.github.gvn2012.grpc.messaging.ProcessMessageResponse;
import io.github.gvn2012.grpc.messaging.TypingRequest;
import io.github.gvn2012.grpc.messaging.UpdateGroupRequest;
import io.github.gvn2012.grpc.messaging.UserIdRequest;
import io.github.gvn2012.messaging_service.dtos.GroupCreateRequest;
import io.github.gvn2012.messaging_service.dtos.GroupMemberRequest;
import io.github.gvn2012.messaging_service.dtos.GroupUpdateRequest;
import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.models.MediaItem;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import io.github.gvn2012.shared.dtos.CallSignal;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class MessagingInternalServiceImpl extends MessagingInternalServiceGrpc.MessagingInternalServiceImplBase {

    private final IMessagingService messagingService;

    @Override
    public void processMessage(ProcessMessageRequest request, StreamObserver<ProcessMessageResponse> responseObserver) {
        MessageRequest msgRequest = new MessageRequest();
        msgRequest.setId(request.getId().isEmpty() ? null : request.getId());
        msgRequest.setBatchId(request.getBatchId().isEmpty() ? null : request.getBatchId());
        msgRequest.setConversationId(request.getConversationId());
        msgRequest.setSenderId(request.getSenderId());
        msgRequest.setContent(request.getContent());

        if (!request.getType().isEmpty()) {
            msgRequest.setType(io.github.gvn2012.messaging_service.models.enums.MessageType.valueOf(request.getType()));
        }

        if (request.getMediaItemsCount() > 0) {
            List<MediaItem> items = new ArrayList<>();
            for (var proto : request.getMediaItemsList()) {
                MediaItem item = MediaItem.builder()
                        .id(proto.getId().isEmpty() ? null : proto.getId())
                        .batchId(proto.getBatchId().isEmpty() ? null : proto.getBatchId())
                        .conversationId(proto.getConversationId().isEmpty() ? null : proto.getConversationId())
                        .fileName(proto.getFileName().isEmpty() ? null : proto.getFileName())
                        .contentType(proto.getContentType().isEmpty() ? null : proto.getContentType())
                        .mediaType(proto.getMediaType().isEmpty() ? null : proto.getMediaType())
                        .status(proto.getStatus().isEmpty() ? null : proto.getStatus())
                        .build();
                items.add(item);
            }
            msgRequest.setMediaItems(items);
        }

        messagingService.processMessage(msgRequest);
        responseObserver.onNext(ProcessMessageResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void editMessage(EditMessageRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.editMessage(request.getMessageId(), request.getNewContent(), request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteMessage(IdUserRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.deleteMessage(request.getId(), request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void recallMessage(IdUserRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.recallMessage(request.getId(), request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void markAsDelivered(IdUserRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.markAsDelivered(request.getId(), request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void markAllAsDelivered(UserIdRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.markAllAsDelivered(request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void markAsSeen(ConvUserRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.markAsSeen(request.getConversationId(), request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteConversation(ConvUserRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.deleteConversation(request.getConversationId(), request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void createConversation(CreateConversationRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.createConversation(
                new ArrayList<>(request.getParticipantIdsList()),
                request.getName(), request.getType());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        GroupCreateRequest groupReq = new GroupCreateRequest();
        groupReq.setName(request.getName());
        groupReq.setParticipantIds(new ArrayList<>(request.getMemberIdsList()));
        messagingService.createGroupConversation(groupReq, request.getCreatorId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateGroup(UpdateGroupRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        GroupUpdateRequest updateReq = new GroupUpdateRequest();
        updateReq.setConversationId(request.getConversationId());
        if (!request.getName().isEmpty()) updateReq.setName(request.getName());
        if (!request.getAvatarUrl().isEmpty()) updateReq.setGroupAvatar(request.getAvatarUrl());
        messagingService.updateGroupConversation(updateReq, request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void manageGroupMembers(ManageGroupMembersRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        for (String userId : request.getUserIdsList()) {
            GroupMemberRequest memberReq = new GroupMemberRequest();
            memberReq.setConversationId(request.getConversationId());
            memberReq.setAction(request.getAction());
            memberReq.setUserId(userId);
            messagingService.manageGroupMembers(memberReq, request.getAdminId());
        }
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void leaveGroup(ConvUserRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.leaveGroupConversation(request.getConversationId(), request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void broadcastTyping(TypingRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        messagingService.broadcastTyping(request.getConversationId(), request.getUserId(), request.getIsTyping());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getConversationParticipants(ConvIdRequest request, StreamObserver<ParticipantsResponse> responseObserver) {
        List<String> participants = messagingService.getConversationParticipantIds(request.getConversationId());
        responseObserver.onNext(ParticipantsResponse.newBuilder().addAllParticipantIds(participants).build());
        responseObserver.onCompleted();
    }

    @Override
    public void persistCallLog(PersistCallLogRequest request, StreamObserver<EmptyMsgResponse> responseObserver) {
        CallSignal signal = CallSignal.builder()
                .type(request.getType())
                .senderId(request.getSenderId().isEmpty() ? null : request.getSenderId())
                .recipientId(request.getRecipientId().isEmpty() ? null : request.getRecipientId())
                .callMode(request.getCallMode())
                .conversationId(request.getConversationId())
                .callId(request.getCallId().isEmpty() ? null : request.getCallId())
                .callStartTime(request.getCallStartTime() == 0 ? null : request.getCallStartTime())
                .build();

        // Extract duration from callStartTime if available
        if (signal.getCallStartTime() != null && signal.getCallStartTime() > 0) {
            long now = System.currentTimeMillis();
            int duration = (int) ((now - signal.getCallStartTime()) / 1000);
            signal.setPayload(Map.of("duration", duration));
        }

        messagingService.persistCallLog(signal, request.getUserId());
        responseObserver.onNext(EmptyMsgResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
