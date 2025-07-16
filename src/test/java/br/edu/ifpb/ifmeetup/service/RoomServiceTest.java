package br.edu.ifpb.ifmeetup.service;

import static br.edu.ifpb.ifmeetup.domain.enums.ResourceType.COMPUTER;
import static br.edu.ifpb.ifmeetup.domain.enums.ResourceType.PROJECTOR;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.domain.projection.RoomProjection;
import br.edu.ifpb.ifmeetup.domain.projection.RoomWithResourcesProjection;
import br.edu.ifpb.ifmeetup.domain.repository.event.RoomRepository;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomRequest;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomResourceRequest;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomStatusRequest;
import br.edu.ifpb.ifmeetup.dto.room.response.RoomResponse;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import br.edu.ifpb.ifmeetup.service.auth.AuthService;
import br.edu.ifpb.ifmeetup.service.room.RoomService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para RoomService")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;
    private User testUser;
    private RoomRequest validRoomRequest;
    
    @Mock
    private RoomProjection mockRoomProjection;
    
    @Mock
    private RoomWithResourcesProjection mockRoomWithResourcesProjection;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("João");
        testUser.setLastName("Silva");
        testUser.setEmail("joao.silva@ifpb.edu.br");

        // Configurar sala de teste
        testRoom = new Room();
        testRoom.setId(UUID.randomUUID());
        testRoom.setName("Laboratório de Informática 01");
        testRoom.setLocation("Bloco A, 2º andar");
        testRoom.setCapacity(40);
        testRoom.setType(RoomType.LABORATORY);
        testRoom.setStatus(RoomStatus.AVAILABLE);
        testRoom.setDescription("Laboratório com 40 computadores");
        testRoom.setCreatedBy(testUser);
        testRoom.setCreatedAt(LocalDateTime.now());
        testRoom.setUpdatedAt(LocalDateTime.now());

        // Configurar request válido
        Set<RoomResourceRequest> resources = Set.of(
            new RoomResourceRequest(COMPUTER, 40, "Computadores Dell"),
            new RoomResourceRequest(PROJECTOR, 1, "Projetor Epson")
        );
        
        validRoomRequest = new RoomRequest(
            "Laboratório de Informática 01",
            "Bloco A, 2º andar",
            40,
            RoomType.LABORATORY,
            RoomStatus.AVAILABLE,
            "Laboratório com 40 computadores",
            resources
                );
    }

    // helper method para configurar mocks da RoomProjection
    private void setupRoomProjectionMock() {
        when(mockRoomProjection.getId()).thenReturn(testRoom.getId());
        when(mockRoomProjection.getName()).thenReturn(testRoom.getName());
        when(mockRoomProjection.getLocation()).thenReturn(testRoom.getLocation());
        when(mockRoomProjection.getCapacity()).thenReturn(testRoom.getCapacity());
        when(mockRoomProjection.getType()).thenReturn(testRoom.getType());
        when(mockRoomProjection.getStatus()).thenReturn(testRoom.getStatus());
        when(mockRoomProjection.getDescription()).thenReturn(testRoom.getDescription());
    }

    // helper method para configurar mocks da RoomWithResourcesProjection
    private void setupRoomWithResourcesProjectionMock() {
        when(mockRoomWithResourcesProjection.getId()).thenReturn(testRoom.getId());
        when(mockRoomWithResourcesProjection.getName()).thenReturn(testRoom.getName());
        when(mockRoomWithResourcesProjection.getLocation()).thenReturn(testRoom.getLocation());
        when(mockRoomWithResourcesProjection.getCapacity()).thenReturn(testRoom.getCapacity());
        when(mockRoomWithResourcesProjection.getType()).thenReturn(testRoom.getType());
        when(mockRoomWithResourcesProjection.getStatus()).thenReturn(testRoom.getStatus());
        when(mockRoomWithResourcesProjection.getDescription()).thenReturn(testRoom.getDescription());
        when(mockRoomWithResourcesProjection.getInventory()).thenReturn(Set.of());
    }

    @Nested
    @DisplayName("Testes de criação de sala")
    class CreateRoomTests {

        @Test
        @DisplayName("Deve criar sala com sucesso")
        void shouldCreateRoomSuccessfully() {
            // Arrange
            when(authService.getCurrentUser()).thenReturn(testUser);
            when(roomRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.empty());
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            // Act
            RoomResponse response = roomService.createRoom(validRoomRequest);

            // Assert
            assertNotNull(response);
            assertEquals(testRoom.getId(), response.id());
            assertEquals(testRoom.getName(), response.name());
            assertEquals(testRoom.getCapacity(), response.capacity());
            
            verify(roomRepository).save(any(Room.class));
            verify(authService).getCurrentUser();
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala já existe")
        void shouldThrowExceptionWhenRoomAlreadyExists() {
            // Arrange
            when(roomRepository.findByNameAndLocation(anyString(), anyString())).thenReturn(Optional.of(testRoom));

            // Act & Assert
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> roomService.createRoom(validRoomRequest)
            );

            assertTrue(exception.getMessage().contains("Já existe uma sala"));
            verify(roomRepository, never()).save(any(Room.class));
        }
    }

    @Nested
    @DisplayName("Testes de busca de salas")
    class FindRoomTests {

        @Test
        @DisplayName("Deve buscar sala por ID com sucesso")
        void shouldFindRoomByIdSuccessfully() {
            // Arrange
            UUID roomId = testRoom.getId();
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));

            // Act
            RoomResponse response = roomService.findById(roomId);

            // Assert
            assertNotNull(response);
            assertEquals(roomId, response.id());
            assertEquals(testRoom.getName(), response.name());
            
            verify(roomRepository).findById(roomId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando sala não encontrada")
        void shouldThrowExceptionWhenRoomNotFound() {
            // Arrange
            UUID roomId = UUID.randomUUID();
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> roomService.findById(roomId)
            );

            assertTrue(exception.getMessage().contains("Sala não encontrada"));
        }

        @Test
        @DisplayName("Deve listar todas as salas com paginação")
        void shouldListAllRoomsWithPagination() {
            // Arrange
            List<Room> rooms = List.of(testRoom);
            Page<Room> roomPage = new PageImpl<>(rooms);
            Pageable pageable = PageRequest.of(0, 20);
            
            when(roomRepository.findAll(pageable)).thenReturn(roomPage);

            // Act
            Page<RoomResponse> response = roomService.findAll(pageable);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            assertEquals(testRoom.getId(), response.getContent().get(0).id());
            
            verify(roomRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Deve listar todas as salas usando projection otimizada")
        void shouldListAllRoomsOptimized() {
            // Arrange
            setupRoomProjectionMock();
            when(roomRepository.findAllProjectedBy()).thenReturn(List.of(mockRoomProjection));

            // Act
            List<RoomResponse> response = roomService.findAllOptimized();

            // Assert
            assertNotNull(response);
            assertFalse(response.isEmpty());
            assertEquals(1, response.size());
            assertEquals(testRoom.getId(), response.get(0).id());
            assertEquals(testRoom.getName(), response.get(0).name());
            
            verify(roomRepository).findAllProjectedBy();
        }

        @Test
        @DisplayName("Deve buscar salas por status")
        void shouldFindRoomsByStatus() {
            // Arrange
            setupRoomProjectionMock();
            when(roomRepository.findProjectedByStatus(RoomStatus.AVAILABLE)).thenReturn(List.of(mockRoomProjection));

            // Act
            List<RoomResponse> response = roomService.findByStatus(RoomStatus.AVAILABLE);

            // Assert
            assertNotNull(response);
            assertFalse(response.isEmpty());
            assertEquals(1, response.size());
            assertEquals(RoomStatus.AVAILABLE, response.get(0).status());
            
            verify(roomRepository).findProjectedByStatus(RoomStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Deve buscar salas disponíveis em período")
        void shouldFindAvailableRoomsByPeriod() {
            // Arrange
            setupRoomProjectionMock();
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);
            
            when(roomRepository.findProjectedAvailableRooms(start, end, RoomStatus.AVAILABLE))
                .thenReturn(List.of(mockRoomProjection));

            // Act
            List<RoomResponse> response = roomService.findAvailableRooms(start, end);

            // Assert
            assertNotNull(response);
            assertFalse(response.isEmpty());
            assertEquals(1, response.size());
            
            verify(roomRepository).findProjectedAvailableRooms(start, end, RoomStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Deve buscar salas por tipo")
        void shouldFindRoomsByType() {
            // Arrange
            setupRoomProjectionMock();
            when(roomRepository.findProjectedByType(RoomType.LABORATORY)).thenReturn(List.of(mockRoomProjection));

            // Act
            List<RoomResponse> response = roomService.findByType(RoomType.LABORATORY);

            // Assert
            assertNotNull(response);
            assertFalse(response.isEmpty());
            assertEquals(1, response.size());
            assertEquals(RoomType.LABORATORY, response.get(0).type());
            
            verify(roomRepository).findProjectedByType(RoomType.LABORATORY);
        }

        @Test
        @DisplayName("Deve buscar salas por capacidade mínima")
        void shouldFindRoomsByMinCapacity() {
            // Arrange
            setupRoomProjectionMock();
            Integer minCapacity = 30;
            when(roomRepository.findProjectedByCapacityGreaterThanEqual(minCapacity)).thenReturn(List.of(mockRoomProjection));

            // Act
            List<RoomResponse> response = roomService.findByMinCapacity(minCapacity);

            // Assert
            assertNotNull(response);
            assertFalse(response.isEmpty());
            assertEquals(1, response.size());
            assertEquals(testRoom.getCapacity(), response.get(0).capacity());
            
            verify(roomRepository).findProjectedByCapacityGreaterThanEqual(minCapacity);
        }

        @Test
        @DisplayName("Deve buscar salas por recurso e quantidade mínima")
        void shouldFindRoomsByResource() {
            // Arrange
            setupRoomWithResourcesProjectionMock();
            ResourceType resourceType = PROJECTOR;
            Integer minQuantity = 1;
            when(roomRepository.findProjectedByResourceAndQuantity(resourceType, minQuantity))
                .thenReturn(List.of(mockRoomWithResourcesProjection));

            // Act
            List<RoomResponse> response = roomService.findByResource(resourceType, minQuantity);

            // Assert
            assertNotNull(response);
            assertFalse(response.isEmpty());
            assertEquals(1, response.size());
            assertEquals(testRoom.getId(), response.get(0).id());
            
            verify(roomRepository).findProjectedByResourceAndQuantity(resourceType, minQuantity);
        }

        @Test
        @DisplayName("Deve validar período ao buscar salas disponíveis")
        void shouldValidatePeriodWhenFindingAvailableRooms() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.minusHours(1); // End antes de start

            // Act & Assert
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> roomService.findAvailableRooms(start, end)
            );

            assertTrue(exception.getMessage().contains("Data/hora inicial deve ser anterior"));
        }
    }

    @Nested
    @DisplayName("Testes de atualização de sala")
    class UpdateRoomTests {

        @Test
        @DisplayName("Deve atualizar sala com sucesso")
        void shouldUpdateRoomSuccessfully() {
            // Arrange
            UUID roomId = testRoom.getId();
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
            when(authService.getCurrentUser()).thenReturn(testUser);
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            RoomRequest updateRequest = new RoomRequest(
                "Laboratório de Informática 02",
                "Bloco B, 1º andar",
                30,
                RoomType.LABORATORY,
                RoomStatus.AVAILABLE,
                "Laboratório atualizado",
                null
            );

            // Act
            RoomResponse response = roomService.updateRoom(roomId, updateRequest);

            // Assert
            assertNotNull(response);
            verify(roomRepository).save(any(Room.class));
            verify(authService).getCurrentUser();
        }

        @Test
        @DisplayName("Deve alterar status da sala com sucesso")
        void shouldUpdateRoomStatusSuccessfully() {
            // Arrange
            UUID roomId = testRoom.getId();
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
            when(authService.getCurrentUser()).thenReturn(testUser);
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            RoomStatusRequest statusRequest = new RoomStatusRequest(
                RoomStatus.UNDER_MAINTENANCE,
                "Manutenção preventiva"
            );

            // Act
            RoomResponse response = roomService.updateRoomStatus(roomId, statusRequest);

            // Assert
            assertNotNull(response);
            verify(roomRepository).save(any(Room.class));
        }

        @Test
        @DisplayName("Deve validar mudança de status inválida")
        void shouldValidateInvalidStatusChange() {
            // Arrange
            UUID roomId = testRoom.getId();
            testRoom.setStatus(RoomStatus.DISABLED);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));

            RoomStatusRequest statusRequest = new RoomStatusRequest(
                RoomStatus.AVAILABLE,
                "Tentando reativar diretamente"
            );

            // Act & Assert
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> roomService.updateRoomStatus(roomId, statusRequest)
            );

            assertTrue(exception.getMessage().contains("Sala desabilitada só pode ser alterada para manutenção"));
        }
    }

    @Nested
    @DisplayName("Testes de exclusão de sala")
    class DeleteRoomTests {

        @Test
        @DisplayName("Deve desabilitar sala com sucesso")
        void shouldDisableRoomSuccessfully() {
            // Arrange
            UUID roomId = testRoom.getId();
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
            when(authService.getCurrentUser()).thenReturn(testUser);
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            // Act
            assertDoesNotThrow(() -> roomService.deleteRoom(roomId));

            // Assert
            verify(roomRepository).save(argThat(room -> 
                room.getStatus() == RoomStatus.DISABLED
            ));
        }
    }
}