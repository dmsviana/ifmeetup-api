package br.edu.ifpb.ifmeetup.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.edu.ifpb.ifmeetup.domain.entity.EventParticipant;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import br.edu.ifpb.ifmeetup.domain.projection.EventParticipantProjection;
import br.edu.ifpb.ifmeetup.dto.user.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta com dados do participante do evento")
public record EventParticipantResponse(
		
		@Schema(description = "Identificador único do registro de participação")
		UUID id,

		@Schema(description = "Evento relacionado")
		EventSummaryResponse event,
		
		@Schema(description = "Usuário participante")
		UserResponse user,
		
		@Schema(description = "Data e hora do registro de participação")
		LocalDateTime registrationDateTime,
		
		@Schema(description = "Status de participação")
		AttendanceStatus attendanceStatus,
		
		@Schema(description = "Indica se o certificado foi emitido")
		Boolean certificateIssued,
		
		@Schema(description = "Feedback do participante")
		String feedback
		
		) {
	
	
	public static EventParticipantResponse fromEntity(EventParticipant participant) {
		
		return new EventParticipantResponse(
				participant.getId(),
				EventSummaryResponse.fromEntity(participant.getEvent()),
				UserResponse.fromEntityBasicInfo(participant.getUser()),
				participant.getRegistrationDateTime(), participant.getAttendanceStatus(),
				participant.isCertificateIssued(),
				participant.getFeedback());
	}
	
	public static EventParticipantResponse fromProjection(EventParticipantProjection projection) {
	       
        return new EventParticipantResponse(
            projection.getId(),
            new EventSummaryResponse(
                projection.getEventId(),
                projection.getEventTitle()
            ),
            new UserResponse(
                projection.getUserId(),
                projection.getUserFirstName() + " " + projection.getUserLastName(),
                projection.getUserEmail(),
                null,
                null,
                null
            ),
            projection.getRegistrationDateTime(),
            projection.getAttendanceStatus(),
            projection.isCertificateIssued(),
            projection.getFeedback()
        );
    }
}
