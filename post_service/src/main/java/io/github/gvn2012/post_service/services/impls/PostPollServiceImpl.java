package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.PostPollMapper;
import io.github.gvn2012.post_service.dtos.requests.PollOptionRequest;
import io.github.gvn2012.post_service.dtos.requests.PostPollRequest;
import io.github.gvn2012.post_service.dtos.responses.PostPollResponse;
import io.github.gvn2012.post_service.entities.PollOption;
import io.github.gvn2012.post_service.entities.PollVote;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostPoll;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.exceptions.BadRequestException;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PollOptionRepository;
import io.github.gvn2012.post_service.repositories.PollVoteRepository;
import io.github.gvn2012.post_service.repositories.PostPollRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostPollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostPollServiceImpl implements IPostPollService {

    private final PostPollRepository pollRepository;
    private final PollOptionRepository optionRepository;
    private final PollVoteRepository voteRepository;
    private final PostRepository postRepository;
    private final PostPollMapper pollMapper;
    private final UserValidationService userValidationService;

    @Override
    @Transactional
    public PostPollResponse createPoll(UUID postId, PostPollRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        userValidationService.validateUserCanInteract(post.getAuthorId());

        post.setPostCategory(PostCategory.POLL);
        postRepository.save(post);

        PostPoll poll = pollMapper.toEntity(request);
        poll.setPost(post);
        poll.setPostId(post.getId());
        poll.setOptions(request.getOptions().stream().map(optRequest -> {
            PollOption option = pollMapper.toOptionEntity(optRequest);
            option.setPoll(poll);
            return option;
        }).collect(Collectors.toSet()));

        PostPoll saved = pollRepository.save(poll);
        return pollMapper.toResponse(saved);
    }

    @Override
    public PostPollResponse getPollByPostId(UUID postId) {
        PostPoll poll = pollRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Poll not found for post: " + postId));
        return pollMapper.toResponse(poll);
    }

    @Override
    @Transactional
    public void addPollOption(UUID pollId, PollOptionRequest request) {
        PostPoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new NotFoundException("Poll not found: " + pollId));

        userValidationService.validateUserCanInteract(poll.getPost().getAuthorId());

        PollOption option = pollMapper.toOptionEntity(request);
        option.setPoll(poll);
        optionRepository.save(option);
    }

    @Override
    @Transactional
    public void removePollOption(UUID pollId, UUID optionId) {
        PostPoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new NotFoundException("Poll not found: " + pollId));

        userValidationService.validateUserCanInteract(poll.getPost().getAuthorId());

        PollOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new NotFoundException("Option not found: " + optionId));

        if (!option.getPoll().getPostId().equals(pollId)) {
            throw new BadRequestException("Option does not belong to poll");
        }

        optionRepository.delete(option);
    }

    @Override
    @Transactional
    public void voteOnPoll(UUID pollId, UUID optionId, UUID userId) {
        userValidationService.validateUserCanInteract(userId);

        PostPoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new NotFoundException("Poll not found: " + pollId));

        if (Boolean.TRUE.equals(poll.getIsClosed()) ||
                (poll.getExpiresAt() != null && poll.getExpiresAt().isBefore(LocalDateTime.now()))) {
            throw new BadRequestException("Poll is closed");
        }

        PollOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new NotFoundException("Option not found: " + optionId));

        if (!option.getPoll().getPostId().equals(pollId)) {
            throw new BadRequestException("Option does not belong to poll");
        }

        // Simple vote logic (could be extended for multiple answers)
        PollVote vote = new PollVote();
        vote.setOption(option);
        vote.setUserId(userId);
        voteRepository.save(vote);

        option.setVoteCount(option.getVoteCount() + 1);
        optionRepository.save(option);
    }

    @Override
    public PostPollResponse getPollResults(UUID pollId) {
        PostPoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new NotFoundException("Poll not found: " + pollId));
        return pollMapper.toResponse(poll);
    }

    @Override
    @Transactional
    public void closePoll(UUID pollId, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        PostPoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new NotFoundException("Poll not found: " + pollId));
        poll.setIsClosed(true);
        pollRepository.save(poll);
    }
}
