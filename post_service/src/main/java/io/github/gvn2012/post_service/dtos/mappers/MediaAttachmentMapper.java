package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.MediaAttachmentRequest;
import io.github.gvn2012.post_service.dtos.responses.MediaAttachmentResponse;
import io.github.gvn2012.post_service.entities.PostMediaAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaAttachmentMapper {

    MediaAttachmentResponse toResponse(PostMediaAttachment attachment);

    PostMediaAttachment toEntity(MediaAttachmentRequest request);
}
