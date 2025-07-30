package br.edu.ifpb.ifmeetup.dto.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para fornecimento de feedback sobre um evento")
public record FeedbackRequest(

    @NotBlank(message = "O feedback é obrigatório")
    @Size(min = 10, max = 1000, message = "O feedback deve ter entre 10 e 1000 caracteres")
    @Schema(description = "Comentário do participante sobre o evento", 
            example = "Evento muito bem organizado, conteúdo relevante e apresentação excelente!")
    String feedback

) {
} 