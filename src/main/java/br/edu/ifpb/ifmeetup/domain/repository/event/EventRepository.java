package br.edu.ifpb.ifmeetup.domain.repository.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.domain.projection.EventProjection;

public interface EventRepository extends JpaRepository<Event, UUID> {

    // Entity-based queries
    List<Event> findByOrganizer(User organizer);

    List<Event> findByRoom(Room room);

    // Para verificar conflitos de horário em uma sala
    @Query("SELECT e FROM Event e WHERE e.room = :room " +
           "AND e.status NOT IN (br.edu.ifpb.ifmeetup.domain.enums.EventStatus.REJECTED, br.edu.ifpb.ifmeetup.domain.enums.EventStatus.CANCELED_BY_ADMIN, br.edu.ifpb.ifmeetup.domain.enums.EventStatus.CANCELED_BY_ORGANIZER) " +
           "AND ((e.startDateTime < :endDateTime AND e.endDateTime > :startDateTime))")
    List<Event> findConflictingEventsInRoom(
            @Param("room") Room room,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByEventType(EventType eventType);

    long countByStatus(EventStatus status); 

    List<Event> findByStartDateTimeAfterAndStatusInOrderByStartDateTimeAsc(LocalDateTime dateTime, List<EventStatus> statuses);

    List<Event> findByOrganizerAndStatusNot(User organizer, EventStatus statusToExclude);

    @Query("SELECT e FROM Event e WHERE e.startDateTime > :now AND e.status = br.edu.ifpb.ifmeetup.domain.enums.EventStatus.APPROVED ORDER BY e.startDateTime ASC")
    List<Event> findUpcomingApprovedEvents(@Param("now") LocalDateTime now);

    List<Event> findByPublicEventTrueAndStatusOrderByStartDateTimeAsc(EventStatus status);

    // Projection-based queries
    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e")
    List<EventProjection> findAllProjectedBy();

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e WHERE e.id = :id")
    Optional<EventProjection> findProjectedById(@Param("id") UUID id);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e WHERE e.organizer = :organizer")
    List<EventProjection> findProjectedByOrganizer(@Param("organizer") User organizer);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e WHERE e.room = :room")
    List<EventProjection> findProjectedByRoom(@Param("room") Room room);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e WHERE e.status = :status")
    List<EventProjection> findProjectedByStatus(@Param("status") EventStatus status);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e WHERE e.eventType = :eventType")
    List<EventProjection> findProjectedByEventType(@Param("eventType") EventType eventType);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e WHERE e.startDateTime > :dateTime AND e.status IN :statuses " +
           "ORDER BY e.startDateTime ASC")
    List<EventProjection> findProjectedByStartDateTimeAfterAndStatusInOrderByStartDateTimeAsc(
            @Param("dateTime") LocalDateTime dateTime, @Param("statuses") List<EventStatus> statuses);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e WHERE e.startDateTime > :now AND e.status = br.edu.ifpb.ifmeetup.domain.enums.EventStatus.APPROVED " +
           "ORDER BY e.startDateTime ASC")
    List<EventProjection> findUpcomingApprovedEventsProjected(@Param("now") LocalDateTime now);

    // Métodos para tarefas agendadas de atualização automática de status

    /**
     * Busca eventos que já passaram da data de término mas ainda não foram marcados como concluídos.
     * Inclui eventos com status APPROVED ou IN_PROGRESS que já terminaram.
     */
    @Query("SELECT e FROM Event e WHERE e.endDateTime < :now " +
           "AND e.status IN (br.edu.ifpb.ifmeetup.domain.enums.EventStatus.APPROVED, br.edu.ifpb.ifmeetup.domain.enums.EventStatus.IN_PROGRESS)")
    List<Event> findEventsToMarkAsConcluded(@Param("now") LocalDateTime now);

    /**
     * Busca eventos aprovados que já iniciaram mas ainda não terminaram 
     * para marcar como em progresso.
     */
    @Query("SELECT e FROM Event e WHERE e.startDateTime <= :now " +
           "AND e.endDateTime > :now " +
           "AND e.status = br.edu.ifpb.ifmeetup.domain.enums.EventStatus.APPROVED")
    List<Event> findEventsToMarkAsInProgress(@Param("now") LocalDateTime now);
}
