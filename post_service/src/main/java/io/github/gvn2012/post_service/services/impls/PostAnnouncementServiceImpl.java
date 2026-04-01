package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.PostAnnouncementMapper;
import io.github.gvn2012.post_service.dtos.requests.PostAnnouncementRequest;
import io.github.gvn2012.post_service.dtos.responses.PostAnnouncementResponse;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostAnnouncement;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostAnnouncementRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostAnnouncementService;
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
public class PostAnnouncementServiceImpl implements IPostAnnouncementService {

    private final PostAnnouncementRepository announcementRepository;
    private final PostRepository postRepository;
    private final PostAnnouncementMapper announcementMapper;
    private final UserValidationService userValidationService;

    @Override
    @Transactional
    public PostAnnouncementResponse createAnnouncement(UUID postId, PostAnnouncementRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        
        userValidationService.validateUserCanInteract(post.getAuthorId());
        
        post.setPostCategory(PostCategory.ANNOUNCEMENT);
        postRepository.save(post);
        
        PostAnnouncement announcement = announcementMapper.toEntity(request);
        announcement.setPost(post);
        PostAnnouncement saved = announcementRepository.save(announcement);
        return announcementMapper.toResponse(saved);
    }

    @Override
    public PostAnnouncementResponse getAnnouncementByPostId(UUID postId) {
        PostAnnouncement announcement = announcementRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Announcement not found for post: " + postId));
        return announcementMapper.toResponse(announcement);
    }

    @Override
    @Transactional
    public void markAsRead(UUID announcementId, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        PostAnnouncement ann = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Announcement not found: " + announcementId));
        ann.setReadCount(ann.getReadCount() + 1);
        announcementRepository.save(ann);
    }

    @Override
    @Transactional
    public void pinAnnouncement(UUID announcementId, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        PostAnnouncement ann = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Announcement not found: " + announcementId));
        ann.setIsPinned(true);
        announcementRepository.save(ann);
    }

    @Override
    @Transactional
    public void unpinAnnouncement(UUID announcementId, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        PostAnnouncement ann = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Announcement not found: " + announcementId));
        ann.setIsPinned(false);
        ann.setPinnedUntil(null);
        announcementRepository.save(ann);
    }

    @Override
    public List<PostAnnouncementResponse> getActiveAnnouncements(Pageable pageable) {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now(), pageable)
                .stream()
                .map(announcementMapper::toResponse)
                .collect(Collectors.toList());
    }
}
