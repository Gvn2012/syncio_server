package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostMention;
import io.github.gvn2012.post_service.entities.PostTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {
            MediaAttachmentMapper.class,
            PostEventMapper.class,
            PostPollMapper.class,
            PostTaskMapper.class,
            PostAnnouncementMapper.class
        })
public interface PostMapper {

    @Mapping(target = "parentPostId", source = "parentPost.id")
    @Mapping(target = "mentions", source = "mentions", qualifiedByName = "mapMentionsToIds")
    @Mapping(target = "tags", source = "postTags", qualifiedByName = "mapTagsToStrings")
    PostResponse toResponse(Post post);

    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "mentions", ignore = true)
    @Mapping(target = "postTags", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "poll", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "announcement", ignore = true)
    Post toEntity(PostCreateRequest request);

    @Named("mapMentionsToIds")
    default List<UUID> mapMentionsToIds(Set<PostMention> mentions) {
        if (mentions == null) return List.of();
        return mentions.stream()
                .map(PostMention::getUserId)
                .collect(Collectors.toList());
    }

    @Named("mapTagsToStrings")
    default List<String> mapTagsToStrings(Set<PostTag> postTags) {
        if (postTags == null) return List.of();
        return postTags.stream()
                .map(pt -> pt.getTag().getName())
                .collect(Collectors.toList());
    }
}
