package br.edu.ifpb.ifmeetup.dto.event;

import java.util.UUID;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta resumida com dados básicos do evento")
public record EventSummaryResponse(
   

    @Schema(description = "Identificador único do evento")
    UUID id,
    
    @Schema(description = "Título do evento")
    String title

    
) {


    public static EventSummaryResponse fromEntity(Event event) {
        return new EventSummaryResponse(
            event.getId(),
            event.getTitle()
        );
    }
}