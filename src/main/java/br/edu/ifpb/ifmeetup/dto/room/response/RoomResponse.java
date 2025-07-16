package br.edu.ifpb.ifmeetup.dto.room.response;

import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.projection.RoomProjection;
import br.edu.ifpb.ifmeetup.domain.projection.RoomWithResourcesProjection;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta com dados da sala")
public record RoomResponse(
    
    @Schema(description = "Identificador único da sala")
    UUID id,
    
    @Schema(description = "Nome da sala")
    String name,
    
    @Schema(description = "Localização da sala")
    String location,
    
    @Schema(description = "Capacidade máxima de pessoas")
    Integer capacity,
    
    @Schema(description = "Tipo da sala")
    RoomType type,
    
    @Schema(description = "Status operacional da sala")
    RoomStatus status,
    
    @Schema(description = "Descrição adicional da sala")
    String description,
    
    @Schema(description = "Recursos disponíveis na sala")
    Set<RoomResourceResponse> resources,
    
    @Schema(description = "Nome do usuário que criou a sala")
    String createdBy,
    
    @Schema(description = "Nome do último usuário que atualizou a sala")
    String updatedBy,
    
    @Schema(description = "Data e hora de criação")
    LocalDateTime createdAt,
    
    @Schema(description = "Data e hora da última atualização")
    LocalDateTime updatedAt
) {
    public static RoomResponse fromEntity(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getName(),
            room.getLocation(),
            room.getCapacity(),
            room.getType(),
            room.getStatus(),
            room.getDescription(),
            room.getInventory() != null ? 
                room.getInventory().stream()
                    .map(RoomResourceResponse::fromEntity)
                    .collect(Collectors.toSet()) : null,
            room.getCreatedBy() != null ? 
                room.getCreatedBy().getFirstName() + " " + room.getCreatedBy().getLastName() : null,
            room.getUpdatedBy() != null ? 
                room.getUpdatedBy().getFirstName() + " " + room.getUpdatedBy().getLastName() : null,
            room.getCreatedAt(),
            room.getUpdatedAt()
        );
    }
    
    public static RoomResponse fromEntityBasic(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getName(),
            room.getLocation(),
            room.getCapacity(),
            room.getType(),
            room.getStatus(),
            room.getDescription(),
            null,
            null,
            null,
            null,
            null
        );
    }

    // novo método para conversão a partir de projection básica
    public static RoomResponse fromProjection(RoomProjection projection) {
        return new RoomResponse(
            projection.getId(),
            projection.getName(),
            projection.getLocation(),
            projection.getCapacity(),
            projection.getType(),
            projection.getStatus(),
            projection.getDescription(),
            null, // resources não estão disponíveis na projection básica
            null, // campos de auditoria não estão disponíveis em projections
            null,
            null,
            null
        );
    }

    // novo método para conversão a partir de projection com recursos
    public static RoomResponse fromProjectionWithResources(RoomWithResourcesProjection projection) {
        return new RoomResponse(
            projection.getId(),
            projection.getName(),
            projection.getLocation(),
            projection.getCapacity(),
            projection.getType(),
            projection.getStatus(),
            projection.getDescription(),
            projection.getInventory() != null ? 
                projection.getInventory().stream()
                    .map(RoomResourceResponse::fromProjection)
                    .collect(Collectors.toSet()) : null,
            null, // campos de auditoria não estão disponíveis em projections
            null,
            null,
            null
        );
    }
}