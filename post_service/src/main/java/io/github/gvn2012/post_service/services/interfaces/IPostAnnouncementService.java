package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.requests.PostAnnouncementRequest;
import io.github.gvn2012.post_service.dtos.responses.PostAnnouncementResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostAnnouncementService {
    PostAnnouncementResponse createAnnouncement(UUID postId, PostAnnouncementRequest data);
    PostAnnouncementResponse getAnnouncementByPostId(UUID postId);
    void markAsRead(UUID announcementId, UUID userId);
    void pinAnnouncement(UUID announcementId);
    void unpinAnnouncement(UUID announcementId);
    List<PostAnnouncementResponse> getActiveAnnouncements(Pageable pageable);
}
