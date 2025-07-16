package br.edu.ifpb.ifmeetup.dto.room.request;

import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados de um recurso disponível na sala")
public record RoomResourceRequest(
    
    @Schema(description = "Tipo do recurso", example = "PROJECTOR")
    @NotNull(message = "Tipo do recurso é obrigatório")
    ResourceType resourceType,
    
    @Schema(description = "Quantidade disponível", example = "2")
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    @Max(value = 999, message = "Quantidade máxima é 999")
    Integer quantity,
    
    @Schema(description = "Detalhes adicionais do recurso", example = "Projetor Epson 3000 lumens")
    @Size(max = 255, message = "Detalhes devem ter no máximo 255 caracteres")
    String details
) {
    @Override
    public String toString() {
        return "RoomResourceRequest{" +
                "resourceType=" + resourceType +
                ", quantity=" + quantity +
                ", details='" + details + '\'' +
                '}';
    }
}