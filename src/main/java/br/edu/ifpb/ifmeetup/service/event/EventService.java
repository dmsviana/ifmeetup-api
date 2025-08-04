package br.edu.ifpb.ifmeetup.service.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.ifpb.ifmeetup.config.SchedulingConfig;
import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.projection.EventProjection;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.EventParticipantRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.EventRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.RoomRepository;
import br.edu.ifpb.ifmeetup.dto.event.EventCreateRequest;
import br.edu.ifpb.ifmeetup.dto.event.EventResponse;
import br.edu.ifpb.ifmeetup.dto.event.EventUpdateRequest;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final SchedulingConfig schedulingConfig;


	private final EventRepository eventRepository;
	private final RoomRepository roomRepository;
	private final UserRepository userRepository;
	private final EventParticipantRepository participantRepository;

	@Transactional
	public EventResponse createEvent(EventCreateRequest request, User organizer) {
		log.info("Criando evento: {} organizado por: {}", request.title(), organizer.getEmail());

		Room room = findRoomEntityById(request.roomId());
		validateRoomAvailability(room);
		
		if (hasTimeConflictInRoom(request.roomId(), request.startDateTime(), request.endDateTime(), null)) {
			
			throw new BusinessValidationException("Já existe um evento agendado para esta sala no horário escolhido");
		}
		
		if (request.maxParticipants() > room.getCapacity()) {
			
			throw new BusinessValidationException(
					String.format("O número máximo de participantes (%d) é maior que a capacidade da sala (%d)", 
							request.maxParticipants(), room.getCapacity()));
			
		}
		
		User managedOrganizer = userRepository.findById(organizer.getId())
				.orElseThrow(() -> new BusinessValidationException("Organizador não encontrado"));
		
		Event event = new Event();
		
		event.setTitle(request.title());
		event.setDescription(request.description());
		event.setOrganizer(managedOrganizer);
		event.setRoom(room);
		event.setStartDateTime(request.startDateTime());
		event.setEndDateTime(request.endDateTime());
		event.setMaxParticipants(request.maxParticipants());
		event.setEventType(request.eventType());
		event.setPublicEvent(request.publicEvent());
		event.setStatus(EventStatus.PENDING_APPROVAL);
		
		Event savedEvent = eventRepository.save(event);
		log.info("Evento criado com ID: {}", savedEvent.getId());
		
		// usa projection para manter consistência com outros endpoints (sem roles/permissions)
		return EventResponse.fromProjection(findEventProjectionById(savedEvent.getId()), 0);

	}
	
	@Transactional
	public EventResponse updateEvent(UUID eventId, EventUpdateRequest request, User currentUser) {
		log.info("Atualizando evento ID: {} por usuário: {}", eventId, currentUser.getEmail());
		
		Event event = findEventEntityById(eventId);
		
		validateUpdatePermissions(event, currentUser);
		
		if (!event.isUpdatable()) {
			throw new BusinessValidationException("Eventos concluídos ou cancelados não podem ser atualizados");
		}
		
		if (event.hasRoomOrTimeChanged(request.roomId(), request.startDateTime(), request.endDateTime())) {
			
			Room newRoom = findRoomEntityById(request.roomId());
			validateRoomAvailability(newRoom);
			
			if (hasTimeConflictInRoom(request.roomId(), request.startDateTime(), request.endDateTime(), eventId)) {
				throw new BusinessValidationException("Já existe um evento agendado para esta sala no horário informado");
			}
			
			if (request.maxParticipants() > newRoom.getCapacity()) {
				
				throw new BusinessValidationException(
						String.format("O número de participantes (%d) ultrapassa a capacidade da sala (%d)", 
								request.maxParticipants(), newRoom.getCapacity()));
			}
			
			event.setRoom(newRoom);
			
		}
		
		event.setTitle(request.title());
		event.setDescription(request.description());
		event.setStartDateTime(request.startDateTime());
		event.setEndDateTime(request.endDateTime());
		event.setMaxParticipants(request.maxParticipants());
		event.setEventType(request.eventType());
		event.setPublicEvent(request.publicEvent());
		
		if (isAdmin(currentUser) && !event.getStatus().equals(request.status())) {
			updateEventStatus(event, request.status(), request.rejectionReason(), currentUser);
		}
		
		Event updatedEvent = eventRepository.save(event);
		log.info("Evento atualizado: {}", updatedEvent.getId());
		
		int currentParticipants = (int) participantRepository.countByEvent(updatedEvent);
		// usa projection para manter consistência (sem roles/permissions)
		return EventResponse.fromProjection(findEventProjectionById(eventId), currentParticipants);
		
	}
	
	@Transactional(readOnly = true)
	public EventResponse findEventById(UUID eventId) {
		log.info("Buscando evento ID: {}", eventId);
		
		Event event = findEventEntityById(eventId);
		int currentParticipants = (int) participantRepository.countByEvent(event);
		
		// usa projection para manter consistência (sem roles/permissions)
		return EventResponse.fromProjection(findEventProjectionById(eventId), currentParticipants);
		
	}
	
	@Transactional(readOnly = true)
	public Page<EventResponse> findAllEvents(Pageable pageable) {
		log.debug("Listando todos os eventos - página: {}, tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());

		return eventRepository.findAllProjectedWithParticipants(pageable)
			.map(projection -> EventResponse.fromProjection(projection, projection.getCurrentParticipants().intValue()));
	}
	
	@Transactional(readOnly = true)
	public Page<EventResponse> findEventsByStatus(EventStatus status, Pageable pageable) {
		log.debug("Listando eventos por status: {} - página: {}, tamanho: {}", 
				status, pageable.getPageNumber(), pageable.getPageSize());

		return eventRepository.findProjectedByStatusWithParticipants(status, pageable)
			.map(projection -> EventResponse.fromProjection(projection, projection.getCurrentParticipants().intValue()));
	}
	
	@Transactional(readOnly = true)
	public Page<EventResponse> findEventsByOrganizer(User organizer, Pageable pageable) {
		log.debug("Listando eventos por organizador: {} - página: {}, tamanho: {}", 
				organizer.getEmail(), pageable.getPageNumber(), pageable.getPageSize());

		return eventRepository.findProjectedByOrganizerWithParticipants(organizer, pageable)
			.map(projection -> EventResponse.fromProjection(projection, projection.getCurrentParticipants().intValue()));
	}
	
	@Transactional(readOnly = true)
	public Page<EventResponse> findEventsByRoom(Room room, Pageable pageable) {
		log.debug("Listando eventos por sala: {} - página: {}, tamanho: {}", 
				room.getName(), pageable.getPageNumber(), pageable.getPageSize());

		return eventRepository.findProjectedByRoomWithParticipants(room, pageable)
			.map(projection -> EventResponse.fromProjection(projection, projection.getCurrentParticipants().intValue()));
	}
	
	@Transactional(readOnly = true)
	public Page<EventResponse> findEventsByEventType(EventType eventType, Pageable pageable) {
		log.debug("Listando eventos por tipo: {} - página: {}, tamanho: {}", 
				eventType, pageable.getPageNumber(), pageable.getPageSize());

		return eventRepository.findProjectedByEventTypeWithParticipants(eventType, pageable)
			.map(projection -> EventResponse.fromProjection(projection, projection.getCurrentParticipants().intValue()));
	}
	
	private void updateEventStatus(Event event, EventStatus newStatus, String rejectionReason, User currentUser) {
		
		switch (newStatus) {
		
		case APPROVED -> event.approve(currentUser, LocalDateTime.now());
		case REJECTED -> {
			
			try {
				event.reject(rejectionReason);
			} catch (IllegalArgumentException ex) {
				throw new BusinessValidationException(ex.getMessage());
			}
		}
		case CANCELED_BY_ADMIN -> event.cancelByAdmin(currentUser);
		case CANCELED_BY_ORGANIZER -> event.cancelByOrganizer();
		default -> event.changeStatus(newStatus);
		}
		
	}
	


	public boolean hasTimeConflictInRoom(UUID roomId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			UUID excludeEventId) {

		Room room = findRoomEntityById(roomId);

		List<Event> conflictingEvents = eventRepository.findConflictingEventsInRoom(room, startDateTime, endDateTime);

		if (excludeEventId != null) {
			
			conflictingEvents = conflictingEvents
					.stream()
					.filter(event -> !event.getId().equals(excludeEventId))
					.collect(Collectors.toList());
		}
		
		boolean hasConflict = !conflictingEvents.isEmpty();
		if (hasConflict) {
			log.warn("Conflito de horário encontrado na sala {} entre {} e {}", roomId, startDateTime, endDateTime);
			log.warn("Eventos conflitantes encontrados:");
			conflictingEvents.forEach(event -> 
				log.warn("- Evento ID: {}, Título: '{}', Status: {}, Período: {} até {}", 
					event.getId(), event.getTitle(), event.getStatus(), 
					event.getStartDateTime(), event.getEndDateTime())
			);
		}
		
		return hasConflict;

	}

	private void validateRoomAvailability(Room room) {

		if (room.getStatus() != RoomStatus.AVAILABLE) {
			throw new BusinessValidationException("A sala selecionada não está disponível");
		}
	}

	private Room findRoomEntityById(UUID id) {
		return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com o ID: " + id));
	}
	
	private void validateUpdatePermissions(Event event, User currentUser) {
		
		if (!isAdmin(currentUser) && !event.getOrganizer().getId().equals(currentUser.getId())) {
			throw new BusinessValidationException("Apenas o organizador ou administradores podem atualizar eventos");
		}
	}
	
	private void validateEventCanBeUpdated(Event event) {
		
		if (!event.getStatus().isUpdatable()) {
			throw new BusinessValidationException("Eventos concluídos ou cancelados não podem ser atualizados");
		}
		
		if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
			throw new BusinessValidationException("Eventos que já começaram não pode ser atualizados.");
		}
	}
	
	private boolean isAdmin(User user) {
		return user.getRoles().stream()
				.anyMatch(role -> "ADMIN".equals(role.getName()));
	}
	
	private void validateAdminPermission(User user) {
        if (!isAdmin(user)) {
            throw new BusinessValidationException("Apenas administradores podem executar esta operação");
        }
    }
	
	@Transactional
	public EventResponse approveEvent(UUID eventId, User currentUser) {
		log.info("Aprovando evento ID: {} por usuário: {}", eventId, currentUser.getEmail());
		
		Event event = findEventEntityById(eventId);
		
		if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
			throw new BusinessValidationException("Não é possível aprovar eventos com datas anteriores a atual");
		}

		try {
			event.approve(currentUser, LocalDateTime.now());
			Event updatedEvent = eventRepository.save(event);
			
			int currentParticipants = (int) participantRepository.countByEvent(updatedEvent);
			
			return EventResponse.fromProjection(findEventProjectionById(eventId), currentParticipants);
			
		} catch (IllegalStateException ex) {
			throw new BusinessValidationException(ex.getMessage());
		}
		
		
	}
	
	@Transactional
	public EventResponse rejectEvent(UUID eventId, String rejectionReason, User currentUser) {
		log.info("Rejeitando evento ID: {} por usuário: {} - motivo: {}", eventId, currentUser.getEmail(), rejectionReason);
		
		Event event = findEventEntityById(eventId);
		
		event.reject(rejectionReason);
		Event updatedEvent = eventRepository.save(event);
		
		int currentParticipants = (int) participantRepository.countByEvent(updatedEvent);
		return EventResponse.fromProjection(findEventProjectionById(eventId), currentParticipants);
	}
	
	@Transactional
	public void deleteEvent(UUID eventId, User currentUser) {
		log.info("Excluindo evento ID: {} por usuário: {}", eventId, currentUser.getEmail());
		
		// validação de permissão é feita no controller via @PreAuthorize
		Event event = findEventEntityById(eventId);
		
		// verifica se o evento pode ser excluído
		long participantCount = participantRepository.countByEvent(event);
		if (participantCount > 0) {
			throw new BusinessValidationException("Não é possível excluir eventos que possuem participantes");
		}
		
		// verifica se o evento não está em andamento ou concluído
		if (event.getStatus() == EventStatus.IN_PROGRESS) {
			throw new BusinessValidationException("Não é possível excluir eventos em andamento");
		}
		
		if (event.getStatus() == EventStatus.CONCLUDED) {
			throw new BusinessValidationException("Não é possível excluir eventos já concluídos");
		}
		
		eventRepository.delete(event);
		log.info("Evento excluído com sucesso: {}", eventId);
	}
	
	@Transactional
	public EventResponse cancelEventByOrganizer(UUID eventId, User currentUser) {
		log.info("Cancelando evento ID: {} pelo organizador: {}", eventId, currentUser.getEmail());
		
		Event event = findEventEntityById(eventId);
		
		// verifica se é o organizador
		if (!event.getOrganizer().getId().equals(currentUser.getId())) {
			throw new BusinessValidationException("Apenas o organizador pode cancelar o evento");
		}
		
		event.cancelByOrganizer();
		Event updatedEvent = eventRepository.save(event);
		
		int currentParticipants = (int) participantRepository.countByEvent(updatedEvent);
		return EventResponse.fromProjection(findEventProjectionById(eventId), currentParticipants);
	}
	
	@Transactional
	public EventResponse cancelEventByAdmin(UUID eventId, User currentUser) {
		log.info("Cancelando evento ID: {} pelo administrador: {}", eventId, currentUser.getEmail());
		
		validateAdminPermission(currentUser);
		Event event = findEventEntityById(eventId);
		
		event.cancelByAdmin(currentUser);
		Event updatedEvent = eventRepository.save(event);
		
		int currentParticipants = (int) participantRepository.countByEvent(updatedEvent);
		return EventResponse.fromProjection(findEventProjectionById(eventId), currentParticipants);
	}

	private Event findEventEntityById(UUID eventId) {
		return eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + eventId));
	}
	
	private EventProjection findEventProjectionById(UUID eventId) {
		return eventRepository.findProjectedById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado com ID: " + eventId));
	}

}
