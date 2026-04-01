package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.PostAnnouncementRequest;
import io.github.gvn2012.post_service.dtos.responses.PostAnnouncementResponse;
import io.github.gvn2012.post_service.entities.PostAnnouncement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostAnnouncementMapper {

    @Mapping(target = "postId", source = "post.id")
    PostAnnouncementResponse toResponse(PostAnnouncement announcement);

    PostAnnouncement toEntity(PostAnnouncementRequest request);
}
