package br.edu.ifpb.ifmeetup.controller;

import br.edu.ifpb.ifmeetup.controller.contract.RoomApiContract;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomRequest;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomStatusRequest;
import br.edu.ifpb.ifmeetup.dto.room.response.RoomResponse;
import br.edu.ifpb.ifmeetup.service.room.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController implements RoomApiContract {

    private final RoomService roomService;

    @Override
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN_ACCESS', 'ROOM_MANAGE_RESERVATIONS')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        log.info("Requisição para criar nova sala: {}", request.name());
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROOM_VIEW_ALL', 'ROOM_VIEW_AVAILABILITY')")
    public ResponseEntity<Page<RoomResponse>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("Requisição para listar salas - página: {}, tamanho: {}", page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<RoomResponse> response = roomService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ROOM_VIEW_ALL', 'ROOM_VIEW_AVAILABILITY')")
    public ResponseEntity<List<RoomResponse>> getRoomsByStatus(@PathVariable RoomStatus status) {
        log.info("Requisição para buscar salas por status: {}", status);
        List<RoomResponse> response = roomService.findByStatus(status);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyAuthority('ROOM_VIEW_ALL', 'ROOM_VIEW_AVAILABILITY')")
    public ResponseEntity<List<RoomResponse>> getRoomsByType(@PathVariable RoomType type) {
        log.info("Requisição para buscar salas por tipo: {}", type);
        List<RoomResponse> response = roomService.findByType(type);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/capacity/{minCapacity}")
    @PreAuthorize("hasAnyAuthority('ROOM_VIEW_ALL', 'ROOM_VIEW_AVAILABILITY')")
    public ResponseEntity<List<RoomResponse>> getRoomsByMinCapacity(@PathVariable Integer minCapacity) {
        log.info("Requisição para buscar salas com capacidade mínima: {}", minCapacity);
        List<RoomResponse> response = roomService.findByMinCapacity(minCapacity);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/available")
    @PreAuthorize("hasAnyAuthority('ROOM_VIEW_ALL', 'ROOM_VIEW_AVAILABILITY', 'ROOM_RESERVE')")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {

        log.info("Requisição para buscar salas disponíveis entre {} e {}", startDateTime, endDateTime);
        List<RoomResponse> response = roomService.findAvailableRooms(startDateTime, endDateTime);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/resource/{resourceType}")
    @PreAuthorize("hasAnyAuthority('ROOM_VIEW_ALL', 'ROOM_VIEW_AVAILABILITY')")
    public ResponseEntity<List<RoomResponse>> getRoomsByResource(
            @PathVariable ResourceType resourceType,
            @RequestParam(defaultValue = "1") Integer minQuantity) {

        log.info("Requisição para buscar salas com recurso {} (quantidade mínima: {})",
                resourceType, minQuantity);

        List<RoomResponse> response = roomService.findByResource(resourceType, minQuantity);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROOM_VIEW_ALL', 'ROOM_VIEW_AVAILABILITY')")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable UUID id) {
        log.info("Requisição para buscar sala por ID: {}", id);
        RoomResponse response = roomService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_ACCESS', 'ROOM_MANAGE_RESERVATIONS')")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody RoomRequest request) {

        log.info("Requisição para atualizar sala ID: {}", id);
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN_ACCESS', 'ROOM_MANAGE_RESERVATIONS')")
    public ResponseEntity<RoomResponse> updateRoomStatus(
            @PathVariable UUID id,
            @Valid @RequestBody RoomStatusRequest request) {

        log.info("Requisição para alterar status da sala ID: {} para {}", id, request.status());
        RoomResponse response = roomService.updateRoomStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        log.info("Requisição para desabilitar sala ID: {}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}