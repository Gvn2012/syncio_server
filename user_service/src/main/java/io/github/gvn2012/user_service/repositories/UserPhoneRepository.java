package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserPhone;
import io.github.gvn2012.user_service.entities.enums.PhoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserPhoneRepository extends JpaRepository<UserPhone, UUID> {

    Boolean existsUserPhoneByPhoneNumber(String phoneNumber);

    Set<UserPhone> findAllByUser_Id(UUID userId);

    Optional<UserPhone> findByIdAndUser_Id(UUID id, UUID userId);

    Optional<UserPhone> findByIdAndUser_IdAndStatus(UUID id, UUID userId, PhoneStatus status);

    Optional<UserPhone> findByIdAndUser_IdAndStatusIn(UUID id, UUID userId, List<PhoneStatus> statuses);

    boolean existsByPhoneNumberAndCountryCodeAndStatusNot(String phoneNumber, String countryCode, PhoneStatus excludedStatus);

    Optional<UserPhone> findByUser_IdAndPrimaryTrueAndStatusNot(UUID userId, PhoneStatus excludedStatus);

    long countByUser_IdAndStatusNot(UUID userId, PhoneStatus excludedStatus);
}
