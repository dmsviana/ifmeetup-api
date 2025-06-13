package br.edu.ifpb.ifmeetup.domain.repository.auth;

import br.edu.ifpb.ifmeetup.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
} 