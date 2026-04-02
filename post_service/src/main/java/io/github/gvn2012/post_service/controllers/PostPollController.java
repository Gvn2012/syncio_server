package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PollOptionRequest;
import io.github.gvn2012.post_service.dtos.requests.PostPollRequest;
import io.github.gvn2012.post_service.dtos.responses.PostPollResponse;
import io.github.gvn2012.post_service.services.interfaces.IPostPollService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/polls")
@RequiredArgsConstructor
public class PostPollController {

    private final IPostPollService pollService;

    @PostMapping("/{pid}")
    public ResponseEntity<APIResource<PostPollResponse>> createPoll(
            @PathVariable("pid") UUID postId,
            @RequestBody PostPollRequest poll) {
        return ResponseEntity.ok(APIResource.ok("Poll created", pollService.createPoll(postId, poll)));
    }

    @GetMapping("/{pid}")
    public ResponseEntity<APIResource<PostPollResponse>> getPoll(@PathVariable("pid") UUID postId) {
        return ResponseEntity.ok(APIResource.ok("Poll retrieved", pollService.getPollByPostId(postId)));
    }

    @PostMapping("/{plid}/vote/{optid}")
    public ResponseEntity<Void> voteOnPoll(
            @PathVariable("plid") UUID pollId,
            @PathVariable("optid") UUID optionId,
            @RequestHeader("X-User-ID") UUID userId) {
        pollService.voteOnPoll(pollId, optionId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{plid}/results")
    public ResponseEntity<APIResource<PostPollResponse>> getPollResults(@PathVariable("plid") UUID pollId) {
        return ResponseEntity.ok(APIResource.ok("Poll results", pollService.getPollResults(pollId)));
    }

    @PatchMapping("/{plid}/close")
    public ResponseEntity<Void> closePoll(
            @PathVariable("plid") UUID pollId,
            @RequestHeader("X-User-ID") UUID userId) {
        pollService.closePoll(pollId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{plid}/add-option")
    public ResponseEntity<Void> addPollOption(@PathVariable("plid") UUID pollId, @RequestBody PollOptionRequest entity) {
        pollService.addPollOption(pollId, entity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{plid}/remove-option/{optid}")
    public ResponseEntity<Void> removePollOption(@PathVariable("plid") UUID pollId, @PathVariable("optid") UUID optionId) {
        pollService.removePollOption(pollId, optionId);
        return ResponseEntity.noContent().build();
    }

}
