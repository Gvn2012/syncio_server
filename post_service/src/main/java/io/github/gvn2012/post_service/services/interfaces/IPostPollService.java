package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.requests.PostPollRequest;
import io.github.gvn2012.post_service.dtos.responses.PostPollResponse;

import java.util.UUID;

public interface IPostPollService {
    PostPollResponse createPoll(UUID postId, PostPollRequest request);
    PostPollResponse getPollByPostId(UUID postId);
    void voteOnPoll(UUID pollId, UUID optionId, UUID userId);
    PostPollResponse getPollResults(UUID pollId);
    void closePoll(UUID pollId, UUID userId);
}
