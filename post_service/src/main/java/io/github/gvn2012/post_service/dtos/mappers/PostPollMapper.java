package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.requests.PollOptionRequest;
import io.github.gvn2012.post_service.dtos.requests.PostPollRequest;
import io.github.gvn2012.post_service.dtos.responses.PollOptionResponse;
import io.github.gvn2012.post_service.dtos.responses.PostPollResponse;
import io.github.gvn2012.post_service.entities.PollOption;
import io.github.gvn2012.post_service.entities.PostPoll;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostPollMapper {

    @Mapping(target = "postId", source = "post.id")
    PostPollResponse toResponse(PostPoll poll);

    PostPoll toEntity(PostPollRequest request);

    PollOptionResponse toOptionResponse(PollOption option);

    PollOption toOptionEntity(PollOptionRequest request);
}
