package br.edu.ifpb.ifmeetup.domain.repository.auth;

import br.edu.ifpb.ifmeetup.domain.entity.SuapUser;
import br.edu.ifpb.ifmeetup.domain.enums.SuapUserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuapUserRepository extends JpaRepository<SuapUser, UUID> {

    Optional<SuapUser> findByMatricula(String matricula);

    Optional<SuapUser> findBySuapUuid(String suapUuid);

    Optional<SuapUser> findByGeneratedEmail(String generatedEmail);

    List<SuapUser> findByUserType(SuapUserType userType);

    boolean existsByMatricula(String matricula);

    boolean existsBySuapUuid(String suapUuid);

    long countByUserType(SuapUserType userType);
}