package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.entities.UserMute;
import io.github.gvn2012.relationship_service.entities.UserRelationship;
import io.github.gvn2012.relationship_service.entities.enums.MuteScope;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.repositories.UserMuteRepository;
import io.github.gvn2012.relationship_service.repositories.UserRelationshipRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IUserMuteService;
import io.github.gvn2012.relationship_service.services.kafka.RelationshipEventProducer;
import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserMuteServiceImpl implements IUserMuteService {

    private final UserMuteRepository muteRepository;
    private final UserRelationshipRepository relationshipRepository;
    private final RelationshipEventProducer eventProducer;

    @Override
    @Transactional
    public APIResource<Void> muteUser(UUID muterId, UUID mutedId, MuteScope scope, LocalDateTime expiresAt) {
        if (muterId.equals(mutedId)) {
            return APIResource.error("SELF_MUTE", "Cannot mute yourself", HttpStatus.BAD_REQUEST, null);
        }

        UserMute mute = muteRepository.findByMuterUserIdAndMutedUserIdAndScope(muterId, mutedId, scope).orElse(null);

        if (mute == null) {
            mute = new UserMute();
            mute.setMuterUserId(muterId);
            mute.setMutedUserId(mutedId);
            mute.setScope(scope);
        }

        mute.setIsActive(true);
        mute.setExpiresAt(expiresAt);
        muteRepository.save(mute);

        // Update relationship if it exists
        updateRelationshipShowInFeed(muterId, mutedId, false);

        eventProducer.publishEvent(new RelationshipChangedEvent(muterId, mutedId, RelationshipChangedEvent.ChangeType.MUTE));

        return APIResource.message("User muted successfully", HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<Void> unmuteUser(UUID muterId, UUID mutedId) {
        List<UserMute> mutes = muteRepository.findAllByMuterUserIdAndMutedUserId(muterId, mutedId);
        if (mutes.isEmpty()) {
            return APIResource.error("NOT_MUTED", "User is not muted", HttpStatus.NOT_FOUND, null);
        }

        for (UserMute mute : mutes) {
            mute.setIsActive(false);
            mute.setUnmutedAt(LocalDateTime.now());
            muteRepository.save(mute);
        }

        updateRelationshipShowInFeed(muterId, mutedId, true);

        eventProducer.publishEvent(new RelationshipChangedEvent(muterId, mutedId, RelationshipChangedEvent.ChangeType.UNMUTE));

        return APIResource.message("User unmuted successfully", HttpStatus.OK);
    }

    private void updateRelationshipShowInFeed(UUID source, UUID target, boolean showInFeed) {
        List<UserRelationship> relationships = relationshipRepository.findAllBySourceUserIdAndStatus(source, RelationshipStatus.ACTIVE);
        for (UserRelationship rel : relationships) {
            if (rel.getTargetUserId().equals(target)) {
                rel.setShowInFeed(showInFeed);
                relationshipRepository.save(rel);
            }
        }
    }
}
