package br.edu.ifpb.ifmeetup.domain.repository.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.entity.EventParticipant;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import br.edu.ifpb.ifmeetup.domain.projection.EventParticipantProjection;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, UUID> {

    // Entity-based queries
    List<EventParticipant> findByEvent(Event event);

    List<EventParticipant> findByUser(User user);

    Optional<EventParticipant> findByEventAndUser(Event event, User user); 

    long countByEvent(Event event); 

    long countByEventAndAttendanceStatus(Event event, AttendanceStatus attendanceStatus);

    List<EventParticipant> findByEventAndAttendanceStatus(Event event, AttendanceStatus attendanceStatus);

    // Para buscar todos os eventos em que um usuário está inscrito (ou participou)
    @Query("SELECT ep.event FROM EventParticipant ep WHERE ep.user = :user")
    List<Event> findEventsRegisteredByUser(@Param("user") User user);

    // Para verificar se um usuário está inscrito em um evento específico
    boolean existsByEventAndUser(Event event, User user);

    // Projection-based queries
    List<EventParticipantProjection> findAllProjectedBy();

    Optional<EventParticipantProjection> findProjectedById(UUID id);

    List<EventParticipantProjection> findProjectedByEvent(Event event);

    List<EventParticipantProjection> findProjectedByUser(User user);

    Optional<EventParticipantProjection> findProjectedByEventAndUser(Event event, User user);

    List<EventParticipantProjection> findProjectedByEventAndAttendanceStatus(Event event, AttendanceStatus attendanceStatus);

    @Query("SELECT ep.id as id, ep.registrationDateTime as registrationDateTime, " +
           "ep.attendanceStatus as attendanceStatus, ep.certificateIssued as certificateIssued, " +
           "ep.feedback as feedback, ep.event.id as eventId, ep.event.title as eventTitle, " +
           "ep.user.id as userId, CONCAT(ep.user.firstName, ' ', ep.user.lastName) as userName, " +
           "ep.user.email as userEmail " +
           "FROM EventParticipant ep WHERE ep.user = :user")
    List<EventParticipantProjection> findProjectedByUserWithEventDetails(@Param("user") User user);

    Optional<EventParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);
}
