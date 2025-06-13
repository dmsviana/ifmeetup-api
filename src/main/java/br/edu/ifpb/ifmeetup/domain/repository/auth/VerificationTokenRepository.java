package br.edu.ifpb.ifmeetup.domain.repository.auth;

import br.edu.ifpb.ifmeetup.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUserId(UUID userId);
} 