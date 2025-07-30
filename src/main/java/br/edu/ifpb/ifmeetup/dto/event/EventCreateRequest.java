package br.edu.ifpb.ifmeetup.dto.event;

import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Requisição para criação de um evento")
public record EventCreateRequest(
		
		@Schema(description = "Título do evento")
		@NotBlank(message = "O título do evento é obrigatório")
		@Size(min = 5, max = 150, message = "O título do evento deve ter entre 5 e 150 caracteres")
		String title,
		
		@Schema(description = "Descrição do evento")
		@NotBlank(message = "A descrição do evento é obrigatória")
		String description,
		
		@Schema(description = "ID da sala onde o evento será realizado")
		@NotNull(message = "A sala para o evento é obrigatória")
		UUID roomId,
		
		@Schema(description = "Data e hora de início do evento")
		@NotNull(message = "A data e hora de início são obrigatórias")
		@Future(message = "A data de início do evento deve ser no futuro")
		LocalDateTime startDateTime,
		
		@Schema(description = "Data e hora de término do evento")
		@NotNull(message = "A data e hora de término são obrigatórias")
		@Future(message = "A data de término do evento deve ser no futuro")
		LocalDateTime endDateTime,
		
		@Schema(description = "Número máximo de participantes")
		@NotNull(message = "O número máximo de participantes é obrigatório")
		@Min(value = 1, message = "O evento deve permitir no mínimo 1 participante")
		Integer maxParticipants,
		
		@Schema(description = "Tipo do evento")
		@NotNull(message = "O tipo do evento é obrigatório")
		EventType eventType,
		
		@Schema(description = "Indica se o evento é público", defaultValue = "true")
		boolean publicEvent
		
		) {
	
	@AssertTrue(message = "A data de término deve ser posterior à data de início")
	public boolean isEndDateTimeAfterStartDateTime() {
		
		if (startDateTime == null || endDateTime == null) {
			return true;
		}
		
		return endDateTime.isAfter(startDateTime);
		
		
	}

}
