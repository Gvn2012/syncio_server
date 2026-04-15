package io.github.gvn2012.post_service.services.subtypes;

import io.github.gvn2012.post_service.dtos.mappers.PostAnnouncementMapper;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostAnnouncement;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.repositories.PostAnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnnouncementSubtypeProcessor implements PostSubtypeProcessor {

    private final PostAnnouncementMapper postAnnouncementMapper;
    private final PostAnnouncementRepository postAnnouncementRepository;

    @Override
    public PostCategory supportedCategory() {
        return PostCategory.ANNOUNCEMENT;
    }

    @Override
    public void process(Post post, PostCreateRequest request) {
        if (request.getAnnouncement() == null) return;
        PostAnnouncement announcement = postAnnouncementMapper.toEntity(request.getAnnouncement());
        announcement.setPost(post);
        PostAnnouncement saved = postAnnouncementRepository.save(announcement);
        post.setAnnouncement(saved);
    }
}
