package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.MediaAttachmentRequest;
import io.github.gvn2012.post_service.dtos.responses.MediaAttachmentResponse;
import io.github.gvn2012.post_service.entities.PostMediaAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MediaAttachmentMapper {

    @Value("${syncio.gateway.host:http://syncio.site}")
    protected String gatewayHost;

    @Mapping(target = "url", source = "attachment", qualifiedByName = "mapProxyUrl")
    public abstract MediaAttachmentResponse toResponse(PostMediaAttachment attachment);

    @Named("mapProxyUrl")
    protected String mapProxyUrl(PostMediaAttachment attachment) {
        if (attachment == null) return null;
        if (attachment.getObjectPath() == null || attachment.getObjectPath().isBlank()) {
            return attachment.getUrl();
        }
        return String.format("%s/api/v1/upload/view?path=%s", gatewayHost, attachment.getObjectPath());
    }

    public abstract PostMediaAttachment toEntity(MediaAttachmentRequest request);
}
