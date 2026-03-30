package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserAddress;
import io.github.gvn2012.user_service.entities.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.gvn2012.user_service.entities.enums.AddressStatus;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUser_IdAndStatusNot(UUID userId, AddressStatus status);

    List<UserAddress> findByUser_IdAndAddressTypeAndStatusNot(UUID userId, AddressType addressType, AddressStatus status);

    Optional<UserAddress> findByIdAndUser_IdAndStatusNot(UUID id, UUID userId, AddressStatus status);

    Optional<UserAddress> findByUser_IdAndPrimaryTrueAndStatusNot(UUID userId, AddressStatus status);
}
