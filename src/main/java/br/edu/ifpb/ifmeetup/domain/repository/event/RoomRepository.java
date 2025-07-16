package br.edu.ifpb.ifmeetup.domain.repository.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.domain.projection.RoomProjection;
import br.edu.ifpb.ifmeetup.domain.projection.RoomWithResourcesProjection;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    // Entity-based queries
    List<Room> findByStatus(final RoomStatus status);

    List<Room> findByType(final RoomType type);

    List<Room> findByCapacityGreaterThanEqual(final Integer capacity);

    Optional<Room> findByNameAndLocation(final String name, final String location);

    // Projection-based queries
    List<RoomProjection> findAllProjectedBy();

    Optional<RoomProjection> findProjectedById(UUID id);

    List<RoomProjection> findProjectedByStatus(RoomStatus status);

    List<RoomProjection> findProjectedByType(RoomType type);

    List<RoomProjection> findProjectedByCapacityGreaterThanEqual(Integer capacity);

    // TODO: Simplificado enquanto não temos a entidade Event
    @Query("SELECT r FROM Room r WHERE r.status = :status AND r.id NOT IN " +
            "(SELECT e.room.id FROM Event e WHERE " +
            "((e.startDateTime < :endDateTime AND e.endDateTime > :startDateTime) AND e.status NOT IN ('REJECTED', 'CANCELED'))"
            +
            ")")
    List<Room> findAvailableRooms(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("status") RoomStatus status);

    // Exemplo de consulta para salas que possuem um tipo específico de recurso com
    // quantidade mínima
    // Isso será útil para o RoomService
    @Query("SELECT r FROM Room r JOIN r.inventory inv WHERE inv.resourceType = :resourceType AND inv.quantity >= :minQuantity AND r.status = br.edu.ifpb.ifmeetup.domain.enums.RoomStatus.AVAILABLE")
    List<Room> findByResourceAndQuantity(
            @Param("resourceType") ResourceType resourceType,
            @Param("minQuantity") Integer minQuantity);

    // Projection-based queries for enhanced functionality
    @Query("SELECT r FROM Room r WHERE r.status = :status AND r.id NOT IN " +
            "(SELECT e.room.id FROM Event e WHERE " +
            "((e.startDateTime < :endDateTime AND e.endDateTime > :startDateTime) AND e.status NOT IN ('REJECTED', 'CANCELED'))"
            +
            ")")
    List<RoomProjection> findProjectedAvailableRooms(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("status") RoomStatus status);

    @Query("SELECT r FROM Room r JOIN r.inventory inv WHERE inv.resourceType = :resourceType AND inv.quantity >= :minQuantity AND r.status = br.edu.ifpb.ifmeetup.domain.enums.RoomStatus.AVAILABLE")
    List<RoomWithResourcesProjection> findProjectedByResourceAndQuantity(
            @Param("resourceType") ResourceType resourceType,
            @Param("minQuantity") Integer minQuantity);

}
