package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.gvn2012.user_service.entities.enums.EmergencyContactStatus;

public interface UserEmergencyContactRepository extends JpaRepository<UserEmergencyContact, UUID> {

    List<UserEmergencyContact> findByUser_IdAndStatusNotOrderByPriorityAsc(UUID userId, EmergencyContactStatus status);

    Optional<UserEmergencyContact> findByIdAndUser_IdAndStatusNot(UUID id, UUID userId, EmergencyContactStatus status);

    Optional<UserEmergencyContact> findByUser_IdAndPrimaryTrueAndStatusNot(UUID userId, EmergencyContactStatus status);
}
