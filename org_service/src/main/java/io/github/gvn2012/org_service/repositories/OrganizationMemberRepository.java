package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrganizationMember;
import io.github.gvn2012.org_service.entities.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    List<OrganizationMember> findByOrganization_IdAndStatus(UUID organizationId, MembershipStatus status);

    List<OrganizationMember> findByUserId(UUID userId);

    List<OrganizationMember> findByUserIdAndStatus(UUID userId, MembershipStatus status);

    Optional<OrganizationMember> findByOrganization_IdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganization_IdAndUserId(UUID organizationId, UUID userId);

    long countByOrganization_IdAndStatus(UUID organizationId, MembershipStatus status);
}
