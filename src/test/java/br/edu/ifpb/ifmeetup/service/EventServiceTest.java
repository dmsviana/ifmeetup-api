package br.edu.ifpb.ifmeetup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.entity.Role;
import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
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
import br.edu.ifpb.ifmeetup.service.impl.EventServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para EventService")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event testEvent;
    private Room testRoom;
    private User testOrganizer;
    private User testAdmin;
    private EventCreateRequest testCreateRequest;
    private EventUpdateRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        // Configurar organizador de teste
        testOrganizer = new User();
        testOrganizer.setId(UUID.randomUUID());
        testOrganizer.setFirstName("João");
        testOrganizer.setLastName("Silva");
        testOrganizer.setEmail("joao.silva@example.com");
        
        Role studentRole = new Role();
        studentRole.setName("STUDENT");
        testOrganizer.setRoles(Set.of(studentRole));

        // Configurar administrador de teste
        testAdmin = new User();
        testAdmin.setId(UUID.randomUUID());
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("Sistema");
        testAdmin.setEmail("admin@example.com");
        
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        testAdmin.setRoles(Set.of(adminRole));

        // Configurar sala de teste
        testRoom = new Room();
        testRoom.setId(UUID.randomUUID());
        testRoom.setName("Sala de Reuniões");
        testRoom.setLocation("Bloco A, 2º andar");
        testRoom.setCapacity(30);
        testRoom.setType(RoomType.MEETING_ROOM);
        testRoom.setStatus(RoomStatus.AVAILABLE);
        testRoom.setDescription("Sala para reuniões e apresentações");

        // Configurar evento de teste
        testEvent = new Event();
        testEvent.setId(UUID.randomUUID());
        testEvent.setTitle("Workshop de Spring Boot");
        testEvent.setDescription("Um workshop prático sobre desenvolvimento com Spring Boot");
        testEvent.setOrganizer(testOrganizer);
        testEvent.setRoom(testRoom);
        testEvent.setStartDateTime(LocalDateTime.now().plusDays(7));
        testEvent.setEndDateTime(LocalDateTime.now().plusDays(7).plusHours(3));
        testEvent.setMaxParticipants(25);
        testEvent.setEventType(EventType.WORKSHOP);
        testEvent.setPublicEvent(true);
        testEvent.setStatus(EventStatus.PENDING_APPROVAL);

        // Configurar request de criação de teste
        testCreateRequest = new EventCreateRequest(
                "Workshop de Spring Boot",
                "Um workshop prático sobre desenvolvimento com Spring Boot",
                testRoom.getId(),
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(7).plusHours(3),
                25,
                EventType.WORKSHOP,
                true
        );

        // Configurar request de atualização de teste
        testUpdateRequest = new EventUpdateRequest(
                "Workshop de Spring Boot - Atualizado",
                "Um workshop prático sobre desenvolvimento com Spring Boot - versão atualizada",
                testRoom.getId(),
                LocalDateTime.now().plusDays(8),
                LocalDateTime.now().plusDays(8).plusHours(4),
                20,
                EventType.WORKSHOP,
                EventStatus.PENDING_APPROVAL,
                true,
                null
        );
    }

    @Nested
    @DisplayName("Testes de criação de evento")
    class CreateEventTests {

        @Test
        @DisplayName("Deve criar evento com sucesso quando dados válidos são fornecidos")
        void shouldCreateEventSuccessfullyWithValidData() {
            // Arrange
            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(any(), any(), any())).thenReturn(Collections.emptyList());
            when(userRepository.findById(testOrganizer.getId())).thenReturn(Optional.of(testOrganizer));
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

            // Act
            EventResponse response = eventService.createEvent(testCreateRequest, testOrganizer);

            // Assert
            assertNotNull(response);
            assertEquals(testEvent.getId(), response.id());
            assertEquals(testEvent.getTitle(), response.title());
            assertEquals(testEvent.getDescription(), response.description());
            assertEquals(testEvent.getEventType(), response.eventType());
            assertEquals(EventStatus.PENDING_APPROVAL, response.status());
            assertEquals(0, response.currentParticipants());

            // Verificar que o método save foi chamado
            verify(eventRepository).save(any(Event.class));
            verify(userRepository).findById(testOrganizer.getId());

            // Verificar captura de argumentos
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());
            Event savedEvent = eventCaptor.getValue();

            assertEquals(testCreateRequest.title(), savedEvent.getTitle());
            assertEquals(testCreateRequest.description(), savedEvent.getDescription());
            assertEquals(testCreateRequest.maxParticipants(), savedEvent.getMaxParticipants());
            assertEquals(testCreateRequest.eventType(), savedEvent.getEventType());
            assertEquals(EventStatus.PENDING_APPROVAL, savedEvent.getStatus());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não existir")
        void shouldThrowExceptionWhenRoomDoesNotExist() {
            // Arrange
            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> eventService.createEvent(testCreateRequest, testOrganizer));

            assertTrue(exception.getMessage().contains("Sala não encontrada"));
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não estiver disponível")
        void shouldThrowExceptionWhenRoomIsNotAvailable() {
            // Arrange
            testRoom.setStatus(RoomStatus.UNDER_MAINTENANCE);
            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.createEvent(testCreateRequest, testOrganizer));

            assertEquals("A sala selecionada não está disponível para eventos", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando houver conflito de horário na sala")
        void shouldThrowExceptionWhenThereIsTimeConflictInRoom() {
            // Arrange
            Event conflictingEvent = new Event();
            conflictingEvent.setId(UUID.randomUUID());
            conflictingEvent.setStartDateTime(testCreateRequest.startDateTime().minusHours(1));
            conflictingEvent.setEndDateTime(testCreateRequest.endDateTime().plusHours(1));

            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(any(), any(), any())).thenReturn(List.of(conflictingEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.createEvent(testCreateRequest, testOrganizer));

            assertEquals("Já existe um evento agendado para esta sala no horário especificado", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando número de participantes exceder capacidade da sala")
        void shouldThrowExceptionWhenMaxParticipantsExceedsRoomCapacity() {
            // Arrange
            EventCreateRequest requestWithTooManyParticipants = new EventCreateRequest(
                    testCreateRequest.title(),
                    testCreateRequest.description(),
                    testCreateRequest.roomId(),
                    testCreateRequest.startDateTime(),
                    testCreateRequest.endDateTime(),
                    50, // Excede a capacidade da sala (30)
                    testCreateRequest.eventType(),
                    testCreateRequest.publicEvent()
            );

            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(any(), any(), any())).thenReturn(Collections.emptyList());

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.createEvent(requestWithTooManyParticipants, testOrganizer));

            assertTrue(exception.getMessage().contains("excede a capacidade da sala"));
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Testes de atualização de evento")
    class UpdateEventTests {

        @Test
        @DisplayName("Deve atualizar evento com sucesso quando organizador fornece dados válidos")
        void shouldUpdateEventSuccessfullyWhenOrganizerProvidesValidData() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(any(), any(), any())).thenReturn(Collections.emptyList());
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(eventParticipantRepository.countByEvent(any())).thenReturn(5L);

            // Act
            EventResponse response = eventService.updateEvent(testEvent.getId(), testUpdateRequest, testOrganizer);

            // Assert
            assertNotNull(response);
            verify(eventRepository).save(any(Event.class));

            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());
            Event updatedEvent = eventCaptor.getValue();

            assertEquals(testUpdateRequest.title(), updatedEvent.getTitle());
            assertEquals(testUpdateRequest.description(), updatedEvent.getDescription());
            assertEquals(testUpdateRequest.maxParticipants(), updatedEvent.getMaxParticipants());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não for organizador nem admin")
        void shouldThrowExceptionWhenUserIsNotOrganizerNorAdmin() {
            // Arrange
            User unauthorizedUser = new User();
            unauthorizedUser.setId(UUID.randomUUID());
            unauthorizedUser.setEmail("unauthorized@example.com");
            
            Role studentRole = new Role();
            studentRole.setName("STUDENT");
            unauthorizedUser.setRoles(Set.of(studentRole));

            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.updateEvent(testEvent.getId(), testUpdateRequest, unauthorizedUser));

            assertEquals("Apenas o organizador ou administradores podem atualizar eventos", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando tentar atualizar evento que já começou")
        void shouldThrowExceptionWhenTryingToUpdateEventThatAlreadyStarted() {
            // Arrange
            testEvent.setStartDateTime(LocalDateTime.now().minusHours(1)); // Evento já começou
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.updateEvent(testEvent.getId(), testUpdateRequest, testOrganizer));

            assertEquals("Eventos que já começaram não podem ser atualizados", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }

        @ParameterizedTest
        @EnumSource(value = EventStatus.class, names = {"CONCLUDED", "CANCELED_BY_ADMIN", "CANCELED_BY_ORGANIZER"})
        @DisplayName("Deve lançar exceção quando tentar atualizar evento com status final")
        void shouldThrowExceptionWhenTryingToUpdateEventWithFinalStatus(EventStatus finalStatus) {
            // Arrange
            testEvent.setStatus(finalStatus);
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.updateEvent(testEvent.getId(), testUpdateRequest, testOrganizer));

            assertEquals("Eventos concluídos ou cancelados não podem ser atualizados", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Testes de busca de evento por ID")
    class FindEventByIdTests {

        @Test
        @DisplayName("Deve encontrar evento por ID com sucesso quando evento existe")
        void shouldFindEventByIdSuccessfullyWhenEventExists() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.countByEvent(testEvent)).thenReturn(10L);

            // Act
            EventResponse response = eventService.findEventById(testEvent.getId());

            // Assert
            assertNotNull(response);
            assertEquals(testEvent.getId(), response.id());
            assertEquals(testEvent.getTitle(), response.title());
            assertEquals(testEvent.getDescription(), response.description());
            assertEquals(10, response.currentParticipants());

            verify(eventRepository).findById(testEvent.getId());
            verify(eventParticipantRepository).countByEvent(testEvent);
        }

        @Test
        @DisplayName("Deve lançar exceção quando evento não for encontrado por ID")
        void shouldThrowExceptionWhenEventWithSpecifiedIdDoesNotExist() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> eventService.findEventById(eventId));

            assertTrue(exception.getMessage().contains("Evento não encontrado"));
            verify(eventRepository).findById(eventId);
        }
    }

    @Nested
    @DisplayName("Testes de aprovação de evento")
    class ApproveEventTests {

        @Test
        @DisplayName("Deve aprovar evento com sucesso quando admin fornece dados válidos")
        void shouldApproveEventSuccessfullyWhenAdminProvidesValidData() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(any(), any(), any())).thenReturn(Collections.emptyList());
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(eventParticipantRepository.countByEvent(any())).thenReturn(0L);

            // Act
            EventResponse response = eventService.approveEvent(testEvent.getId(), testAdmin);

            // Assert
            assertNotNull(response);
            verify(eventRepository).save(any(Event.class));

            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());
            Event approvedEvent = eventCaptor.getValue();

            assertEquals(EventStatus.APPROVED, approvedEvent.getStatus());
            assertEquals(testAdmin, approvedEvent.getApprovedBy());
            assertNotNull(approvedEvent.getApprovalDateTime());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não for administrador")
        void shouldThrowExceptionWhenUserIsNotAdmin() {
            // Arrange - Não é necessário mock pois a validação de admin acontece antes da busca do evento

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.approveEvent(testEvent.getId(), testOrganizer));

            assertEquals("Apenas administradores podem executar esta operação", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando evento não estiver pendente de aprovação")
        void shouldThrowExceptionWhenEventIsNotPendingApproval() {
            // Arrange
            testEvent.setStatus(EventStatus.APPROVED);
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.approveEvent(testEvent.getId(), testAdmin));

            assertEquals("Apenas eventos pendentes podem ser aprovados", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando houver conflito de horário antes da aprovação")
        void shouldThrowExceptionWhenThereIsTimeConflictBeforeApproval() {
            // Arrange
            Event conflictingEvent = new Event();
            conflictingEvent.setId(UUID.randomUUID());

            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(any(), any(), any())).thenReturn(List.of(conflictingEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.approveEvent(testEvent.getId(), testAdmin));

            assertEquals("Não é possível aprovar: existe conflito de horário na sala", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Testes de rejeição de evento")
    class RejectEventTests {

        @Test
        @DisplayName("Deve rejeitar evento com sucesso quando admin fornece motivo válido")
        void shouldRejectEventSuccessfullyWhenAdminProvidesValidReason() {
            // Arrange
            String rejectionReason = "Não atende aos critérios de qualidade";
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(eventParticipantRepository.countByEvent(any())).thenReturn(0L);

            // Act
            EventResponse response = eventService.rejectEvent(testEvent.getId(), rejectionReason, testAdmin);

            // Assert
            assertNotNull(response);
            verify(eventRepository).save(any(Event.class));

            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());
            Event rejectedEvent = eventCaptor.getValue();

            assertEquals(EventStatus.REJECTED, rejectedEvent.getStatus());
            assertEquals(rejectionReason, rejectedEvent.getRejectionReason());
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo de rejeição não for fornecido")
        void shouldThrowExceptionWhenRejectionReasonIsNotProvided() {
            // Arrange

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.rejectEvent(testEvent.getId(), null, testAdmin));

            assertEquals("Motivo da rejeição é obrigatório", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo de rejeição estiver vazio")
        void shouldThrowExceptionWhenRejectionReasonIsEmpty() {
            // Arrange

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.rejectEvent(testEvent.getId(), "   ", testAdmin));

            assertEquals("Motivo da rejeição é obrigatório", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Testes de cancelamento de evento")
    class CancelEventTests {

        @Test
        @DisplayName("Deve cancelar evento com sucesso quando organizador cancela seu próprio evento")
        void shouldCancelEventSuccessfullyWhenOrganizerCancelsOwnEvent() {
            // Arrange
            testEvent.setStatus(EventStatus.APPROVED);
            String cancellationReason = "Mudança de cronograma";
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(eventParticipantRepository.countByEvent(any())).thenReturn(5L);

            // Act
            EventResponse response = eventService.cancelEvent(testEvent.getId(), cancellationReason, testOrganizer);

            // Assert
            assertNotNull(response);
            verify(eventRepository).save(any(Event.class));

            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());
            Event cancelledEvent = eventCaptor.getValue();

            assertEquals(EventStatus.CANCELED_BY_ORGANIZER, cancelledEvent.getStatus());
            assertEquals(cancellationReason, cancelledEvent.getRejectionReason());
        }

        @Test
        @DisplayName("Deve cancelar evento com sucesso quando admin cancela evento")
        void shouldCancelEventSuccessfullyWhenAdminCancelsEvent() {
            // Arrange
            testEvent.setStatus(EventStatus.APPROVED);
            String cancellationReason = "Violação de política";
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(eventParticipantRepository.countByEvent(any())).thenReturn(5L);

            // Act
            EventResponse response = eventService.cancelEvent(testEvent.getId(), cancellationReason, testAdmin);

            // Assert
            assertNotNull(response);
            verify(eventRepository).save(any(Event.class));

            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());
            Event cancelledEvent = eventCaptor.getValue();

            assertEquals(EventStatus.CANCELED_BY_ADMIN, cancelledEvent.getStatus());
            assertEquals(cancellationReason, cancelledEvent.getRejectionReason());
        }

        @ParameterizedTest
        @EnumSource(value = EventStatus.class, names = {"CONCLUDED", "CANCELED_BY_ADMIN", "CANCELED_BY_ORGANIZER"})
        @DisplayName("Deve lançar exceção quando tentar cancelar evento com status que não permite cancelamento")
        void shouldThrowExceptionWhenTryingToCancelEventWithStatusThatDoesNotAllowCancellation(EventStatus status) {
            // Arrange
            testEvent.setStatus(status);
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.cancelEvent(testEvent.getId(), "Motivo", testOrganizer));

            assertEquals("Este evento não pode ser cancelado", exception.getMessage());
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Testes de verificação de conflito de horário")
    class TimeConflictTests {

        @Test
        @DisplayName("Deve retornar true quando houver conflito de horário na sala")
        void shouldReturnTrueWhenThereIsTimeConflictInRoom() {
            // Arrange
            Event conflictingEvent = new Event();
            conflictingEvent.setId(UUID.randomUUID());
            
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = startTime.plusHours(2);

            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(testRoom, startTime, endTime))
                    .thenReturn(List.of(conflictingEvent));

            // Act
            boolean hasConflict = eventService.hasTimeConflictInRoom(testRoom.getId(), startTime, endTime, null);

            // Assert
            assertTrue(hasConflict);
            verify(eventRepository).findConflictingEventsInRoom(testRoom, startTime, endTime);
        }

        @Test
        @DisplayName("Deve retornar false quando não houver conflito de horário na sala")
        void shouldReturnFalseWhenThereIsNoTimeConflictInRoom() {
            // Arrange
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = startTime.plusHours(2);

            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(testRoom, startTime, endTime))
                    .thenReturn(Collections.emptyList());

            // Act
            boolean hasConflict = eventService.hasTimeConflictInRoom(testRoom.getId(), startTime, endTime, null);

            // Assert
            assertFalse(hasConflict);
            verify(eventRepository).findConflictingEventsInRoom(testRoom, startTime, endTime);
        }

        @Test
        @DisplayName("Deve excluir evento específico da verificação de conflito")
        void shouldExcludeSpecificEventFromConflictCheck() {
            // Arrange
            UUID excludeEventId = UUID.randomUUID();
            Event conflictingEvent = new Event();
            conflictingEvent.setId(excludeEventId);
            
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = startTime.plusHours(2);

            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
            when(eventRepository.findConflictingEventsInRoom(testRoom, startTime, endTime))
                    .thenReturn(List.of(conflictingEvent));

            // Act
            boolean hasConflict = eventService.hasTimeConflictInRoom(testRoom.getId(), startTime, endTime, excludeEventId);

            // Assert
            assertFalse(hasConflict); // Não deve haver conflito pois o evento foi excluído
            verify(eventRepository).findConflictingEventsInRoom(testRoom, startTime, endTime);
        }
    }

    @Nested
    @DisplayName("Testes de busca de eventos por status")
    class FindEventsByStatusTests {

        @Test
        @DisplayName("Deve encontrar eventos por status com sucesso")
        void shouldFindEventsByStatusSuccessfully() {
            // Arrange
            EventProjection eventProjection = mock(EventProjection.class);
            when(eventProjection.getId()).thenReturn(testEvent.getId());
            when(eventProjection.getTitle()).thenReturn(testEvent.getTitle());
            when(eventProjection.getStatus()).thenReturn(EventStatus.APPROVED);

            when(eventRepository.findProjectedByStatus(EventStatus.APPROVED))
                    .thenReturn(List.of(eventProjection));
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.countByEvent(testEvent)).thenReturn(15L);

            // Act
            List<EventResponse> responses = eventService.findEventsByStatus(EventStatus.APPROVED);

            // Assert
            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(testEvent.getId(), responses.get(0).id());
            assertEquals(15, responses.get(0).currentParticipants());

            verify(eventRepository).findProjectedByStatus(EventStatus.APPROVED);
            verify(eventParticipantRepository).countByEvent(testEvent);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver eventos com status especificado")
        void shouldReturnEmptyListWhenNoEventsWithSpecifiedStatus() {
            // Arrange
            when(eventRepository.findProjectedByStatus(EventStatus.CONCLUDED))
                    .thenReturn(Collections.emptyList());

            // Act
            List<EventResponse> responses = eventService.findEventsByStatus(EventStatus.CONCLUDED);

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(eventRepository).findProjectedByStatus(EventStatus.CONCLUDED);
        }
    }

    @Nested
    @DisplayName("Testes de contagem de eventos por status")
    class CountEventsByStatusTests {

        @ParameterizedTest
        @MethodSource("eventStatusProvider")
        @DisplayName("Deve contar eventos por status corretamente")
        void shouldCountEventsByStatusCorrectly(EventStatus status, long expectedCount) {
            // Arrange
            when(eventRepository.countByStatus(status)).thenReturn(expectedCount);

            // Act
            long actualCount = eventService.countEventsByStatus(status);

            // Assert
            assertEquals(expectedCount, actualCount);
            verify(eventRepository).countByStatus(status);
        }

        static Stream<Arguments> eventStatusProvider() {
            return Stream.of(
                    Arguments.of(EventStatus.PENDING_APPROVAL, 5L),
                    Arguments.of(EventStatus.APPROVED, 10L),
                    Arguments.of(EventStatus.REJECTED, 2L),
                    Arguments.of(EventStatus.CONCLUDED, 15L),
                    Arguments.of(EventStatus.CANCELED_BY_ORGANIZER, 1L),
                    Arguments.of(EventStatus.CANCELED_BY_ADMIN, 0L),
                    Arguments.of(EventStatus.IN_PROGRESS, 3L)
            );
        }
    }

    @Nested
    @DisplayName("Testes de exclusão de evento")
    class DeleteEventTests {

        @Test
        @DisplayName("Deve excluir evento com sucesso quando organizador exclui evento sem participantes")
        void shouldDeleteEventSuccessfullyWhenOrganizerDeletesEventWithoutParticipants() {
            // Arrange
            testEvent.setStatus(EventStatus.PENDING_APPROVAL);
            testEvent.setStartDateTime(LocalDateTime.now().plusDays(1)); // Evento no futuro
            
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.countByEvent(testEvent)).thenReturn(0L);
            doNothing().when(eventRepository).delete(testEvent);

            // Act
            eventService.deleteEvent(testEvent.getId(), testOrganizer);

            // Assert
            verify(eventRepository).delete(testEvent);
            verify(eventParticipantRepository).countByEvent(testEvent);
        }

        @Test
        @DisplayName("Deve lançar exceção quando tentar excluir evento com participantes confirmados")
        void shouldThrowExceptionWhenTryingToDeleteEventWithConfirmedParticipants() {
            // Arrange
            testEvent.setStatus(EventStatus.APPROVED);
            testEvent.setStartDateTime(LocalDateTime.now().plusDays(1)); // Evento no futuro
            
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.countByEvent(testEvent)).thenReturn(5L);

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.deleteEvent(testEvent.getId(), testOrganizer));

            assertEquals("Não é possível excluir evento com participantes confirmados", exception.getMessage());
            verify(eventRepository, never()).delete(any(Event.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando tentar excluir evento que já começou")
        void shouldThrowExceptionWhenTryingToDeleteEventThatAlreadyStarted() {
            // Arrange
            testEvent.setStartDateTime(LocalDateTime.now().minusHours(1)); // Evento já começou
            testEvent.setStatus(EventStatus.IN_PROGRESS);
            
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.deleteEvent(testEvent.getId(), testOrganizer));

            assertTrue(exception.getMessage().contains("que já começaram não podem ser excluídos"));
            verify(eventRepository, never()).delete(any(Event.class));
        }

        @ParameterizedTest
        @EnumSource(value = EventStatus.class, names = {"CONCLUDED", "IN_PROGRESS"})
        @DisplayName("Deve lançar exceção quando tentar excluir evento com status que não permite exclusão")
        void shouldThrowExceptionWhenTryingToDeleteEventWithStatusThatDoesNotAllowDeletion(EventStatus status) {
            // Arrange
            testEvent.setStatus(status);
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventService.deleteEvent(testEvent.getId(), testOrganizer));

            assertTrue(exception.getMessage().contains("não podem ser excluídos"));
            verify(eventRepository, never()).delete(any(Event.class));
        }
    }
} 