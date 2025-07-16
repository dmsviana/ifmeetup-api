package br.edu.ifpb.ifmeetup.dto.room.request;

import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para alteração de status da sala")
public record RoomStatusRequest(
    
    @Schema(description = "Novo status da sala", example = "UNDER_MAINTENANCE")
    @NotNull(message = "Status é obrigatório")
    RoomStatus status,
    
    @Schema(description = "Motivo da mudança de status", example = "Manutenção preventiva dos equipamentos")
    String reason
) {}