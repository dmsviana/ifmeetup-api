package br.edu.ifpb.ifmeetup.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requisição para rejeição de um evento")
public record EventRejectRequest(
    
    @Schema(description = "Motivo da rejeição do evento", example = "Campus estará fechado para reforma na data do evento")
    @NotBlank(message = "O motivo da rejeição é obrigatório")
    @Size(min = 10, max = 500, message = "O motivo da rejeição deve ter entre 10 e 500 caracteres")
    String rejectionReason
    
) {
}