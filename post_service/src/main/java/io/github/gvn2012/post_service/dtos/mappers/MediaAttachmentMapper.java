package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.MediaAttachmentRequest;
import io.github.gvn2012.post_service.dtos.responses.MediaAttachmentResponse;
import io.github.gvn2012.post_service.entities.PostMediaAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaAttachmentMapper {

    @Mapping(target = "height", source = "length")
    MediaAttachmentResponse toResponse(PostMediaAttachment attachment);

    @Mapping(target = "length", source = "height")
    PostMediaAttachment toEntity(MediaAttachmentRequest request);
}
