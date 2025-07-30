package br.edu.ifpb.ifmeetup.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.domain.projection.EventProjection;
import br.edu.ifpb.ifmeetup.dto.room.response.RoomResponse;
import br.edu.ifpb.ifmeetup.dto.user.response.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta com dados do evento")
public record EventResponse(


    @Schema(description = "Identificador único do evento")
    UUID id,
    
    @Schema(description = "Título do evento")
    String title,
    
    @Schema(description = "Descrição do evento")
    String description,
    
    @Schema(description = "Organizador do evento")
    UserResponse organizer,
    
    @Schema(description = "Sala do evento")
    RoomResponse room,
    
    @Schema(description = "Data e hora de início do evento")
    LocalDateTime startDateTime,
    
    @Schema(description = "Data e hora de término do evento")
    LocalDateTime endDateTime,
    
    @Schema(description = "Número máximo de participantes")
    Integer maxParticipants,
    
    @Schema(description = "Status do evento")
    EventStatus status,
    
    @Schema(description = "Tipo do evento")
    EventType eventType,
    
    @Schema(description = "Indica se o evento é público")
    boolean publicEvent,
    
    @Schema(description = "Usuário que aprovou o evento")
    UserResponse approvedBy,
    
    @Schema(description = "Data e hora da aprovação do evento")
    LocalDateTime approvalDateTime,
    
    @Schema(description = "Motivo da rejeição do evento")
    String rejectionReason,
    
    @Schema(description = "Número atual de participantes")
    Integer currentParticipants


) {


    public static EventResponse fromEntity(Event event, Integer currentParticipants) {

        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            UserResponse.fromEntity(event.getOrganizer()),
            RoomResponse.fromEntity(event.getRoom()),
            event.getStartDateTime(),
            event.getEndDateTime(),
            event.getMaxParticipants(),
            event.getStatus(),
            event.getEventType(),
            event.isPublicEvent(),
            event.getApprovedBy() != null ? UserResponse.fromEntity(event.getApprovedBy()) : null,
            event.getApprovalDateTime(),
            event.getRejectionReason(),
            currentParticipants
        );
    }
    
    public static EventResponse fromProjection(EventProjection projection, Integer currentParticipants) {
        return new EventResponse(
            projection.getId(),
            projection.getTitle(),
            projection.getDescription(),
            // organizer básico a partir da projection
            new UserResponse(
                projection.getOrganizerId(),
                projection.getOrganizerName(),
                projection.getOrganizerEmail(),
                null, // phoneNumber não disponível na projection
                null, // roles não disponíveis na projection
                null  // permissions não disponíveis na projection
            ),
            // room básico a partir da projection
            new RoomResponse(
                projection.getRoomId(),
                projection.getRoomName(),
                null, // location não disponível na projection
                null, // capacity não disponível na projection
                null, // type não disponível na projection
                null, // status não disponível na projection
                null, // description não disponível na projection
                null, // resources não disponíveis na projection
                null, // createdBy não disponível na projection
                null, // updatedBy não disponível na projection
                null, // createdAt não disponível na projection
                null  // updatedAt não disponível na projection
            ),
            projection.getStartDateTime(),
            projection.getEndDateTime(),
            projection.getMaxParticipants(),
            projection.getStatus(),
            projection.getEventType(),
            projection.isPublicEvent(),
            null, // approvedBy não disponível na projection
            null, // approvalDateTime não disponível na projection
            null, // rejectionReason não disponível na projection
            currentParticipants
        );
    }
    
    public static EventResponse fromProjection(EventProjection projection) {
        return new EventResponse(
            projection.getId(),
            projection.getTitle(),
            projection.getDescription(),
            // organizer básico a partir da projection
            new UserResponse(
                projection.getOrganizerId(),
                projection.getOrganizerName(),
                projection.getOrganizerEmail(),
                null, // phoneNumber não disponível na projection
                null, // roles não disponíveis na projection
                null  // permissions não disponíveis na projection
            ),
            // room básico a partir da projection
            new RoomResponse(
                projection.getRoomId(),
                projection.getRoomName(),
                null, // location não disponível na projection
                null, // capacity não disponível na projection
                null, // type não disponível na projection
                null, // status não disponível na projection
                null, // description não disponível na projection
                null, // resources não disponíveis na projection
                null, // createdBy não disponível na projection
                null, // updatedBy não disponível na projection
                null, // createdAt não disponível na projection
                null  // updatedAt não disponível na projection
            ),
            projection.getStartDateTime(),
            projection.getEndDateTime(),
            projection.getMaxParticipants(),
            projection.getStatus(),
            projection.getEventType(),
            projection.isPublicEvent(),
            null, // approvedBy não disponível na projection
            null, // approvalDateTime não disponível na projection
            null, // rejectionReason não disponível na projection
            null
     );
    }
}