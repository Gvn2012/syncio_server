package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.PostTaskRequest;
import io.github.gvn2012.post_service.dtos.responses.PostTaskResponse;
import io.github.gvn2012.post_service.entities.PostTask;
import io.github.gvn2012.post_service.entities.PostTaskAssignee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostTaskMapper {

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "assignees", source = "assignees", qualifiedByName = "mapAssigneesToIds")
    PostTaskResponse toResponse(PostTask task);

    @Mapping(target = "assignees", ignore = true)
    PostTask toEntity(PostTaskRequest request);

    @Named("mapAssigneesToIds")
    default List<UUID> mapAssigneesToIds(Set<PostTaskAssignee> assignees) {
        if (assignees == null) return List.of();
        return assignees.stream()
                .map(PostTaskAssignee::getUserId)
                .collect(Collectors.toList());
    }
}
