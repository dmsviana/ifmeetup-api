package br.edu.ifpb.ifmeetup.controller.contract;

import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.dto.event.EventCreateRequest;
import br.edu.ifpb.ifmeetup.dto.event.EventRejectRequest;
import br.edu.ifpb.ifmeetup.dto.event.EventResponse;
import br.edu.ifpb.ifmeetup.dto.event.EventUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Eventos", description = "Endpoints para gerenciamento de eventos acadêmicos")
@SecurityRequirement(name = "JWT")
public interface EventApiContract {

    @Operation(summary = "Criar novo evento", description = "Cria um novo evento acadêmico. O evento será criado com status PENDING_APPROVAL e requer aprovação de um coordenador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso", content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou conflito de horário na sala"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar eventos"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário na sala selecionada")
    })
    ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Listar todos os eventos", description = "Retorna lista paginada de todos os eventos cadastrados no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar eventos")
    })
    ResponseEntity<Page<EventResponse>> getAllEvents(
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "startDateTime") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction);

    @Operation(summary = "Buscar eventos por status", description = "Retorna lista paginada de eventos filtrados por status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar eventos")
    })
    ResponseEntity<Page<EventResponse>> getEventsByStatus(
            @Parameter(description = "Status do evento", required = true) @PathVariable EventStatus status,
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "startDateTime") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction);

    @Operation(summary = "Buscar eventos por organizador", description = "Retorna lista paginada de eventos criados por um organizador específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar eventos do organizador"),
            @ApiResponse(responseCode = "404", description = "Organizador não encontrado")
    })
    ResponseEntity<Page<EventResponse>> getEventsByOrganizer(
            @Parameter(description = "ID do organizador", required = true) @PathVariable UUID organizerId,
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "startDateTime") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction);

    @Operation(summary = "Buscar eventos por sala", description = "Retorna lista paginada de eventos agendados para uma sala específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar eventos da sala"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    ResponseEntity<Page<EventResponse>> getEventsByRoom(
            @Parameter(description = "ID da sala", required = true) @PathVariable UUID roomId,
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "startDateTime") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction);

    @Operation(summary = "Buscar eventos por tipo", description = "Retorna lista paginada de eventos filtrados por tipo (palestra, workshop, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar eventos")
    })
    ResponseEntity<Page<EventResponse>> getEventsByType(
            @Parameter(description = "Tipo do evento", required = true) @PathVariable EventType eventType,
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "startDateTime") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction);

    @Operation(summary = "Buscar evento por ID", description = "Retorna os detalhes completos de um evento específico, incluindo número atual de participantes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado", content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar o evento"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<EventResponse> getEventById(
            @Parameter(description = "ID do evento", required = true) @PathVariable UUID id);

    @Operation(summary = "Atualizar evento", description = "Atualiza todas as informações de um evento. Apenas o organizador ou administradores podem atualizar. Eventos concluídos ou cancelados não podem ser atualizados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso", content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, evento não pode ser atualizado ou conflito de horário"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar o evento"),
            @ApiResponse(responseCode = "404", description = "Evento ou sala não encontrados"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário na sala selecionada")
    })
    ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "ID do evento", required = true) @PathVariable UUID id,
            @Valid @RequestBody EventUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Aprovar evento", description = "Aprova um evento pendente. Requer permissão de coordenador ou administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento aprovado com sucesso", content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Evento não pode ser aprovado (status inválido)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para aprovar eventos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<EventResponse> approveEvent(
            @Parameter(description = "ID do evento", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Rejeitar evento", description = "Rejeita um evento pendente com motivo. Requer permissão de coordenador ou administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento rejeitado com sucesso", content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Evento não pode ser rejeitado ou motivo não fornecido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para rejeitar eventos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<EventResponse> rejectEvent(
            @Parameter(description = "ID do evento", required = true) @PathVariable UUID id,
            @Valid @RequestBody EventRejectRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Cancelar evento (organizador)", description = "Permite ao organizador cancelar seu próprio evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento cancelado com sucesso", content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Evento não pode ser cancelado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Apenas o organizador pode cancelar o evento"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<EventResponse> cancelEventByOrganizer(
            @Parameter(description = "ID do evento", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Cancelar evento (administrador)", description = "Permite ao administrador cancelar qualquer evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento cancelado com sucesso", content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Evento não pode ser cancelado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para cancelar eventos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<EventResponse> cancelEventByAdmin(
            @Parameter(description = "ID do evento", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser);

    @Operation(summary = "Excluir evento", description = "Remove permanentemente um evento do sistema. Apenas administradores podem excluir eventos. Eventos com participantes não podem ser excluídos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Evento não pode ser excluído (possui participantes ou está em andamento)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir eventos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID do evento", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser);
}