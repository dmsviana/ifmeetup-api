package br.edu.ifpb.ifmeetup.controller;

import br.edu.ifpb.ifmeetup.controller.contract.EventParticipantApiContract;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import br.edu.ifpb.ifmeetup.dto.event.EventParticipantResponse;
import br.edu.ifpb.ifmeetup.dto.event.FeedbackRequest;
import br.edu.ifpb.ifmeetup.service.event.EventParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventParticipantController implements EventParticipantApiContract {

    private final EventParticipantService participantService;

    @Override
    @PostMapping("/{eventId}/register")
    @PreAuthorize("hasAuthority('EVENT_REGISTER_SELF')")
    public ResponseEntity<EventParticipantResponse> registerForEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User currentUser) {
        log.info("Usuário {} se inscrevendo no evento {}", currentUser.getEmail(), eventId);
        
        EventParticipantResponse response = participantService.registerParticipant(eventId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @DeleteMapping("/{eventId}/registration")
    @PreAuthorize("hasAuthority('EVENT_REGISTER_SELF')")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User currentUser) {
        log.info("Usuário {} cancelando inscrição no evento {}", currentUser.getEmail(), eventId);
        
        participantService.cancelRegistration(eventId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{eventId}/attendance")
    @PreAuthorize("hasAuthority('EVENT_MANAGE_PARTICIPANTS')")
    public ResponseEntity<EventParticipantResponse> updateAttendanceStatus(
            @PathVariable UUID eventId,
            @RequestParam UUID userId,
            @RequestParam AttendanceStatus status,
            @AuthenticationPrincipal User currentUser) {
        log.info("Usuário {} atualizando status de presença do usuário {} no evento {} para {}", 
                currentUser.getEmail(), userId, eventId, status);
        
        EventParticipantResponse response = participantService.updatedAttendanceStatus(
                eventId, userId, status, currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{eventId}/feedback")
    @PreAuthorize("hasAuthority('EVENT_REGISTER_SELF')")
    public ResponseEntity<EventParticipantResponse> provideFeedback(
            @PathVariable UUID eventId,
            @Valid @RequestBody FeedbackRequest feedbackRequest,
            @AuthenticationPrincipal User currentUser) {
        log.info("Usuário {} fornecendo feedback para o evento {}", currentUser.getEmail(), eventId);
        
        EventParticipantResponse response = participantService.provideFeedback(
                eventId, feedbackRequest.feedback(), currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{eventId}/participants")
    @PreAuthorize("hasAuthority('EVENT_VIEW_PARTICIPANTS')")
    public ResponseEntity<List<EventParticipantResponse>> getEventParticipants(@PathVariable UUID eventId) {
        log.debug("Buscando participantes do evento {}", eventId);
        
        List<EventParticipantResponse> participants = participantService.findParticipantsByEvent(eventId);
        return ResponseEntity.ok(participants);
    }

    @Override
    @GetMapping("/{eventId}/participants/by-status")
    @PreAuthorize("hasAuthority('EVENT_VIEW_PARTICIPANTS')")
    public ResponseEntity<List<EventParticipantResponse>> getEventParticipantsByStatus(
            @PathVariable UUID eventId,
            @RequestParam AttendanceStatus status) {
        log.debug("Buscando participantes do evento {} com status {}", eventId, status);
        
        List<EventParticipantResponse> participants = participantService.findParticipantsByEventAndStatus(
                eventId, status);
        return ResponseEntity.ok(participants);
    }

    @Override
    @GetMapping("/participants/events")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<List<EventParticipantResponse>> getUserEvents(@RequestParam UUID userId) {
        log.debug("Buscando eventos do usuário {}", userId);
        
        List<EventParticipantResponse> events = participantService.findEventsByParticipant(userId);
        return ResponseEntity.ok(events);
    }

    @Override
    @GetMapping("/participants/my-events")
    @PreAuthorize("hasAuthority('EVENT_REGISTER_SELF')")
    public ResponseEntity<List<EventParticipantResponse>> getMyEvents(@AuthenticationPrincipal User currentUser) {
        log.debug("Buscando eventos do usuário {}", currentUser.getEmail());
        
        List<EventParticipantResponse> events = participantService.findMyEvents(currentUser);
        return ResponseEntity.ok(events);
    }

    @Override
    @GetMapping("/{eventId}/registration/check")
    @PreAuthorize("hasAuthority('EVENT_VIEW_PARTICIPANTS') or #userId == authentication.principal.id")
    public ResponseEntity<Boolean> checkUserRegistration(
            @PathVariable UUID eventId,
            @RequestParam UUID userId) {
        log.debug("Verificando inscrição do usuário {} no evento {}", userId, eventId);
        
        boolean isRegistered = participantService.isUserRegistered(eventId, userId);
        return ResponseEntity.ok(isRegistered);
    }

    @Override
    @GetMapping("/{eventId}/participants/count")
    @PreAuthorize("hasAuthority('EVENT_VIEW_PARTICIPANTS')")
    public ResponseEntity<Long> getConfirmedParticipantsCount(@PathVariable UUID eventId) {
        log.debug("Contando participantes confirmados do evento {}", eventId);
        
        long count = participantService.countConfirmedParticipants(eventId);
        return ResponseEntity.ok(count);
    }
} 