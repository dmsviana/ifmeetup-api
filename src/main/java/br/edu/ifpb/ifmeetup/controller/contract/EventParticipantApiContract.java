package br.edu.ifpb.ifmeetup.controller.contract;

import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import br.edu.ifpb.ifmeetup.dto.event.EventParticipantResponse;
import br.edu.ifpb.ifmeetup.dto.event.FeedbackRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Participantes de Eventos", description = "Endpoints relacionados ao gerenciamento de participantes em eventos")
public interface EventParticipantApiContract {

    @Operation(summary = "Inscrever-se em evento", description = "Permite que o usuário atual se inscreva em um evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscrição realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventParticipantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuário já inscrito no evento"),
            @ApiResponse(responseCode = "422", description = "Evento lotado ou não disponível para inscrição"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<EventParticipantResponse> registerForEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Cancelar inscrição", description = "Permite que o usuário cancele sua inscrição em um evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Inscrição cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Evento ou inscrição não encontrada"),
            @ApiResponse(responseCode = "422", description = "Não é possível cancelar após o início do evento"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Void> cancelRegistration(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Atualizar status de presença", 
               description = "Permite que o organizador ou admin atualize o status de presença de um participante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = EventParticipantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento, usuário ou inscrição não encontrada"),
            @ApiResponse(responseCode = "422", description = "Status inválido ou não permitido"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não tem permissão")
    })
    ResponseEntity<EventParticipantResponse> updateAttendanceStatus(
            @PathVariable UUID eventId,
            @RequestParam UUID userId,
            @RequestParam AttendanceStatus status,
            @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Fornecer feedback", 
               description = "Permite que um participante forneça feedback sobre o evento após sua conclusão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = EventParticipantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento ou inscrição não encontrada"),
            @ApiResponse(responseCode = "422", description = "Evento ainda não terminou ou participante não compareceu"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<EventParticipantResponse> provideFeedback(
            @PathVariable UUID eventId,
            @Valid @RequestBody FeedbackRequest feedbackRequest,
            @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Listar participantes do evento", 
               description = "Retorna a lista de participantes de um evento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de participantes recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventParticipantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<EventParticipantResponse>> getEventParticipants(@PathVariable UUID eventId);

    @Operation(summary = "Listar participantes por status", 
               description = "Retorna a lista de participantes de um evento filtrados por status de presença")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de participantes recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventParticipantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<EventParticipantResponse>> getEventParticipantsByStatus(
            @PathVariable UUID eventId,
            @RequestParam AttendanceStatus status);

    @Operation(summary = "Eventos de um usuário específico", 
               description = "Retorna a lista de eventos em que um usuário específico está inscrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventParticipantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<EventParticipantResponse>> getUserEvents(@RequestParam UUID userId);

    @Operation(summary = "Meus eventos", 
               description = "Retorna a lista de eventos em que o usuário atual está inscrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventParticipantResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<List<EventParticipantResponse>> getMyEvents(@AuthenticationPrincipal User currentUser);

    @Operation(summary = "Verificar inscrição", 
               description = "Verifica se um usuário está inscrito em um evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status de inscrição verificado",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Boolean> checkUserRegistration(
            @PathVariable UUID eventId,
            @RequestParam UUID userId);

    @Operation(summary = "Contar participantes confirmados", 
               description = "Retorna o número de participantes confirmados para um evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<Long> getConfirmedParticipantsCount(@PathVariable UUID eventId);
} 