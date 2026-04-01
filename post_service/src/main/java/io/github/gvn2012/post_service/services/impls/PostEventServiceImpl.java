package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.PostEventMapper;
import io.github.gvn2012.post_service.dtos.requests.PostEventRequest;
import io.github.gvn2012.post_service.dtos.responses.PostEventParticipantResponse;
import io.github.gvn2012.post_service.dtos.responses.PostEventResponse;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostEvent;
import io.github.gvn2012.post_service.entities.PostEventParticipant;
import io.github.gvn2012.post_service.entities.enums.EventStatus;
import io.github.gvn2012.post_service.entities.enums.EventParticipantStatus;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostEventParticipantRepository;
import io.github.gvn2012.post_service.repositories.PostEventRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostEventServiceImpl implements IPostEventService {

    private final PostEventRepository eventRepository;
    private final PostEventParticipantRepository participantRepository;
    private final PostRepository postRepository;
    private final PostEventMapper eventMapper;
    private final UserValidationService userValidationService;

    @Override
    @Transactional
    public PostEventResponse createEvent(UUID postId, PostEventRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        
        userValidationService.validateUserCanInteract(post.getAuthorId());
        
        post.setPostCategory(PostCategory.EVENT);
        postRepository.save(post);
        
        PostEvent event = eventMapper.toEntity(request);
        event.setPost(post);
        event.setPostId(post.getId());
        event.setStatus(EventStatus.SCHEDULED);
        
        PostEvent saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    public PostEventResponse getEventByPostId(UUID postId) {
        PostEvent event = eventRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Event not found for post: " + postId));
        return eventMapper.toResponse(event);
    }

    @Override
    public List<PostEventResponse> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable)
                .stream().map(eventMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void respondToEvent(UUID eventId, UUID userId, String status) {
        userValidationService.validateUserCanInteract(userId);
        PostEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        
        PostEventParticipant participant = participantRepository.findByEventPostIdAndUserId(eventId, userId)
                .orElseGet(() -> {
                    PostEventParticipant p = new PostEventParticipant();
                    p.setEvent(event);
                    p.setUserId(userId);
                    return p;
                });
        
        participant.setStatus(EventParticipantStatus.valueOf(status.toUpperCase()));
        participant.setRespondedAt(LocalDateTime.now());
        participantRepository.save(participant);
        
        // Update counts
        event.setAcceptedCount((int) participantRepository.countByEventPostIdAndStatus(eventId, EventParticipantStatus.ACCEPTED));
        event.setDeclinedCount((int) participantRepository.countByEventPostIdAndStatus(eventId, EventParticipantStatus.DECLINED));
        event.setTentativeCount((int) participantRepository.countByEventPostIdAndStatus(eventId, EventParticipantStatus.TENTATIVE));
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void cancelEvent(UUID eventId) {
        PostEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Override
    public List<PostEventParticipantResponse> getParticipants(UUID eventId) {
        return participantRepository.findByEventPostId(eventId)
                .stream().map(eventMapper::toParticipantResponse).toList();
    }
}
