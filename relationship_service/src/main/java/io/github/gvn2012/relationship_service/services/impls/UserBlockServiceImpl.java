package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.CheckBlockStatusResponse;
import io.github.gvn2012.relationship_service.entities.UserBlock;
import io.github.gvn2012.relationship_service.entities.UserRelationship;
import io.github.gvn2012.relationship_service.entities.enums.BlockReason;
import io.github.gvn2012.relationship_service.entities.enums.BlockScope;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.repositories.UserBlockRepository;
import io.github.gvn2012.relationship_service.repositories.UserRelationshipRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IUserBlockService;
import io.github.gvn2012.relationship_service.services.kafka.RelationshipEventProducer;
import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements IUserBlockService {

    private final UserBlockRepository blockRepository;
    private final UserRelationshipRepository relationshipRepository;
    private final RelationshipEventProducer eventProducer;

    @Override
    @Transactional
    public APIResource<Void> blockUser(UUID blockerId, UUID blockedId, BlockReason reason, String notes) {
        if (blockerId.equals(blockedId)) {
            return APIResource.error("SELF_BLOCK", "Cannot block yourself", HttpStatus.BAD_REQUEST, null);
        }

        if (blockRepository.existsByBlockerUserIdAndBlockedUserId(blockerId, blockedId)) {
            return APIResource.error("ALREADY_BLOCKED", "User is already blocked", HttpStatus.BAD_REQUEST, null);
        }

        UserBlock block = new UserBlock();
        block.setBlockerUserId(blockerId);
        block.setBlockedUserId(blockedId);
        block.setReason(reason);
        block.setReasonNote(notes);
        block.setScope(BlockScope.FULL);
        blockRepository.save(block);

        // Sever relationships
        severRelationships(blockerId, blockedId);
        severRelationships(blockedId, blockerId);

        eventProducer.publishEvent(
                new RelationshipChangedEvent(blockerId, blockedId, RelationshipChangedEvent.ChangeType.BLOCK));

        return APIResource.message("User blocked successfully", HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<Void> unblockUser(UUID blockerId, UUID blockedId) {
        UserBlock block = blockRepository.findByBlockerUserIdAndBlockedUserId(blockerId, blockedId).orElse(null);
        if (block == null) {
            return APIResource.error("NOT_BLOCKED", "User is not blocked", HttpStatus.NOT_FOUND, null);
        }

        blockRepository.delete(block);
        eventProducer.publishEvent(
                new RelationshipChangedEvent(blockerId, blockedId, RelationshipChangedEvent.ChangeType.UNBLOCK));

        return APIResource.message("User unblocked successfully", HttpStatus.OK);
    }

    private void severRelationships(UUID source, UUID target) {
        List<UserRelationship> relationships = relationshipRepository.findAllBySourceUserIdAndStatus(source,
                RelationshipStatus.ACTIVE);
        for (UserRelationship rel : relationships) {
            if (rel.getTargetUserId().equals(target)) {
                rel.setStatus(RelationshipStatus.REMOVED);
                relationshipRepository.save(rel);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CheckBlockStatusResponse checkBlockStatus(UUID userIdF, UUID userIdS) {
        boolean fDirection = blockRepository.existsByBlockerUserIdAndBlockedUserIdAndIsActiveTrue(userIdF, userIdS);
        boolean sDirection = blockRepository.existsByBlockerUserIdAndBlockedUserIdAndIsActiveTrue(userIdS, userIdF);

        return CheckBlockStatusResponse.builder()
                .isBlocked(fDirection || sDirection)
                .isBidirectionalBlocked(fDirection && sDirection)
                .blockerId(
                        (fDirection && sDirection) ? null : (fDirection ? userIdF : (sDirection ? userIdS : null)))
                .build();
    }
}
