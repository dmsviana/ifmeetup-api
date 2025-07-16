package br.edu.ifpb.ifmeetup.dto.room.response;

import br.edu.ifpb.ifmeetup.domain.entity.RoomResource;
import br.edu.ifpb.ifmeetup.domain.projection.RoomResourceProjection;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta com dados de recurso da sala")
public record RoomResourceResponse(
    
    @Schema(description = "Identificador único do recurso")
    UUID id,
    
    @Schema(description = "Tipo do recurso")
    ResourceType resourceType,
    
    @Schema(description = "Quantidade disponível")
    Integer quantity,
    
    @Schema(description = "Detalhes adicionais do recurso")
    String details
) {
    public static RoomResourceResponse fromEntity(RoomResource resource) {
        return new RoomResourceResponse(
            resource.getId(),
            resource.getResourceType(),
            resource.getQuantity(),
            resource.getDetails()
        );
    }

    // novo método para conversão a partir de projection
    public static RoomResourceResponse fromProjection(RoomResourceProjection projection) {
        return new RoomResourceResponse(
            projection.getId(),
            projection.getResourceType(),
            projection.getQuantity(),
            projection.getDetails()
        );
    }
}