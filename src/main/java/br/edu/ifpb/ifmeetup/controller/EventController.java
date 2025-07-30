package br.edu.ifpb.ifmeetup.controller;

import br.edu.ifpb.ifmeetup.controller.contract.EventApiContract;
import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.RoomRepository;
import br.edu.ifpb.ifmeetup.dto.event.EventCreateRequest;
import br.edu.ifpb.ifmeetup.dto.event.EventRejectRequest;
import br.edu.ifpb.ifmeetup.dto.event.EventResponse;
import br.edu.ifpb.ifmeetup.dto.event.EventUpdateRequest;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import br.edu.ifpb.ifmeetup.service.event.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController implements EventApiContract {

    private final EventService eventService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Override
    @PostMapping
    @PreAuthorize("hasAnyAuthority('EVENT_CREATE')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Requisição para criar novo evento: {} por usuário: {}", request.title(), currentUser.getEmail());
        
        EventResponse response = eventService.createEvent(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAnyAuthority('EVENT_VIEW_ALL', 'EVENT_VIEW_PUBLISHED')")
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("Requisição para listar eventos - página: {}, tamanho: {}", page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<EventResponse> response = eventService.findAllEvents(pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('EVENT_VIEW_ALL', 'EVENT_VIEW_PUBLISHED')")
    public ResponseEntity<Page<EventResponse>> getEventsByStatus(
            @PathVariable EventStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("Requisição para buscar eventos por status: {}", status);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<EventResponse> response = eventService.findEventsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/organizer/{organizerId}")
    @PreAuthorize("hasAnyAuthority('EVENT_VIEW_ALL') or @eventController.isCurrentUserOrAdmin(#organizerId, authentication.principal)")
    public ResponseEntity<Page<EventResponse>> getEventsByOrganizer(
            @PathVariable UUID organizerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("Requisição para buscar eventos por organizador: {}", organizerId);

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizador não encontrado com ID: " + organizerId));

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<EventResponse> response = eventService.findEventsByOrganizer(organizer, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyAuthority('EVENT_VIEW_ALL', 'EVENT_VIEW_PUBLISHED')")
    public ResponseEntity<Page<EventResponse>> getEventsByRoom(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("Requisição para buscar eventos por sala: {}", roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com ID: " + roomId));

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<EventResponse> response = eventService.findEventsByRoom(room, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/type/{eventType}")
    @PreAuthorize("hasAnyAuthority('EVENT_VIEW_ALL', 'EVENT_VIEW_PUBLISHED')")
    public ResponseEntity<Page<EventResponse>> getEventsByType(
            @PathVariable EventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("Requisição para buscar eventos por tipo: {}", eventType);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<EventResponse> response = eventService.findEventsByEventType(eventType, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EVENT_VIEW_ALL', 'EVENT_VIEW_PUBLISHED')")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        log.info("Requisição para buscar evento por ID: {}", id);
        
        EventResponse response = eventService.findEventById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EVENT_EDIT') and (@eventController.isEventOrganizer(#id, authentication.principal) or hasAnyAuthority('ADMIN_ACCESS'))")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Requisição para atualizar evento ID: {} por usuário: {}", id, currentUser.getEmail());
        
        EventResponse response = eventService.updateEvent(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('EVENT_APPROVE', 'ADMIN_ACCESS')")
    public ResponseEntity<EventResponse> approveEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        log.info("Requisição para aprovar evento ID: {} por usuário: {}", id, currentUser.getEmail());
        
        EventResponse response = eventService.approveEvent(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('EVENT_APPROVE', 'ADMIN_ACCESS')")
    public ResponseEntity<EventResponse> rejectEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventRejectRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Requisição para rejeitar evento ID: {} por usuário: {}", id, currentUser.getEmail());
        
        EventResponse response = eventService.rejectEvent(id, request.rejectionReason(), currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('EVENT_EDIT') and @eventController.isEventOrganizer(#id, authentication.principal)")
    public ResponseEntity<EventResponse> cancelEventByOrganizer(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        log.info("Requisição para cancelar evento ID: {} pelo organizador: {}", id, currentUser.getEmail());
        
        EventResponse response = eventService.cancelEventByOrganizer(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/cancel-admin")
    @PreAuthorize("hasAnyAuthority('ADMIN_ACCESS')")
    public ResponseEntity<EventResponse> cancelEventByAdmin(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        log.info("Requisição para cancelar evento ID: {} pelo administrador: {}", id, currentUser.getEmail());
        
        EventResponse response = eventService.cancelEventByAdmin(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EVENT_DELETE') and (@eventController.isEventOrganizer(#id, authentication.principal) or hasAnyAuthority('ADMIN_ACCESS'))")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        log.info("Requisição para excluir evento ID: {} por usuário: {}", id, currentUser.getEmail());
        
        eventService.deleteEvent(id, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    // métodos para que as spEL funcionem (n sei se é boa prática deixar aqui no controller)

    /**
     * Verifica se o usuário atual é o mesmo do ID fornecido ou é um administrador.
     * Usado para controle de acesso em SpEL - deve ser público para funcionar.
     */
    public boolean isCurrentUserOrAdmin(UUID userId, User currentUser) {
        try {
            // verifica se é o próprio usuário
            if (currentUser.getId().equals(userId)) {
                return true;
            }
            
            // verifica se é admin
            return currentUser.getRoles().stream()
                    .anyMatch(role -> "ADMIN".equals(role.getName()));
                    
        } catch (Exception e) {
            log.warn("Erro ao verificar permissões do usuário: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário atual é o organizador do evento.
     * Usado para controle de acesso em SpEL - deve ser público para funcionar.
     */
    public boolean isEventOrganizer(UUID eventId, User currentUser) {
        try {
            EventResponse event = eventService.findEventById(eventId);
            return event.organizer().id().equals(currentUser.getId());
        } catch (Exception e) {
            log.warn("Erro ao verificar se usuário é organizador do evento: {}", e.getMessage());
            return false;
        }
    }
}