package io.github.gvn2012.relationship_service.services.interfaces;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.entities.enums.MuteScope;
import java.time.LocalDateTime;
import java.util.UUID;

public interface IUserMuteService {
    APIResource<Void> muteUser(UUID muterId, UUID mutedId, MuteScope scope, LocalDateTime expiresAt);
    APIResource<Void> unmuteUser(UUID muterId, UUID mutedId);
}
