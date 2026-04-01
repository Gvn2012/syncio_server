package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.requests.PostEventRequest;
import io.github.gvn2012.post_service.dtos.responses.PostEventParticipantResponse;
import io.github.gvn2012.post_service.dtos.responses.PostEventResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostEventService {
    PostEventResponse createEvent(UUID postId, PostEventRequest request);
    PostEventResponse getEventByPostId(UUID postId);
    List<PostEventResponse> getUpcomingEvents(Pageable pageable);
    void respondToEvent(UUID eventId, UUID userId, String status);
    void cancelEvent(UUID eventId, UUID userId);
    List<PostEventParticipantResponse> getParticipants(UUID eventId);
}
