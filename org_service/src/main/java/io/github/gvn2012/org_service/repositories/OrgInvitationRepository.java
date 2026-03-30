package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgInvitation;
import io.github.gvn2012.org_service.entities.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgInvitationRepository extends JpaRepository<OrgInvitation, UUID> {

    List<OrgInvitation> findByOrganization_IdAndStatus(UUID organizationId, InvitationStatus status);

    List<OrgInvitation> findByInvitedEmailAndStatus(String email, InvitationStatus status);

    Optional<OrgInvitation> findByIdAndOrganization_Id(UUID id, UUID organizationId);

    boolean existsByOrganization_IdAndInvitedEmailAndStatus(UUID organizationId, String email, InvitationStatus status);
}
