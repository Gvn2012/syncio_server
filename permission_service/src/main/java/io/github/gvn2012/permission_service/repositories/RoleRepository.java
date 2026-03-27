package io.github.gvn2012.permission_service.repositories;


import io.github.gvn2012.permission_service.entities.Role;
import io.github.gvn2012.permission_service.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

}
