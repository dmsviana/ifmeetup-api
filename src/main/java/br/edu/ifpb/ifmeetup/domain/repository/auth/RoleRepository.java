package br.edu.ifpb.ifmeetup.domain.repository.auth;

import br.edu.ifpb.ifmeetup.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
} 