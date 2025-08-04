package br.edu.ifpb.ifmeetup.dto.event;

import java.util.UUID;

import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para registro ou atualização do participante em evento")
public record EventParticipantRequest(
		
		@Schema(description = "ID do evento")
		@NotNull(message = "O ID do evento é obrigatório")
		UUID eventId,
		
		@Schema(description = "ID do usuário participante")
		@NotNull(message = "O ID do usuário é obrigatório")
		UUID userId,
		
		@Schema(description = "Status da participação")
		@NotNull(message = "O status de participação é obrigatório")
		AttendanceStatus attendanceStatus,
		
		@Schema(description = "Feedback do participante")
		String feedback,
		
		@Schema(description = "Indica se o certificado foi emitido")
		Boolean certificateIssued
		
		) {

}
