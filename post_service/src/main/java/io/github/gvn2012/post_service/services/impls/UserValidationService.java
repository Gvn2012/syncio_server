package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RelationshipClient;
import io.github.gvn2012.post_service.clients.UserClient;
import io.github.gvn2012.post_service.dtos.UserStatusResponse;
import io.github.gvn2012.post_service.exceptions.BadRequestException;
import io.github.gvn2012.post_service.exceptions.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private static final java.time.Duration TIMEOUT = java.time.Duration.ofMillis(2000);
    private final UserClient userClient;
    private final RelationshipClient relationshipClient;

    public void validateUserCanInteract(UUID userId) {
        UserStatusResponse status = userClient.getUserStatus(userId)
                .timeout(TIMEOUT)
                .onErrorReturn(new UserStatusResponse(false, false, false, true))
                .block();

        if (status == null) {
            throw new BadRequestException("User not found");
        }
        if (Boolean.TRUE.equals(status.getSoftDeleted())) {
            throw new ForbiddenException("Account deleted");
        }
        if (Boolean.TRUE.equals(status.getBanned())) {
            throw new ForbiddenException("Account is banned");
        }
        if (Boolean.TRUE.equals(status.getSuspended())) {
            throw new ForbiddenException("Account is suspended");
        }
        if (Boolean.FALSE.equals(status.getActive())) {
            throw new ForbiddenException("Account is inactive");
        }
    }

    public void validateNotBlocked(UUID authorId, UUID interactorId) {
        if (authorId.equals(interactorId)) {
            return;
        }
        Boolean isBlocked = relationshipClient.isBlocked(authorId, interactorId)
                .timeout(TIMEOUT)
                .onErrorReturn(false)
                .block();

        if (Boolean.TRUE.equals(isBlocked)) {
            throw new ForbiddenException("You are blocked by this user");
        }
    }

    public void validateCanView(io.github.gvn2012.post_service.entities.Post post, UUID viewerId) {
        if (post.getAuthorId().equals(viewerId)) {
            return;
        }

        validateNotBlocked(post.getAuthorId(), viewerId);

        switch (post.getVisibility()) {
            case PRIVATE -> throw new ForbiddenException("This post is private");
            case FOLLOWER -> {
                Boolean isFollowing = relationshipClient.isFollowing(viewerId, post.getAuthorId())
                        .timeout(TIMEOUT)
                        .onErrorReturn(false)
                        .block();
                if (!Boolean.TRUE.equals(isFollowing)) {
                    throw new ForbiddenException("You must follow the author to view this post");
                }
            }
            default -> {}
        }
    }
}
