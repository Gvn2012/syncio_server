package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.PostEventRequest;
import io.github.gvn2012.post_service.dtos.responses.PostEventParticipantResponse;
import io.github.gvn2012.post_service.dtos.responses.PostEventResponse;
import io.github.gvn2012.post_service.entities.PostEvent;
import io.github.gvn2012.post_service.entities.PostEventParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostEventMapper {

    @Mapping(target = "postId", source = "post.id")
    PostEventResponse toResponse(PostEvent event);

    PostEvent toEntity(PostEventRequest request);

    @Mapping(target = "eventId", source = "event.postId")
    PostEventParticipantResponse toParticipantResponse(PostEventParticipant participant);
}
