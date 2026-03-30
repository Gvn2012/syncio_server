package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEmergencyContactRepository extends JpaRepository<UserEmergencyContact, UUID> {

    List<UserEmergencyContact> findByUser_IdOrderByPriorityAsc(UUID userId);

    Optional<UserEmergencyContact> findByIdAndUser_Id(UUID id, UUID userId);

    Optional<UserEmergencyContact> findByUser_IdAndPrimaryTrue(UUID userId);
}
