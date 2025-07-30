package br.edu.ifpb.ifmeetup.domain.repository.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.domain.projection.EventProjection;
import br.edu.ifpb.ifmeetup.domain.projection.EventProjectionWithParticipants;

public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByOrganizer(User organizer, Pageable pageable);

    Page<Event> findByRoom(Room room, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.room = :room " +
           "AND e.status NOT IN (br.edu.ifpb.ifmeetup.domain.enums.EventStatus.REJECTED, br.edu.ifpb.ifmeetup.domain.enums.EventStatus.CANCELED_BY_ADMIN, br.edu.ifpb.ifmeetup.domain.enums.EventStatus.CANCELED_BY_ORGANIZER) " +
           "AND ((e.startDateTime < :endDateTime AND e.endDateTime > :startDateTime))")
    List<Event> findConflictingEventsInRoom(
            @Param("room") Room room,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByEventType(EventType eventType, Pageable pageable);

    long countByStatus(EventStatus status); 

    Page<Event> findByStartDateTimeAfterAndStatusInOrderByStartDateTimeAsc(LocalDateTime dateTime, List<EventStatus> statuses, Pageable pageable);

    Page<Event> findByOrganizerAndStatusNot(User organizer, EventStatus statusToExclude, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startDateTime > :now AND e.status = br.edu.ifpb.ifmeetup.domain.enums.EventStatus.APPROVED ORDER BY e.startDateTime ASC")
    List<Event> findUpcomingApprovedEvents(@Param("now") LocalDateTime now);

    List<Event> findByPublicEventTrueAndStatusOrderByStartDateTimeAsc(EventStatus status);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail " +
           "FROM Event e")
    Page<EventProjection> findAllProjectedBy(Pageable pageable);

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
           "e.organizer.email as organizerEmail, " +
           "COUNT(ep.id) as currentParticipants " +
           "FROM Event e LEFT JOIN EventParticipant ep ON e.id = ep.event.id " +
           "WHERE e.organizer = :organizer " +
           "GROUP BY e.id, e.title, e.description, e.startDateTime, e.endDateTime, " +
           "e.maxParticipants, e.status, e.eventType, e.publicEvent, " +
           "e.room.id, e.room.name, e.organizer.id, e.organizer.firstName, " +
           "e.organizer.lastName, e.organizer.email")
    Page<EventProjectionWithParticipants> findProjectedByOrganizerWithParticipants(@Param("organizer") User organizer, Pageable pageable);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail, " +
           "COUNT(ep.id) as currentParticipants " +
           "FROM Event e LEFT JOIN EventParticipant ep ON e.id = ep.event.id " +
           "WHERE e.room = :room " +
           "GROUP BY e.id, e.title, e.description, e.startDateTime, e.endDateTime, " +
           "e.maxParticipants, e.status, e.eventType, e.publicEvent, " +
           "e.room.id, e.room.name, e.organizer.id, e.organizer.firstName, " +
           "e.organizer.lastName, e.organizer.email")
    Page<EventProjectionWithParticipants> findProjectedByRoomWithParticipants(@Param("room") Room room, Pageable pageable);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail, " +
           "COUNT(ep.id) as currentParticipants " +
           "FROM Event e LEFT JOIN EventParticipant ep ON e.id = ep.event.id " +
           "WHERE e.status = :status " +
           "GROUP BY e.id, e.title, e.description, e.startDateTime, e.endDateTime, " +
           "e.maxParticipants, e.status, e.eventType, e.publicEvent, " +
           "e.room.id, e.room.name, e.organizer.id, e.organizer.firstName, " +
           "e.organizer.lastName, e.organizer.email")
    Page<EventProjectionWithParticipants> findProjectedByStatusWithParticipants(@Param("status") EventStatus status, Pageable pageable);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail, " +
           "COUNT(ep.id) as currentParticipants " +
           "FROM Event e LEFT JOIN EventParticipant ep ON e.id = ep.event.id " +
           "WHERE e.eventType = :eventType " +
           "GROUP BY e.id, e.title, e.description, e.startDateTime, e.endDateTime, " +
           "e.maxParticipants, e.status, e.eventType, e.publicEvent, " +
           "e.room.id, e.room.name, e.organizer.id, e.organizer.firstName, " +
           "e.organizer.lastName, e.organizer.email")
    Page<EventProjectionWithParticipants> findProjectedByEventTypeWithParticipants(@Param("eventType") EventType eventType, Pageable pageable);

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

    @Query("SELECT e FROM Event e WHERE e.endDateTime < :now " +
           "AND e.status IN (br.edu.ifpb.ifmeetup.domain.enums.EventStatus.APPROVED, br.edu.ifpb.ifmeetup.domain.enums.EventStatus.IN_PROGRESS)")
    List<Event> findEventsToMarkAsConcluded(@Param("now") LocalDateTime now);


    @Query("SELECT e FROM Event e WHERE e.startDateTime <= :now " +
           "AND e.endDateTime > :now " +
           "AND e.status = br.edu.ifpb.ifmeetup.domain.enums.EventStatus.APPROVED")
    List<Event> findEventsToMarkAsInProgress(@Param("now") LocalDateTime now);

    @Query("SELECT e.id as id, e.title as title, e.description as description, " +
           "e.startDateTime as startDateTime, e.endDateTime as endDateTime, " +
           "e.maxParticipants as maxParticipants, e.status as status, " +
           "e.eventType as eventType, e.publicEvent as publicEvent, " +
           "e.room.id as roomId, e.room.name as roomName, " +
           "e.organizer.id as organizerId, " +
           "CONCAT(e.organizer.firstName, ' ', e.organizer.lastName) as organizerName, " +
           "e.organizer.email as organizerEmail, " +
           "COUNT(ep.id) as currentParticipants " +
           "FROM Event e LEFT JOIN EventParticipant ep ON e.id = ep.event.id " +
           "GROUP BY e.id, e.title, e.description, e.startDateTime, e.endDateTime, " +
           "e.maxParticipants, e.status, e.eventType, e.publicEvent, " +
           "e.room.id, e.room.name, e.organizer.id, e.organizer.firstName, " +
           "e.organizer.lastName, e.organizer.email")
    Page<EventProjectionWithParticipants> findAllProjectedWithParticipants(Pageable pageable);
}
