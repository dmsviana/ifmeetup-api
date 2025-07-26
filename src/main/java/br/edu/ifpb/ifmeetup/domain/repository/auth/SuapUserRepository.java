package br.edu.ifpb.ifmeetup.domain.repository.auth;

import br.edu.ifpb.ifmeetup.domain.entity.SuapUser;
import br.edu.ifpb.ifmeetup.domain.enums.SuapUserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuapUserRepository extends JpaRepository<SuapUser, UUID> {

    Optional<SuapUser> findByMatricula(String matricula);

    Optional<SuapUser> findBySuapUuid(String suapUuid);

    Optional<SuapUser> findByGeneratedEmail(String generatedEmail);

    @Query("SELECT s FROM SuapUser s WHERE s.lastSync IS NULL OR s.lastSync < :cutoffTime")
    List<SuapUser> findUsersNeedingSync(@Param("cutoffTime") LocalDateTime cutoffTime);

    List<SuapUser> findByUserType(SuapUserType userType);

    @Query("SELECT s FROM SuapUser s WHERE s.user IS NULL")
    List<SuapUser> findUsersWithoutSystemUser();

    boolean existsByMatricula(String matricula);

    boolean existsBySuapUuid(String suapUuid);

    @Query("SELECT s FROM SuapUser s WHERE s.lastSync >= :since ORDER BY s.lastSync DESC")
    List<SuapUser> findRecentlySyncedUsers(@Param("since") LocalDateTime since);

    long countByUserType(SuapUserType userType);
}