package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.requests.PostAnnouncementRequest;
import io.github.gvn2012.post_service.dtos.responses.PostAnnouncementResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public interface IPostAnnouncementService {
    PostAnnouncementResponse createAnnouncement(@NonNull UUID postId, PostAnnouncementRequest data);

    PostAnnouncementResponse getAnnouncementByPostId(@NonNull UUID postId);

    void markAsRead(@NonNull UUID announcementId, UUID userId);

    void pinAnnouncement(@NonNull UUID announcementId, UUID userId);

    void unpinAnnouncement(@NonNull UUID announcementId, UUID userId);

    List<PostAnnouncementResponse> getActiveAnnouncements(Pageable pageable);
}
