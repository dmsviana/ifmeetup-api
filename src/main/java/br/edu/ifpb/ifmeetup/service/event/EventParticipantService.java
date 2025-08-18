package br.edu.ifpb.ifmeetup.service.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.entity.EventParticipant;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.EventParticipantRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.EventRepository;
import br.edu.ifpb.ifmeetup.dto.event.EventParticipantResponse;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventParticipantService {

    private final EventParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;


    @Transactional
    public EventParticipantResponse registerParticipant(UUID eventId, User currentUser) {
        log.info("Registrando usuário {} no evento {}", currentUser.getId(), eventId);

        Event event = findEventById(eventId);
        validateEventRegistration(event, currentUser);

        // corrige o problema de inscrever-se -> cancelar inscrição -> inscrever-se novamente
        // buscar ou criar participação
        EventParticipant participant = participantRepository
                .findByEventIdAndUserId(eventId, currentUser.getId())
                .orElse(new EventParticipant());

        // configurar/resetar dados (funciona tanto para novo quanto para reativação)
        participant.setEvent(event);
        participant.setUser(currentUser);
        participant.setAttendanceStatus(AttendanceStatus.REGISTERED);
        participant.setRegistrationDateTime(LocalDateTime.now());
        participant.setCertificateIssued(false);
        participant.setFeedback(null);

        EventParticipant saved = participantRepository.save(participant);
        return EventParticipantResponse.fromEntity(saved);
    }

    @Transactional
    public void cancelRegistration(UUID eventId, User currentUser) {
        log.info("Cancelando inscrição do usuário {} para o evento {}", currentUser.getId(), eventId);

        Event event = findEventById(eventId);

        EventParticipant participant = findParticipantByEventAndUser(eventId, currentUser.getId());

        validateCancellation(event, participant);

        participant.setAttendanceStatus(AttendanceStatus.CANCELED);
        participantRepository.save(participant);

        log.info("Inscrição cancelada para o usuário {} no evento {}", currentUser.getId(), eventId);

    }


    public EventParticipantResponse updatedAttendanceStatus(UUID eventId, UUID userId, AttendanceStatus status,
                                                            User updatedBy) {
        log.info("Atualizando status de presença para o usuário {} no evento {} para {}", userId, eventId, status);

        Event event = findEventById(eventId);
        User user = findUserById(userId);

        EventParticipant participant = findParticipantByEventAndUser(eventId, userId);

        if (!isEventOrganizer(event, updatedBy) && !isAdmin(updatedBy)) {
            throw new BusinessValidationException(
                    "Apenas o organizador do evento ou administradores podem atualizar o status de presença");
        }

        if (participant.getAttendanceStatus() == AttendanceStatus.CANCELED) {
            throw new BusinessValidationException("Não é possível alterar o status de uma inscrição cancelada");
        }

        participant.setAttendanceStatus(status);
        EventParticipant updatedParticipant = participantRepository.save(participant);

        log.info("Status de presença atualizado com sucesso");
        return EventParticipantResponse.fromEntity(updatedParticipant);

    }

    private boolean isAdmin(User user) {

        return user.getAuthorities()
                .stream().anyMatch(authority -> authority.getAuthority().equals("ADMIN_ACCESS"));

    }

    public EventParticipantResponse provideFeedback(UUID eventId, String feedback, User currentUser) {
        log.info("Usuário {} fornecendo feedback para o evento {}", currentUser.getId(), eventId);

        Event event = findEventById(eventId);
        EventParticipant participant = findParticipantByEventAndUser(eventId, currentUser.getId());

        if (event.getEndDateTime().isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException("Feedback só pode ser fornecido após o término do evento");
        }

        if (participant.getAttendanceStatus() != AttendanceStatus.PRESENT) {
            throw new BusinessValidationException(
                    "Apenas participantes que compareceram ao evento podem fornecer feedback");
        }

        participant.setFeedback(feedback);
        EventParticipant updatedParticipant = participantRepository.save(participant);

        log.info("Feedback salvo com sucesso");
        return EventParticipantResponse.fromEntity(updatedParticipant);
    }

    public List<EventParticipantResponse> findParticipantsByEvent(UUID eventId) {

        log.info("Buscando participantes do evento {}", eventId);

        Event event = findEventById(eventId);

        return participantRepository.findProjectedByEvent(event)
                .stream()
                .map(EventParticipantResponse::fromProjection)
                .collect(Collectors.toList());

    }

    public List<EventParticipantResponse> findParticipantsByEventAndStatus(UUID eventId, AttendanceStatus status) {
        log.info("Buscando participantes do evento {} com status {}", eventId, status);

        Event event = findEventById(eventId);

        return participantRepository.findProjectedByEventAndAttendanceStatus(event, status).stream()
                .map(EventParticipantResponse::fromProjection)
                .collect(Collectors.toList());
    }

    public List<EventParticipantResponse> findEventsByParticipant(UUID userId) {
        log.info("Buscando eventos do participante {}", userId);

        User user = findUserById(userId);

        return participantRepository.findProjectedByUserWithEventDetails(user).stream()
                .map(EventParticipantResponse::fromProjection)
                .collect(Collectors.toList());
    }

    public List<EventParticipantResponse> findMyEvents(User currentUser) {
        return findEventsByParticipant(currentUser.getId());
    }

    public boolean isUserRegistered(UUID eventId, UUID userId) {

        // verifica se existe participação ativa (não cancelada)
        return participantRepository.findByEventIdAndUserId(eventId, userId)
                .map(participant -> participant.getAttendanceStatus() != AttendanceStatus.CANCELED)
                .orElse(false);

    }

    public long countConfirmedParticipants(UUID eventId) {

        Event event = findEventById(eventId);
        return participantRepository.countByEventAndAttendanceStatus(event, AttendanceStatus.REGISTERED);

    }

    private Event findEventById(UUID eventId) {

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com o ID:" + eventId));

    }

    private User findUserById(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID:" + userId));

    }

    private EventParticipant findParticipantByEventAndUser(UUID eventId, UUID userId) {

        return participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participação não encontrada para o evento:" + eventId + " e usuário:" + userId));

    }

    private void validateEventRegistration(Event event, User currentUser) {

        // verifica se o status do evento permite registro
        if (event.getStatus() != EventStatus.APPROVED) {

            String message = switch (event.getStatus()) {

                case PENDING_APPROVAL -> "Não é possível se inscrever em evento pendente de aprovação";

                case REJECTED -> "Não é possível se inscrever em evento rejeitado";

                case CANCELED_BY_ORGANIZER -> "Não é possível se inscrever em evento cancelado pelo organizador";

                case CANCELED_BY_ADMIN -> "Não é possível se inscrever em evento cancelado pelo administrador";

                case CONCLUDED -> "Não é possível se inscrever em evento já concluído";

                case IN_PROGRESS -> "Não é possível se inscrever em evento já em andamento";

                default -> "Não é possível se inscrever neste evento devido ao seu status atual";

            };

            throw new BusinessValidationException(message);
        }

        if (isUserRegistered(event.getId(), currentUser.getId())) {
            throw new BusinessValidationException("Usuário já está inscrito neste evento");
        }

        if (event.getMaxParticipants() != null && event.getMaxParticipants() > 0) {

            long currentCount = participantRepository.countByEvent(event);

            if (currentCount >= event.getMaxParticipants()) {
                throw new BusinessValidationException("Evento lotado");
            }

        }

        if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessValidationException("Não é possível se inscrever em evento já iniciado");
        }

    }

    private void validateCancellation(Event event, EventParticipant participant) {

        // não permitir cancelamento se o evento já começou (faz sentido!?)
        if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessValidationException("Não é possível cancelar inscrição após início do evento");
        }

        // não permitir cancelamento se já está cancelado
        if (participant.getAttendanceStatus() == AttendanceStatus.CANCELED) {
            throw new BusinessValidationException("A inscrição já está cancelada");
        }

    }

    private boolean isEventOrganizer(Event event, User user) {
        return event.getOrganizer().getId().equals(user.getId());
    }

}
