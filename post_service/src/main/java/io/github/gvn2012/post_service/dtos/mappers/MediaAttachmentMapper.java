package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.MediaAttachmentRequest;
import io.github.gvn2012.post_service.dtos.responses.MediaAttachmentResponse;
import io.github.gvn2012.post_service.entities.PostMediaAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MediaAttachmentMapper {

    public abstract MediaAttachmentResponse toResponse(PostMediaAttachment attachment);

    public abstract PostMediaAttachment toEntity(MediaAttachmentRequest request);
}
