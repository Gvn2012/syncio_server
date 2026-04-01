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

    private final UserClient userClient;
    private final RelationshipClient relationshipClient;

    public void validateUserCanInteract(UUID userId) {
        UserStatusResponse status = userClient.getUserStatus(userId).block();
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
        Boolean isBlocked = relationshipClient.isBlocked(authorId, interactorId).block();
        if (Boolean.TRUE.equals(isBlocked)) {
            throw new ForbiddenException("You are blocked by this user");
        }
    }
}
