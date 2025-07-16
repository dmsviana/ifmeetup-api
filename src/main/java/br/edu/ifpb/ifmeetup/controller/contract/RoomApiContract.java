package br.edu.ifpb.ifmeetup.controller.contract;

import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomRequest;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomStatusRequest;
import br.edu.ifpb.ifmeetup.dto.room.response.RoomResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Salas", description = "Endpoints para gerenciamento de salas e espaços físicos")
@SecurityRequirement(name = "JWT")
public interface RoomApiContract {

        @Operation(summary = "Criar nova sala", description = "Cria uma nova sala no sistema. Requer permissão de administrador ou gerente de salas.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Sala criada com sucesso", content = @Content(schema = @Schema(implementation = RoomResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para criar salas"),
                        @ApiResponse(responseCode = "409", description = "Sala já existe com mesmo nome e localização")
        })
        ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request);

        @Operation(summary = "Listar todas as salas", description = "Retorna lista paginada de todas as salas cadastradas")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de salas retornada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar salas")
        })
        ResponseEntity<Page<RoomResponse>> getAllRooms(
                        @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "name") String sort,
                        @Parameter(description = "Direção da ordenação (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction);

        @Operation(summary = "Buscar salas por status", description = "Retorna lista de salas filtradas por status operacional")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de salas retornada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar salas")
        })
        ResponseEntity<List<RoomResponse>> getRoomsByStatus(
                        @Parameter(description = "Status da sala", required = true) @PathVariable RoomStatus status);

        @Operation(summary = "Buscar salas por tipo", description = "Retorna lista de salas filtradas por tipo")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de salas retornada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar salas")
        })
        ResponseEntity<List<RoomResponse>> getRoomsByType(
                        @Parameter(description = "Tipo da sala", required = true) @PathVariable RoomType type);

        @Operation(summary = "Buscar salas por capacidade mínima", description = "Retorna lista de salas que comportam no mínimo o número especificado de pessoas")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de salas retornada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar salas")
        })
        ResponseEntity<List<RoomResponse>> getRoomsByMinCapacity(
                        @Parameter(description = "Capacidade mínima desejada", required = true) @PathVariable Integer minCapacity);

        @Operation(summary = "Buscar salas disponíveis em período", description = "Retorna lista de salas disponíveis em um período específico (sem conflitos de horário)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de salas disponíveis retornada com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Período inválido"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar disponibilidade")
        })
        ResponseEntity<List<RoomResponse>> getAvailableRooms(
                        @Parameter(description = "Data/hora de início (formato ISO 8601)", required = true, example = "2025-07-20T10:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
                        @Parameter(description = "Data/hora de término (formato ISO 8601)", required = true, example = "2025-07-20T12:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime);

        @Operation(summary = "Buscar salas por recurso", description = "Retorna lista de salas que possuem um recurso específico com quantidade mínima")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de salas retornada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar salas")
        })
        ResponseEntity<List<RoomResponse>> getRoomsByResource(
                        @Parameter(description = "Tipo do recurso", required = true) @PathVariable ResourceType resourceType,
                        @Parameter(description = "Quantidade mínima do recurso") @RequestParam(defaultValue = "1") Integer minQuantity);

        @Operation(summary = "Buscar sala por ID", description = "Retorna os detalhes completos de uma sala específica")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Sala encontrada", content = @Content(schema = @Schema(implementation = RoomResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar salas"),
                        @ApiResponse(responseCode = "404", description = "Sala não encontrada")
        })
        ResponseEntity<RoomResponse> getRoomById(
                        @Parameter(description = "ID da sala", required = true) @PathVariable UUID id);

        @Operation(summary = "Atualizar dados da sala", description = "Atualiza todas as informações de uma sala. Requer permissão de administrador ou gerente de salas.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Sala atualizada com sucesso", content = @Content(schema = @Schema(implementation = RoomResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar salas"),
                        @ApiResponse(responseCode = "404", description = "Sala não encontrada"),
                        @ApiResponse(responseCode = "409", description = "Conflito com outra sala existente")
        })
        ResponseEntity<RoomResponse> updateRoom(
                        @Parameter(description = "ID da sala", required = true) @PathVariable UUID id,
                        @Valid @RequestBody RoomRequest request);

        @Operation(summary = "Alterar status da sala", description = "Altera apenas o status operacional de uma sala. Requer permissão de administrador ou gerente de salas.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso", content = @Content(schema = @Schema(implementation = RoomResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Mudança de status inválida"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para alterar status"),
                        @ApiResponse(responseCode = "404", description = "Sala não encontrada")
        })
        ResponseEntity<RoomResponse> updateRoomStatus(
                        @Parameter(description = "ID da sala", required = true) @PathVariable UUID id,
                        @Valid @RequestBody RoomStatusRequest request);

        @Operation(summary = "Desabilitar sala", description = "Desabilita uma sala (soft delete). A sala permanece no sistema mas com status DISABLED. Requer permissão de administrador.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Sala desabilitada com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Sala possui eventos futuros agendados"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão para desabilitar salas"),
                        @ApiResponse(responseCode = "404", description = "Sala não encontrada")
        })
        ResponseEntity<Void> deleteRoom(
                        @Parameter(description = "ID da sala", required = true) @PathVariable UUID id);
}