package br.edu.ifpb.ifmeetup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.edu.ifpb.ifmeetup.domain.entity.Event;
import br.edu.ifpb.ifmeetup.domain.entity.EventParticipant;
import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.domain.projection.EventParticipantProjection;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.EventParticipantRepository;
import br.edu.ifpb.ifmeetup.domain.repository.event.EventRepository;
import br.edu.ifpb.ifmeetup.dto.event.EventParticipantResponse;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import br.edu.ifpb.ifmeetup.service.impl.EventParticipantServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes para EventParticipantService")
class EventParticipantServiceTest {

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventParticipantServiceImpl eventParticipantService;

    private Event testEvent;
    private Event rejectedEvent;
    private Event canceledEvent;
    private Event concludedEvent;
    private Event inProgressEvent;
    private Event eventWithoutVacancies;
    private Event pastEvent;
    private Room testRoom;
    @Mock
    private User testUser;
    @Mock
    private User testOrganizer;
    @Mock
    private User testAdmin;
    @Mock
    private User testCoordinator;
    private EventParticipant testParticipant;
    private EventParticipantProjection testProjection;

    @BeforeEach
    void setUp() {
        setupUsers();
        setupRoom();
        setupEvents();
        setupParticipant();
        setupProjection();
    }

    private void setupUsers() {
        // Configurar usuário comum
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(testUser.getId()).thenReturn(userId);
        when(testUser.getFirstName()).thenReturn("João");
        when(testUser.getLastName()).thenReturn("Silva");
        when(testUser.getEmail()).thenReturn("joao.silva@example.com");
        when(testUser.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("EVENT_REGISTER_SELF")));

