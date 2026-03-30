package io.github.gvn2012.permission_service.repositories;

import io.github.gvn2012.permission_service.entities.Permission;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);
}
