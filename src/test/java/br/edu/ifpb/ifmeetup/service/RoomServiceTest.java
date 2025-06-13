package br.edu.ifpb.ifmeetup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.RoomResource;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.domain.projection.RoomProjection;
import br.edu.ifpb.ifmeetup.domain.repository.event.RoomRepository;
import br.edu.ifpb.ifmeetup.domain.repository.auth.UserRepository;
import br.edu.ifpb.ifmeetup.dto.room.RoomRequest;
import br.edu.ifpb.ifmeetup.dto.room.RoomResourceRequest;
import br.edu.ifpb.ifmeetup.dto.room.RoomResponse;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import br.edu.ifpb.ifmeetup.service.impl.RoomServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para RoomService")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room testRoom;
    private User testUser;
    private RoomResource testResource;
    private RoomRequest testRoomRequest;
    private Set<RoomResourceRequest> testResourceRequests;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("João");
        testUser.setLastName("Silva");
        testUser.setEmail("joao.silva@example.com");

        // Configurar recurso de teste
        testResource = new RoomResource();
        testResource.setId(UUID.randomUUID());
        testResource.setResourceType(ResourceType.COMPUTER);
        testResource.setQuantity(10);
        testResource.setDetails("Computadores para laboratório");

        // Configurar sala de teste
        testRoom = new Room();
        testRoom.setId(UUID.randomUUID());
        testRoom.setName("Sala de Reuniões");
        testRoom.setLocation("Bloco A, 2º andar");
        testRoom.setCapacity(20);
        testRoom.setType(RoomType.MEETING_ROOM);
        testRoom.setStatus(RoomStatus.AVAILABLE);
        testRoom.setDescription("Sala para reuniões e apresentações");
        testRoom.setCreatedBy(testUser);
        testRoom.setUpdatedBy(testUser);

        Set<RoomResource> resources = new HashSet<>();
        resources.add(testResource);
        testResource.setRoom(testRoom);
        testRoom.setInventory(resources);

        // Configurar request de recurso de teste
        testResourceRequests = new HashSet<>();
        RoomResourceRequest resourceRequest = new RoomResourceRequest(
                ResourceType.COMPUTER,
                10,
                "Computadores para laboratório"
        );
        testResourceRequests.add(resourceRequest);

        // Configurar request de sala de teste
        testRoomRequest = new RoomRequest(
                "Sala de Reuniões",
                "Bloco A, 2º andar",
                20,
                testResourceRequests,
                RoomType.MEETING_ROOM,
                RoomStatus.AVAILABLE,
                "Sala para reuniões e apresentações"
        );
    }

    @Nested
    @DisplayName("Testes de criação de sala")
    class CreateRoomTests {

        @Test
        @DisplayName("Deve criar sala com sucesso quando dados válidos são fornecidos")
        void shouldCreateRoomSuccessfullyWithValidData() {
            // Arrange
            when(roomRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.empty());
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            // Act
            RoomResponse response = roomService.createRoom(testRoomRequest, testUser);

            // Assert
            assertNotNull(response);
            assertEquals(testRoom.getId(), response.id());
            assertEquals(testRoom.getName(), response.name());
            assertEquals(testRoom.getLocation(), response.location());
            assertEquals(testRoom.getCapacity(), response.capacity());
            assertEquals(testRoom.getType(), response.type());
            assertEquals(testRoom.getStatus(), response.status());
            assertEquals(testRoom.getDescription(), response.description());

            // Verificar que o método save foi chamado
            verify(roomRepository).save(any(Room.class));
            verify(userRepository).findById(testUser.getId());

            // Verificar captura de argumentos
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository).save(roomCaptor.capture());
            Room savedRoom = roomCaptor.getValue();

            assertEquals(testRoomRequest.name(), savedRoom.getName());
            assertEquals(testRoomRequest.location(), savedRoom.getLocation());
            assertEquals(testRoomRequest.capacity(), savedRoom.getCapacity());
            assertEquals(testRoomRequest.type(), savedRoom.getType());
            assertEquals(testRoomRequest.status(), savedRoom.getStatus());
            assertEquals(testRoomRequest.description(), savedRoom.getDescription());
            assertEquals(testUser, savedRoom.getCreatedBy());
            assertFalse(savedRoom.getInventory().isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala com mesmo nome e localização já existir")
        void shouldThrowExceptionWhenRoomWithSameNameAndLocationAlreadyExists() {
            // Arrange
            when(roomRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.of(testRoom));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> roomService.createRoom(testRoomRequest, testUser));

            assertEquals("Já existe uma sala com o mesmo nome e localização", exception.getMessage());
            verify(roomRepository, never()).save(any(Room.class));
        }
    }

    @Nested
    @DisplayName("Testes de atualização de sala")
    class UpdateRoomTests {

        @Test
        @DisplayName("Deve atualizar sala com sucesso quando dados válidos são fornecidos")
        void shouldUpdateRoomSuccessfullyWithValidData() {
            // Arrange
            UUID roomId = testRoom.getId();
            RoomRequest updateRequest = new RoomRequest(
                    "Sala de Reuniões Atualizada",
                    "Bloco B, 1º andar",
                    30,
                    testResourceRequests,
                    RoomType.CLASSROOM,
                    RoomStatus.UNDER_MAINTENANCE,
                    "Sala atualizada para aulas"
            );

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
            when(roomRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.empty());
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            // Act
            RoomResponse response = roomService.updateRoom(roomId, updateRequest, testUser);

            // Assert
            assertNotNull(response);

            // Verificar que o método save foi chamado
            verify(roomRepository).save(any(Room.class));

            // Verificar captura de argumentos
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository).save(roomCaptor.capture());
            Room updatedRoom = roomCaptor.getValue();

            assertEquals(updateRequest.name(), updatedRoom.getName());
            assertEquals(updateRequest.location(), updatedRoom.getLocation());
            assertEquals(updateRequest.capacity(), updatedRoom.getCapacity());
            assertEquals(updateRequest.type(), updatedRoom.getType());
            assertEquals(updateRequest.status(), updatedRoom.getStatus());
            assertEquals(updateRequest.description(), updatedRoom.getDescription());
            assertEquals(testUser, updatedRoom.getUpdatedBy());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não for encontrada para atualização")
        void shouldThrowExceptionWhenRoomToUpdateDoesNotExist() {
            // Arrange
            UUID roomId = UUID.randomUUID();
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> roomService.updateRoom(roomId, testRoomRequest, testUser));

            assertTrue(exception.getMessage().contains("Sala não encontrada"));
            verify(roomRepository, never()).save(any(Room.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando outra sala com mesmo nome e localização já existir")
        void shouldThrowExceptionWhenAnotherRoomWithSameNameAndLocationAlreadyExists() {
            // Arrange
            UUID roomId = testRoom.getId();
            Room anotherRoom = new Room();
            anotherRoom.setId(UUID.randomUUID());
            anotherRoom.setName(testRoomRequest.name());
            anotherRoom.setLocation(testRoomRequest.location());

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
            when(roomRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.of(anotherRoom));

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> roomService.updateRoom(roomId, testRoomRequest, testUser));

            assertEquals("Já existe outra sala com o mesmo nome e localização", exception.getMessage());
            verify(roomRepository, never()).save(any(Room.class));
        }
    }

    @Nested
    @DisplayName("Testes de busca de sala por ID")
    class FindRoomByIdTests {

        @Test
        @DisplayName("Deve encontrar sala por ID com sucesso quando sala existe")
        void shouldFindRoomByIdSuccessfullyWhenRoomExists() {
            // Arrange
            UUID roomId = testRoom.getId();
            RoomProjection roomProjection = mock(RoomProjection.class);
            when(roomProjection.getId()).thenReturn(roomId);
            when(roomProjection.getName()).thenReturn(testRoom.getName());
            when(roomProjection.getLocation()).thenReturn(testRoom.getLocation());
            when(roomProjection.getCapacity()).thenReturn(testRoom.getCapacity());
            when(roomProjection.getType()).thenReturn(testRoom.getType());
            when(roomProjection.getStatus()).thenReturn(testRoom.getStatus());
            when(roomProjection.getDescription()).thenReturn(testRoom.getDescription());

            when(roomRepository.findProjectedById(roomId)).thenReturn(Optional.of(roomProjection));

            // Act
            RoomResponse response = roomService.findRoomById(roomId);

            // Assert
            assertNotNull(response);
            assertEquals(roomId, response.id());
            assertEquals(testRoom.getName(), response.name());
            assertEquals(testRoom.getLocation(), response.location());
            assertEquals(testRoom.getCapacity(), response.capacity());
            assertEquals(testRoom.getType(), response.type());
            assertEquals(testRoom.getStatus(), response.status());
            assertEquals(testRoom.getDescription(), response.description());

            verify(roomRepository).findProjectedById(roomId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não for encontrada por ID")
        void shouldThrowExceptionWhenRoomWithSpecifiedIdDoesNotExist() {
            // Arrange
            UUID roomId = UUID.randomUUID();
            when(roomRepository.findProjectedById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> roomService.findRoomById(roomId));

            assertTrue(exception.getMessage().contains("Sala não encontrada"));
            verify(roomRepository).findProjectedById(roomId);
        }
    }

    @Nested
    @DisplayName("Testes de busca de sala com detalhes completos por ID")
    class FindRoomWithDetailsByIdTests {

        @Test
        @DisplayName("Deve encontrar sala com detalhes completos por ID com sucesso quando sala existir")
        void shouldFindRoomWithDetailsByIdSuccessfullyWhenRoomExists() {
            // Arrange
            when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));

            // Act
            RoomResponse response = roomService.findRoomWithDetailsById(testRoom.getId());

            // Assert
            assertNotNull(response);
            assertEquals(testRoom.getId(), response.id());
            assertEquals(testRoom.getName(), response.name());
            assertEquals(testRoom.getLocation(), response.location());
            assertEquals(testRoom.getCapacity(), response.capacity());
            assertEquals(testRoom.getType(), response.type());
            assertEquals(testRoom.getStatus(), response.status());
            assertEquals(testRoom.getDescription(), response.description());
            assertNotNull(response.inventory());
            assertFalse(response.inventory().isEmpty());
            assertEquals(1, response.inventory().size());

            verify(roomRepository).findById(testRoom.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não for encontrada por ID para detalhes completos")
        void shouldThrowExceptionWhenRoomWithSpecifiedIdDoesNotExistForDetails() {
            // Arrange
            UUID roomId = UUID.randomUUID();
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> roomService.findRoomWithDetailsById(roomId));

            assertTrue(exception.getMessage().contains("Sala não encontrada"));
            verify(roomRepository).findById(roomId);
        }
    }

    @Nested
    @DisplayName("Testes de busca de todas as salas")
    class FindAllRoomsTests {

        @Test
        @DisplayName("Deve encontrar todas as salas com sucesso quando existem salas cadastradas")
        void shouldFindAllRoomsSuccessfullyWhenRoomsExist() {
            // Arrange
            List<RoomProjection> roomProjections = new ArrayList<>();
            RoomProjection roomProjection = mock(RoomProjection.class);
            when(roomProjection.getId()).thenReturn(testRoom.getId());
            when(roomProjection.getName()).thenReturn(testRoom.getName());
            when(roomProjection.getLocation()).thenReturn(testRoom.getLocation());
            when(roomProjection.getCapacity()).thenReturn(testRoom.getCapacity());
            when(roomProjection.getType()).thenReturn(testRoom.getType());
            when(roomProjection.getStatus()).thenReturn(testRoom.getStatus());
            when(roomProjection.getDescription()).thenReturn(testRoom.getDescription());
            roomProjections.add(roomProjection);

            when(roomRepository.findAllProjectedBy()).thenReturn(roomProjections);

            // Act
            List<RoomResponse> responses = roomService.findAllRooms();

            // Assert
            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());

            RoomResponse response = responses.get(0);
            assertEquals(testRoom.getId(), response.id());
            assertEquals(testRoom.getName(), response.name());
            assertEquals(testRoom.getLocation(), response.location());
            assertEquals(testRoom.getCapacity(), response.capacity());
            assertEquals(testRoom.getType(), response.type());
            assertEquals(testRoom.getStatus(), response.status());
            assertEquals(testRoom.getDescription(), response.description());

            verify(roomRepository).findAllProjectedBy();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver salas cadastradas")
        void shouldReturnEmptyListWhenNoRoomsExistInDatabase() {
            // Arrange
            when(roomRepository.findAllProjectedBy()).thenReturn(Collections.emptyList());

            // Act
            List<RoomResponse> responses = roomService.findAllRooms();

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(roomRepository).findAllProjectedBy();
        }
    }

    @Nested
    @DisplayName("Testes de busca de salas por status")
    class FindRoomsByStatusTests {

        @Test
        @DisplayName("Deve encontrar salas por status com sucesso quando existem salas com o status especificado")
        void shouldFindRoomsByStatusSuccessfullyWhenRoomsWithSpecifiedStatusExist() {
            // Arrange
            RoomStatus status = RoomStatus.AVAILABLE;
            List<RoomProjection> roomProjections = new ArrayList<>();
            RoomProjection roomProjection = mock(RoomProjection.class);
            when(roomProjection.getId()).thenReturn(testRoom.getId());
            when(roomProjection.getName()).thenReturn(testRoom.getName());
            when(roomProjection.getLocation()).thenReturn(testRoom.getLocation());
            when(roomProjection.getCapacity()).thenReturn(testRoom.getCapacity());
            when(roomProjection.getType()).thenReturn(testRoom.getType());
            when(roomProjection.getStatus()).thenReturn(status);
            when(roomProjection.getDescription()).thenReturn(testRoom.getDescription());
            roomProjections.add(roomProjection);

            when(roomRepository.findProjectedByStatus(status)).thenReturn(roomProjections);

            // Act
            List<RoomResponse> responses = roomService.findRoomsByStatus(status);

            // Assert
            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());

            RoomResponse response = responses.get(0);
            assertEquals(testRoom.getId(), response.id());
            assertEquals(status, response.status());

            verify(roomRepository).findProjectedByStatus(status);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver salas com o status especificado")
        void shouldReturnEmptyListWhenNoRoomsWithSpecifiedStatusExistInDatabase() {
            // Arrange
            RoomStatus status = RoomStatus.UNDER_MAINTENANCE;
            when(roomRepository.findProjectedByStatus(status)).thenReturn(Collections.emptyList());

            // Act
            List<RoomResponse> responses = roomService.findRoomsByStatus(status);

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(roomRepository).findProjectedByStatus(status);
        }
    }

    @Nested
    @DisplayName("Testes de busca de salas por tipo")
    class FindRoomsByTypeTests {

        @Test
        @DisplayName("Deve encontrar salas por tipo com sucesso quando existem salas com o tipo especificado")
        void shouldFindRoomsByTypeSuccessfullyWhenRoomsWithSpecifiedTypeExist() {
            // Arrange
            RoomType type = RoomType.MEETING_ROOM;
            List<RoomProjection> roomProjections = new ArrayList<>();
            RoomProjection roomProjection = mock(RoomProjection.class);
            when(roomProjection.getId()).thenReturn(testRoom.getId());
            when(roomProjection.getName()).thenReturn(testRoom.getName());
            when(roomProjection.getLocation()).thenReturn(testRoom.getLocation());
            when(roomProjection.getCapacity()).thenReturn(testRoom.getCapacity());
            when(roomProjection.getType()).thenReturn(type);
            when(roomProjection.getStatus()).thenReturn(testRoom.getStatus());
            when(roomProjection.getDescription()).thenReturn(testRoom.getDescription());
            roomProjections.add(roomProjection);

            when(roomRepository.findProjectedByType(type)).thenReturn(roomProjections);

            // Act
            List<RoomResponse> responses = roomService.findRoomsByType(type);

            // Assert
            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());

            RoomResponse response = responses.get(0);
            assertEquals(testRoom.getId(), response.id());
            assertEquals(type, response.type());

            verify(roomRepository).findProjectedByType(type);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver salas com o tipo especificado")
        void shouldReturnEmptyListWhenNoRoomsWithSpecifiedTypeExistInDatabase() {
            // Arrange
            RoomType type = RoomType.LABORATORY;
            when(roomRepository.findProjectedByType(type)).thenReturn(Collections.emptyList());

            // Act
            List<RoomResponse> responses = roomService.findRoomsByType(type);

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(roomRepository).findProjectedByType(type);
        }
    }

    @Nested
    @DisplayName("Testes de busca de salas por capacidade mínima")
    class FindRoomsByMinCapacityTests {

        @Test
        @DisplayName("Deve encontrar salas por capacidade mínima com sucesso quando existem salas com capacidade suficiente")
        void shouldFindRoomsByMinCapacitySuccessfullyWhenRoomsWithSufficientCapacityExist() {
            // Arrange
            Integer minCapacity = 15;
            List<RoomProjection> roomProjections = new ArrayList<>();
            RoomProjection roomProjection = mock(RoomProjection.class);
            when(roomProjection.getId()).thenReturn(testRoom.getId());
            when(roomProjection.getName()).thenReturn(testRoom.getName());
            when(roomProjection.getLocation()).thenReturn(testRoom.getLocation());
            when(roomProjection.getCapacity()).thenReturn(20);
            when(roomProjection.getType()).thenReturn(testRoom.getType());
            when(roomProjection.getStatus()).thenReturn(testRoom.getStatus());
            when(roomProjection.getDescription()).thenReturn(testRoom.getDescription());
            roomProjections.add(roomProjection);

            when(roomRepository.findProjectedByCapacityGreaterThanEqual(minCapacity)).thenReturn(roomProjections);

            // Act
            List<RoomResponse> responses = roomService.findRoomsByMinCapacity(minCapacity);

            // Assert
            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());

            RoomResponse response = responses.get(0);
            assertEquals(testRoom.getId(), response.id());
            assertTrue(response.capacity() >= minCapacity);

            verify(roomRepository).findProjectedByCapacityGreaterThanEqual(minCapacity);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver salas com a capacidade mínima especificada")
        void shouldReturnEmptyListWhenNoRoomsWithSpecifiedMinCapacityExistInDatabase() {
            // Arrange
            Integer minCapacity = 50;
            when(roomRepository.findProjectedByCapacityGreaterThanEqual(minCapacity)).thenReturn(Collections.emptyList());

            // Act
            List<RoomResponse> responses = roomService.findRoomsByMinCapacity(minCapacity);

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(roomRepository).findProjectedByCapacityGreaterThanEqual(minCapacity);
        }
    }

    @Nested
    @DisplayName("Testes de busca de salas disponíveis")
    class FindAvailableRoomsTests {

        @Test
        @DisplayName("Deve encontrar salas disponíveis com sucesso quando existem salas disponíveis no período especificado")
        void shouldFindAvailableRoomsSuccessfullyWhenRoomsAreAvailableInSpecifiedPeriod() {
            // Arrange
            LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endDateTime = startDateTime.plusHours(2);
            RoomStatus status = RoomStatus.AVAILABLE;

            List<Room> availableRooms = new ArrayList<>();
            availableRooms.add(testRoom);

            when(roomRepository.findAvailableRooms(startDateTime, endDateTime, status)).thenReturn(availableRooms);

            // Act
            List<RoomResponse> responses = roomService.findAvailableRooms(startDateTime, endDateTime, status);

            // Assert
            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());

            RoomResponse response = responses.get(0);
            assertEquals(testRoom.getId(), response.id());

            verify(roomRepository).findAvailableRooms(startDateTime, endDateTime, status);
        }

        @Test
        @DisplayName("Deve lançar exceção quando data de início for posterior à data de término")
        void shouldThrowExceptionWhenStartDateTimeIsAfterEndDateTime() {
            // Arrange
            LocalDateTime startDateTime = LocalDateTime.now().plusDays(2);
            LocalDateTime endDateTime = LocalDateTime.now().plusDays(1);
            RoomStatus status = RoomStatus.AVAILABLE;

            // Act & Assert
            BusinessValidationException exception = assertThrows(BusinessValidationException.class,
                    () -> roomService.findAvailableRooms(startDateTime, endDateTime, status));

            assertEquals("A data de início deve ser anterior à data de término", exception.getMessage());
            verify(roomRepository, never()).findAvailableRooms(any(), any(), any());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver salas disponíveis no período especificado")
        void shouldReturnEmptyListWhenNoRoomsAreAvailableInSpecifiedPeriod() {
            // Arrange
            LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endDateTime = startDateTime.plusHours(2);
            RoomStatus status = RoomStatus.AVAILABLE;

            when(roomRepository.findAvailableRooms(startDateTime, endDateTime, status)).thenReturn(Collections.emptyList());

            // Act
            List<RoomResponse> responses = roomService.findAvailableRooms(startDateTime, endDateTime, status);

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(roomRepository).findAvailableRooms(startDateTime, endDateTime, status);
        }
    }

    @Nested
    @DisplayName("Testes de busca de salas por recurso")
    class FindRoomsByResourceTests {

        @Test
        @DisplayName("Deve encontrar salas por recurso com sucesso quando existem salas com o recurso especificado")
        void shouldFindRoomsByResourceSuccessfullyWhenRoomsWithSpecifiedResourceExist() {
            // Arrange
            ResourceType resourceType = ResourceType.COMPUTER;
            Integer minQuantity = 5;

            List<Room> roomsWithResource = new ArrayList<>();
            roomsWithResource.add(testRoom);

            when(roomRepository.findByResourceAndQuantity(resourceType, minQuantity)).thenReturn(roomsWithResource);

            // Act
            List<RoomResponse> responses = roomService.findRoomsByResource(resourceType, minQuantity);

            // Assert
            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());

            RoomResponse response = responses.get(0);
            assertEquals(testRoom.getId(), response.id());

            verify(roomRepository).findByResourceAndQuantity(resourceType, minQuantity);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver salas com o recurso especificado")
        void shouldReturnEmptyListWhenNoRoomsWithSpecifiedResourceExistInDatabase() {
            // Arrange
            ResourceType resourceType = ResourceType.PROJECTOR;
            Integer minQuantity = 1;

            when(roomRepository.findByResourceAndQuantity(resourceType, minQuantity)).thenReturn(Collections.emptyList());

            // Act
            List<RoomResponse> responses = roomService.findRoomsByResource(resourceType, minQuantity);

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
            verify(roomRepository).findByResourceAndQuantity(resourceType, minQuantity);
        }
    }

    @Nested
    @DisplayName("Testes de alteração de status de sala")
    class ChangeRoomStatusTests {

        @Test
        @DisplayName("Deve alterar status de sala com sucesso quando sala existe")
        void shouldChangeRoomStatusSuccessfullyWhenRoomExists() {
            // Arrange
            UUID roomId = testRoom.getId();
            RoomStatus newStatus = RoomStatus.UNDER_MAINTENANCE;

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            // Act
            RoomResponse response = roomService.changeRoomStatus(roomId, newStatus, testUser);

            // Assert
            assertNotNull(response);

            // Verificar que o método save foi chamado
            verify(roomRepository).save(any(Room.class));

            // Verificar captura de argumentos
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository).save(roomCaptor.capture());
            Room updatedRoom = roomCaptor.getValue();

            assertEquals(newStatus, updatedRoom.getStatus());
            assertEquals(testUser, updatedRoom.getUpdatedBy());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não for encontrada para alteração de status")
        void shouldThrowExceptionWhenRoomToChangeStatusDoesNotExist() {
            // Arrange
            UUID roomId = UUID.randomUUID();
            RoomStatus newStatus = RoomStatus.UNDER_MAINTENANCE;

            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> roomService.changeRoomStatus(roomId, newStatus, testUser));

            assertTrue(exception.getMessage().contains("Sala não encontrada"));
            verify(roomRepository, never()).save(any(Room.class));
        }
    }

    @Nested
    @DisplayName("Testes de exclusão de sala")
    class DeleteRoomTests {

        @Test
        @DisplayName("Deve excluir sala com sucesso quando sala existe")
        void shouldDeleteRoomSuccessfullyWhenRoomExists() {
            // Arrange
            UUID roomId = testRoom.getId();
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
            doNothing().when(roomRepository).delete(testRoom);

            // Act
            roomService.deleteRoom(roomId);

            // Assert
            verify(roomRepository).delete(testRoom);
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não for encontrada para exclusão")
        void shouldThrowExceptionWhenRoomToDeleteDoesNotExist() {
            // Arrange
            UUID roomId = UUID.randomUUID();
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> roomService.deleteRoom(roomId));

            assertTrue(exception.getMessage().contains("Sala não encontrada"));
            verify(roomRepository, never()).delete(any(Room.class));
        }
    }
}
