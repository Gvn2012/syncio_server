package io.github.gvn2012.relationship_service.services.interfaces;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.CheckBlockStatusResponse;
import io.github.gvn2012.relationship_service.entities.enums.BlockReason;
import java.util.UUID;

public interface IUserBlockService {
    APIResource<Void> blockUser(UUID blockerId, UUID blockedId, BlockReason reason, String notes);

    APIResource<Void> unblockUser(UUID blockerId, UUID blockedId);

    CheckBlockStatusResponse checkBlockStatus(UUID userIdF, UUID userIdS);
}
