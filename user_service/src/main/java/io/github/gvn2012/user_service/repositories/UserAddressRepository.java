package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserAddress;
import io.github.gvn2012.user_service.entities.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUser_Id(UUID userId);

    List<UserAddress> findByUser_IdAndAddressType(UUID userId, AddressType addressType);

    Optional<UserAddress> findByIdAndUser_Id(UUID id, UUID userId);

    Optional<UserAddress> findByUser_IdAndPrimaryTrue(UUID userId);
}
