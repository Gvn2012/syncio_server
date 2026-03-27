package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserPhone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface UserPhoneRepository extends JpaRepository<UserPhone, UUID> {
    Boolean existsUserPhoneByPhoneNumber (String phoneNumber);

    Set<UserPhone> findAllByUser_Id(UUID user_id);
}