        // Configurar organizador do evento  
        UUID organizerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(testOrganizer.getId()).thenReturn(organizerId);
        when(testOrganizer.getFirstName()).thenReturn("Organizador");
        when(testOrganizer.getLastName()).thenReturn("Teste");
        when(testOrganizer.getEmail()).thenReturn("organizador@example.com");
        when(testOrganizer.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("EVENT_CREATE")));

        // Configurar administrador
        UUID adminId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        when(testAdmin.getId()).thenReturn(adminId);
        when(testAdmin.getFirstName()).thenReturn("Admin");
        when(testAdmin.getLastName()).thenReturn("Sistema");
        when(testAdmin.getEmail()).thenReturn("admin@example.com");
        when(testAdmin.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ADMIN_ACCESS")));

        // Configurar coordenador
        UUID coordinatorId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        when(testCoordinator.getId()).thenReturn(coordinatorId);
        when(testCoordinator.getFirstName()).thenReturn("Coordenador");
        when(testCoordinator.getLastName()).thenReturn("Teste");
        when(testCoordinator.getEmail()).thenReturn("coordenador@example.com");
        when(testCoordinator.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("EVENT_MANAGE_PARTICIPANTS")));
    }

    private void setupRoom() {
        testRoom = new Room();
        testRoom.setId(UUID.randomUUID());
        testRoom.setName("Sala de Reuniões");
        testRoom.setLocation("Bloco A, 2º andar");
        testRoom.setCapacity(30);
        testRoom.setType(RoomType.MEETING_ROOM);
        testRoom.setStatus(RoomStatus.AVAILABLE);
    }

    private void setupEvents() {
        // Evento aprovado (válido para registro)
        testEvent = new Event();
        testEvent.setId(UUID.randomUUID());
        testEvent.setTitle("Workshop de Spring Boot");
        testEvent.setDescription("Workshop prático");
        testEvent.setOrganizer(testOrganizer);
        testEvent.setRoom(testRoom);
        testEvent.setStartDateTime(LocalDateTime.now().plusDays(7));
        testEvent.setEndDateTime(LocalDateTime.now().plusDays(7).plusHours(3));
        testEvent.setMaxParticipants(25);
        testEvent.setEventType(EventType.WORKSHOP);
        testEvent.setPublicEvent(true);
        testEvent.setStatus(EventStatus.APPROVED);

        // Evento rejeitado
        rejectedEvent = new Event();
        rejectedEvent.setId(UUID.randomUUID());
        rejectedEvent.setTitle("Evento Rejeitado");
        rejectedEvent.setOrganizer(testOrganizer);
        rejectedEvent.setRoom(testRoom);
        rejectedEvent.setStartDateTime(LocalDateTime.now().plusDays(7));
        rejectedEvent.setEndDateTime(LocalDateTime.now().plusDays(7).plusHours(3));
        rejectedEvent.setStatus(EventStatus.REJECTED);

        // Evento cancelado
        canceledEvent = new Event();
        canceledEvent.setId(UUID.randomUUID());
        canceledEvent.setTitle("Evento Cancelado");
        canceledEvent.setOrganizer(testOrganizer);
        canceledEvent.setRoom(testRoom);
        canceledEvent.setStartDateTime(LocalDateTime.now().plusDays(7));
        canceledEvent.setEndDateTime(LocalDateTime.now().plusDays(7).plusHours(3));
        canceledEvent.setStatus(EventStatus.CANCELED_BY_ADMIN);

        // Evento concluído
        concludedEvent = new Event();
        concludedEvent.setId(UUID.randomUUID());
        concludedEvent.setTitle("Evento Concluído");
        concludedEvent.setOrganizer(testOrganizer);
        concludedEvent.setRoom(testRoom);
        concludedEvent.setStartDateTime(LocalDateTime.now().minusDays(1));
        concludedEvent.setEndDateTime(LocalDateTime.now().minusHours(2));
        concludedEvent.setStatus(EventStatus.CONCLUDED);

        // Evento em progresso
        inProgressEvent = new Event();
        inProgressEvent.setId(UUID.randomUUID());
        inProgressEvent.setTitle("Evento Em Progresso");
        inProgressEvent.setOrganizer(testOrganizer);
        inProgressEvent.setRoom(testRoom);
        inProgressEvent.setStartDateTime(LocalDateTime.now().minusHours(1));
        inProgressEvent.setEndDateTime(LocalDateTime.now().plusHours(2));
        inProgressEvent.setStatus(EventStatus.IN_PROGRESS);

        // Evento sem vagas
        eventWithoutVacancies = new Event();
        eventWithoutVacancies.setId(UUID.randomUUID());
        eventWithoutVacancies.setTitle("Evento Lotado");
        eventWithoutVacancies.setOrganizer(testOrganizer);
        eventWithoutVacancies.setRoom(testRoom);
        eventWithoutVacancies.setStartDateTime(LocalDateTime.now().plusDays(7));
        eventWithoutVacancies.setEndDateTime(LocalDateTime.now().plusDays(7).plusHours(3));
        eventWithoutVacancies.setMaxParticipants(1);
        eventWithoutVacancies.setStatus(EventStatus.APPROVED);

        // Evento no passado
        pastEvent = new Event();
        pastEvent.setId(UUID.randomUUID());
        pastEvent.setTitle("Evento Passado");
        pastEvent.setOrganizer(testOrganizer);
        pastEvent.setRoom(testRoom);
        pastEvent.setStartDateTime(LocalDateTime.now().minusDays(1));
        pastEvent.setEndDateTime(LocalDateTime.now().minusHours(1));
        pastEvent.setStatus(EventStatus.APPROVED);
    }

    private void setupParticipant() {
        testParticipant = new EventParticipant();
        testParticipant.setId(UUID.randomUUID());
        testParticipant.setEvent(testEvent);
        testParticipant.setUser(testUser);
        testParticipant.setRegistrationDateTime(LocalDateTime.now());
        testParticipant.setAttendanceStatus(AttendanceStatus.REGISTERED);
        testParticipant.setCertificateIssued(false);
    }

    private void setupProjection() {
        testProjection = mock(EventParticipantProjection.class);
        UUID participantId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        
        when(testProjection.getId()).thenReturn(participantId);
        when(testProjection.getRegistrationDateTime()).thenReturn(LocalDateTime.now());
        when(testProjection.getAttendanceStatus()).thenReturn(AttendanceStatus.REGISTERED);
        when(testProjection.isCertificateIssued()).thenReturn(false);
        when(testProjection.getFeedback()).thenReturn(null);
        when(testProjection.getEventId()).thenReturn(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        when(testProjection.getEventTitle()).thenReturn("Workshop de Spring Boot");
        when(testProjection.getUserId()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(testProjection.getUserFirstName()).thenReturn("João");
        when(testProjection.getUserLastName()).thenReturn("Silva");
        when(testProjection.getUserEmail()).thenReturn("joao.silva@example.com");
    }

    @Nested
    @DisplayName("Testes de registro de participante")
    class RegisterParticipantTests {

        @Test
        @DisplayName("Deve registrar participante com sucesso quando dados válidos são fornecidos")
        void shouldRegisterParticipantSuccessfullyWithValidData() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.existsByEventAndUser(testEvent, testUser)).thenReturn(false);
            when(eventParticipantRepository.countByEvent(testEvent)).thenReturn(10L);
            when(eventParticipantRepository.save(any(EventParticipant.class))).thenReturn(testParticipant);

            // Act
            EventParticipantResponse response = eventParticipantService.registerParticipant(testEvent.getId(), testUser);

            // Assert
            assertNotNull(response);
            assertEquals(testParticipant.getId(), response.id());
            assertEquals(AttendanceStatus.REGISTERED, response.attendanceStatus());
            assertEquals(testEvent.getTitle(), response.event().title());
            assertEquals(testUser.getFirstName() + " " + testUser.getLastName(), response.user().name());

            verify(eventParticipantRepository).save(any(EventParticipant.class));
            
            ArgumentCaptor<EventParticipant> participantCaptor = ArgumentCaptor.forClass(EventParticipant.class);
            verify(eventParticipantRepository).save(participantCaptor.capture());
            EventParticipant savedParticipant = participantCaptor.getValue();
            
            assertEquals(testEvent, savedParticipant.getEvent());
            assertEquals(testUser, savedParticipant.getUser());
            assertEquals(AttendanceStatus.REGISTERED, savedParticipant.getAttendanceStatus());
        }

        @Test
        @DisplayName("Deve lançar exceção quando evento não for encontrado")
        void shouldThrowExceptionWhenEventNotFound() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> eventParticipantService.registerParticipant(eventId, testUser));

            assertTrue(exception.getMessage().contains("Evento não encontrado"));
            verify(eventParticipantRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = EventStatus.class, names = {"APPROVED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Deve lançar exceção quando evento não estiver aprovado")
        void shouldThrowExceptionWhenEventNotApproved(EventStatus status) {
            // Arrange
            testEvent.setStatus(status);
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.registerParticipant(testEvent.getId(), testUser));

            assertTrue(exception.getMessage().contains("Não é possível se inscrever"));
            verify(eventParticipantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário já estiver inscrito")
        void shouldThrowExceptionWhenUserAlreadyRegistered() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.existsByEventAndUser(testEvent, testUser)).thenReturn(true);

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.registerParticipant(testEvent.getId(), testUser));

            assertEquals("Usuário já está inscrito neste evento", exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando evento estiver lotado")
        void shouldThrowExceptionWhenEventIsFull() {
            // Arrange
            when(eventRepository.findById(eventWithoutVacancies.getId())).thenReturn(Optional.of(eventWithoutVacancies));
            when(eventParticipantRepository.existsByEventAndUser(eventWithoutVacancies, testUser)).thenReturn(false);
            when(eventParticipantRepository.countByEvent(eventWithoutVacancies)).thenReturn(1L);

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.registerParticipant(eventWithoutVacancies.getId(), testUser));

            assertEquals("Evento lotado", exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando evento já tiver começado")
        void shouldThrowExceptionWhenEventAlreadyStarted() {
            // Arrange
            when(eventRepository.findById(pastEvent.getId())).thenReturn(Optional.of(pastEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.registerParticipant(pastEvent.getId(), testUser));

            assertEquals("Não é possível se inscrever em evento já iniciado", exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de cancelamento de registro")
    class CancelRegistrationTests {

        @Test
        @DisplayName("Deve cancelar registro com sucesso quando dados válidos são fornecidos")
        void shouldCancelRegistrationSuccessfullyWithValidData() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));
            when(eventParticipantRepository.save(any(EventParticipant.class))).thenReturn(testParticipant);

            // Act
            eventParticipantService.cancelRegistration(testEvent.getId(), testUser);

            // Assert
            verify(eventParticipantRepository).save(any(EventParticipant.class));
            
            ArgumentCaptor<EventParticipant> participantCaptor = ArgumentCaptor.forClass(EventParticipant.class);
            verify(eventParticipantRepository).save(participantCaptor.capture());
            EventParticipant canceledParticipant = participantCaptor.getValue();
            
            assertEquals(AttendanceStatus.CANCELED, canceledParticipant.getAttendanceStatus());
        }

        @Test
        @DisplayName("Deve lançar exceção quando participação não for encontrada")
        void shouldThrowExceptionWhenParticipationNotFound() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> eventParticipantService.cancelRegistration(testEvent.getId(), testUser));

            assertTrue(exception.getMessage().contains("Participação não encontrada"));
            verify(eventParticipantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tentar cancelar após início do evento")
        void shouldThrowExceptionWhenTryingToCancelAfterEventStart() {
            // Arrange
            when(eventRepository.findById(pastEvent.getId())).thenReturn(Optional.of(pastEvent));
            when(eventParticipantRepository.findByEventIdAndUserId(pastEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.cancelRegistration(pastEvent.getId(), testUser));

            assertEquals("Não é possível cancelar inscrição após início do evento", exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando inscrição já estiver cancelada")
        void shouldThrowExceptionWhenRegistrationAlreadyCanceled() {
            // Arrange
            testParticipant.setAttendanceStatus(AttendanceStatus.CANCELED);
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.cancelRegistration(testEvent.getId(), testUser));

            assertEquals("A inscrição já está cancelada", exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de atualização de status de presença")
    class UpdateAttendanceStatusTests {

        @Test
        @DisplayName("Deve atualizar status com sucesso quando organizador do evento atualiza")
        void shouldUpdateStatusSuccessfullyWhenEventOrganizerUpdates() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));
            when(eventParticipantRepository.save(any(EventParticipant.class))).thenReturn(testParticipant);

            // Act
            EventParticipantResponse response = eventParticipantService.updatedAttendanceStatus(
                    testEvent.getId(), testUser.getId(), AttendanceStatus.PRESENT, testOrganizer);

            // Assert
            assertNotNull(response);
            verify(eventParticipantRepository).save(any(EventParticipant.class));
        }

        @Test
        @DisplayName("Deve atualizar status com sucesso quando admin atualiza")
        void shouldUpdateStatusSuccessfullyWhenAdminUpdates() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));
            when(eventParticipantRepository.save(any(EventParticipant.class))).thenReturn(testParticipant);

            // Act
            EventParticipantResponse response = eventParticipantService.updatedAttendanceStatus(
                    testEvent.getId(), testUser.getId(), AttendanceStatus.PRESENT, testAdmin);

            // Assert
            assertNotNull(response);
            verify(eventParticipantRepository).save(any(EventParticipant.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não tem permissão")
        void shouldThrowExceptionWhenUserHasNoPermission() {
            // Arrange
            User unauthorizedUser = mock(User.class);
            when(unauthorizedUser.getId()).thenReturn(UUID.randomUUID());
            when(unauthorizedUser.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("EVENT_REGISTER_SELF")));
            
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.updatedAttendanceStatus(
                            testEvent.getId(), testUser.getId(), AttendanceStatus.PRESENT, unauthorizedUser));

            assertEquals("Apenas o organizador do evento ou administradores podem atualizar o status de presença", 
                    exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tentar alterar status de inscrição cancelada")
        void shouldThrowExceptionWhenTryingToUpdateCanceledRegistration() {
            // Arrange
            testParticipant.setAttendanceStatus(AttendanceStatus.CANCELED);
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.updatedAttendanceStatus(
                            testEvent.getId(), testUser.getId(), AttendanceStatus.PRESENT, testOrganizer));

            assertEquals("Não é possível alterar o status de uma inscrição cancelada", exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de fornecimento de feedback")
    class ProvideFeedbackTests {

        @Test
        @DisplayName("Deve fornecer feedback com sucesso quando participante presente fornece após evento concluído")
        void shouldProvideFeedbackSuccessfullyWhenPresentParticipantProvidesAfterEventEnd() {
            // Arrange
            testParticipant.setAttendanceStatus(AttendanceStatus.PRESENT);
            String feedback = "Excelente evento, muito bem organizado!";
            
            when(eventRepository.findById(concludedEvent.getId())).thenReturn(Optional.of(concludedEvent));
            when(eventParticipantRepository.findByEventIdAndUserId(concludedEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));
            when(eventParticipantRepository.save(any(EventParticipant.class))).thenReturn(testParticipant);

            // Act
            EventParticipantResponse response = eventParticipantService.provideFeedback(
                    concludedEvent.getId(), feedback, testUser);

            // Assert
            assertNotNull(response);
            verify(eventParticipantRepository).save(any(EventParticipant.class));
            
            ArgumentCaptor<EventParticipant> participantCaptor = ArgumentCaptor.forClass(EventParticipant.class);
            verify(eventParticipantRepository).save(participantCaptor.capture());
            EventParticipant updatedParticipant = participantCaptor.getValue();
            
            assertEquals(feedback, updatedParticipant.getFeedback());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tentar fornecer feedback antes do fim do evento")
        void shouldThrowExceptionWhenTryingToProvideFeedbackBeforeEventEnd() {
            // Arrange
            String feedback = "Feedback prematuro";
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.findByEventIdAndUserId(testEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.provideFeedback(testEvent.getId(), feedback, testUser));

            assertEquals("Feedback só pode ser fornecido após o término do evento", exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando participante não compareceu ao evento")
        void shouldThrowExceptionWhenParticipantDidNotAttend() {
            // Arrange
            testParticipant.setAttendanceStatus(AttendanceStatus.ABSENT);
            String feedback = "Feedback inválido";
            
            when(eventRepository.findById(concludedEvent.getId())).thenReturn(Optional.of(concludedEvent));
            when(eventParticipantRepository.findByEventIdAndUserId(concludedEvent.getId(), testUser.getId()))
                    .thenReturn(Optional.of(testParticipant));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.provideFeedback(concludedEvent.getId(), feedback, testUser));

            assertEquals("Apenas participantes que compareceram ao evento podem fornecer feedback", 
                    exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de busca de participantes")
    class FindParticipantsTests {

        @Test
        @DisplayName("Deve encontrar participantes do evento com sucesso")
        void shouldFindEventParticipantsSuccessfully() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.findProjectedByEvent(testEvent))
                    .thenReturn(List.of(testProjection));

            // Act
            List<EventParticipantResponse> participants = eventParticipantService.findParticipantsByEvent(testEvent.getId());

            // Assert
            assertNotNull(participants);
            assertEquals(1, participants.size());
            assertEquals(testProjection.getId(), participants.get(0).id());
            verify(eventParticipantRepository).findProjectedByEvent(testEvent);
        }

        @Test
        @DisplayName("Deve encontrar participantes por status com sucesso")
        void shouldFindParticipantsByStatusSuccessfully() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.findProjectedByEventAndAttendanceStatus(testEvent, AttendanceStatus.PRESENT))
                    .thenReturn(List.of(testProjection));

            // Act
            List<EventParticipantResponse> participants = eventParticipantService.findParticipantsByEventAndStatus(
                    testEvent.getId(), AttendanceStatus.PRESENT);

            // Assert
            assertNotNull(participants);
            assertEquals(1, participants.size());
            verify(eventParticipantRepository).findProjectedByEventAndAttendanceStatus(testEvent, AttendanceStatus.PRESENT);
        }

        @Test
        @DisplayName("Deve encontrar eventos do participante com sucesso")
        void shouldFindEventsByParticipantSuccessfully() {
            // Arrange
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.findProjectedByUserWithEventDetails(testUser))
                    .thenReturn(List.of(testProjection));

            // Act
            List<EventParticipantResponse> events = eventParticipantService.findEventsByParticipant(testUser.getId());

            // Assert
            assertNotNull(events);
            assertEquals(1, events.size());
            verify(eventParticipantRepository).findProjectedByUserWithEventDetails(testUser);
        }

        @Test
        @DisplayName("Deve encontrar meus eventos com sucesso")
        void shouldFindMyEventsSuccessfully() {
            // Arrange
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.findProjectedByUserWithEventDetails(testUser))
                    .thenReturn(List.of(testProjection));

            // Act
            List<EventParticipantResponse> events = eventParticipantService.findMyEvents(testUser);

            // Assert
            assertNotNull(events);
            assertEquals(1, events.size());
        }
    }

    @Nested
    @DisplayName("Testes de verificação de registro")
    class RegistrationCheckTests {

        @Test
        @DisplayName("Deve retornar true quando usuário estiver registrado")
        void shouldReturnTrueWhenUserIsRegistered() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.existsByEventAndUser(testEvent, testUser)).thenReturn(true);

            // Act
            boolean isRegistered = eventParticipantService.isUserRegistered(testEvent.getId(), testUser.getId());

            // Assert
            assertTrue(isRegistered);
            verify(eventParticipantRepository).existsByEventAndUser(testEvent, testUser);
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não estiver registrado")
        void shouldReturnFalseWhenUserIsNotRegistered() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(eventParticipantRepository.existsByEventAndUser(testEvent, testUser)).thenReturn(false);

            // Act
            boolean isRegistered = eventParticipantService.isUserRegistered(testEvent.getId(), testUser.getId());

            // Assert
            assertFalse(isRegistered);
            verify(eventParticipantRepository).existsByEventAndUser(testEvent, testUser);
        }
    }

    @Nested
    @DisplayName("Testes de contagem de participantes")
    class CountParticipantsTests {

        @Test
        @DisplayName("Deve contar participantes confirmados corretamente")
        void shouldCountConfirmedParticipantsCorrectly() {
            // Arrange
            long expectedCount = 15L;
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.countByEventAndAttendanceStatus(testEvent, AttendanceStatus.REGISTERED))
                    .thenReturn(expectedCount);

            // Act
            long count = eventParticipantService.countConfirmedParticipants(testEvent.getId());

            // Assert
            assertEquals(expectedCount, count);
            verify(eventParticipantRepository).countByEventAndAttendanceStatus(testEvent, AttendanceStatus.REGISTERED);
        }

        @Test
        @DisplayName("Deve retornar zero quando não houver participantes confirmados")
        void shouldReturnZeroWhenNoConfirmedParticipants() {
            // Arrange
            when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
            when(eventParticipantRepository.countByEventAndAttendanceStatus(testEvent, AttendanceStatus.REGISTERED))
                    .thenReturn(0L);

            // Act
            long count = eventParticipantService.countConfirmedParticipants(testEvent.getId());

            // Assert
            assertEquals(0L, count);
        }
    }

    @Nested
    @DisplayName("Testes de cenários específicos com status de eventos")
    class EventStatusSpecificTests {

        @ParameterizedTest
        @MethodSource("invalidEventStatusProvider")
        @DisplayName("Deve lançar exceção específica para cada status inválido de evento")
        void shouldThrowSpecificExceptionForEachInvalidEventStatus(EventStatus status, String expectedMessage) {
            // Arrange
            Event invalidEvent = new Event();
            invalidEvent.setId(UUID.randomUUID());
            invalidEvent.setStatus(status);
            invalidEvent.setStartDateTime(LocalDateTime.now().plusDays(1));
            
            when(eventRepository.findById(invalidEvent.getId())).thenReturn(Optional.of(invalidEvent));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> eventParticipantService.registerParticipant(invalidEvent.getId(), testUser));

            assertEquals(expectedMessage, exception.getMessage());
            verify(eventParticipantRepository, never()).save(any());
        }

        static Stream<Arguments> invalidEventStatusProvider() {
            return Stream.of(
                Arguments.of(EventStatus.PENDING_APPROVAL, "Não é possível se inscrever em evento pendente de aprovação"),
                Arguments.of(EventStatus.REJECTED, "Não é possível se inscrever em evento rejeitado"),
                Arguments.of(EventStatus.CANCELED_BY_ORGANIZER, "Não é possível se inscrever em evento cancelado pelo organizador"),
                Arguments.of(EventStatus.CANCELED_BY_ADMIN, "Não é possível se inscrever em evento cancelado pelo administrador"),
                Arguments.of(EventStatus.CONCLUDED, "Não é possível se inscrever em evento já concluído"),
                Arguments.of(EventStatus.IN_PROGRESS, "Não é possível se inscrever em evento já em andamento")
            );
        }
    }
} 