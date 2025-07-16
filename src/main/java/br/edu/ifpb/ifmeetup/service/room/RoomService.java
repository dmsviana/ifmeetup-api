package br.edu.ifpb.ifmeetup.service.room;

import br.edu.ifpb.ifmeetup.domain.entity.Room;
import br.edu.ifpb.ifmeetup.domain.entity.RoomResource;
import br.edu.ifpb.ifmeetup.domain.entity.User;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.domain.repository.event.RoomRepository;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomRequest;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomResourceRequest;
import br.edu.ifpb.ifmeetup.dto.room.request.RoomStatusRequest;
import br.edu.ifpb.ifmeetup.dto.room.response.RoomResponse;
import br.edu.ifpb.ifmeetup.exception.BusinessValidationException;
import br.edu.ifpb.ifmeetup.exception.ValidationException;
import br.edu.ifpb.ifmeetup.exception.ResourceNotFoundException;
import br.edu.ifpb.ifmeetup.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final AuthService authService;

    /**
     * Criar uma nova sala
     */
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        log.debug("Criando nova sala: {}", request.name());

        validateRoomUniqueness(request.name(), request.location());

        User currentUser = authService.getCurrentUser();

        Room room = new Room();

        room.setName(request.name());
        room.setLocation(request.location());
        room.setCapacity(request.capacity());
        room.setType(request.type());
        room.setStatus(request.status());
        room.setDescription(request.description());
        room.setCreatedBy(currentUser);

        if (request.resources() != null && !request.resources().isEmpty()) {

            for (RoomResourceRequest resourceRequest : request.resources()) {

                RoomResource resource = new RoomResource();
                resource.setResourceType(resourceRequest.resourceType());
                resource.setQuantity(resourceRequest.quantity());
                resource.setDetails(resourceRequest.details());

                room.addResource(resource);
            }
        }

        Room savedRoom = roomRepository.save(room);
        log.info("Sala criada com sucesso: {} (ID: {})", savedRoom.getName(), savedRoom.getId());

        return RoomResponse.fromEntity(savedRoom);
    }

    /**
     * Buscar sala por ID
     */
    public RoomResponse findById(UUID id) {
        log.debug("Buscando sala por ID: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com ID: " + id));

        return RoomResponse.fromEntity(room);
    }

    /**
     * Listar todas as salas com paginação
     */
    public Page<RoomResponse> findAll(Pageable pageable) {
        log.debug("Listando todas as salas - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return roomRepository.findAll(pageable)
                .map(RoomResponse::fromEntityBasic);
    }

    /**
     * Listar todas as salas usando projection (otimizado)
     */
    public List<RoomResponse> findAllOptimized() {
        log.debug("Listando todas as salas usando projection otimizada");

        return roomRepository.findAllProjectedBy().stream()
                .map(RoomResponse::fromProjection)
                .collect(Collectors.toList());
    }

    /**
     * Buscar salas por status
     */
    public List<RoomResponse> findByStatus(RoomStatus status) {
        log.debug("Buscando salas por status: {}", status);

        return roomRepository.findProjectedByStatus(status).stream()
                .map(RoomResponse::fromProjection)
                .collect(Collectors.toList());
    }

    /**
     * Buscar salas por tipo
     */
    public List<RoomResponse> findByType(RoomType type) {
        log.debug("Buscando salas por tipo: {}", type);

        return roomRepository.findProjectedByType(type).stream()
                .map(RoomResponse::fromProjection)
                .collect(Collectors.toList());
    }

    /**
     * Buscar salas por capacidade mínima
     */
    public List<RoomResponse> findByMinCapacity(Integer minCapacity) {
        log.debug("Buscando salas com capacidade mínima: {}", minCapacity);

        return roomRepository.findProjectedByCapacityGreaterThanEqual(minCapacity).stream()
                .map(RoomResponse::fromProjection)
                .collect(Collectors.toList());
    }

    /**
     * Buscar salas disponíveis em um período específico
     */
    public List<RoomResponse> findAvailableRooms(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        log.debug("Buscando salas disponíveis entre {} e {}", startDateTime, endDateTime);

        if (startDateTime.isAfter(endDateTime)) {
            throw ValidationException.forField("startDateTime", startDateTime, 
                "Data/hora inicial deve ser anterior à data/hora final")
                .addDetail("endDateTime", endDateTime);
        }

        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw ValidationException.forField("startDateTime", startDateTime, 
                "Data/hora inicial deve ser futura");
        }

        return roomRepository.findProjectedAvailableRooms(startDateTime, endDateTime, RoomStatus.AVAILABLE).stream()
                .map(RoomResponse::fromProjection)
                .collect(Collectors.toList());
    }

    /**
     * Buscar salas por recurso e quantidade mínima
     */
    public List<RoomResponse> findByResource(ResourceType resourceType, Integer minQuantity) {
        log.debug("Buscando salas com recurso {} (quantidade mínima: {})", resourceType, minQuantity);

        return roomRepository.findProjectedByResourceAndQuantity(resourceType, minQuantity).stream()
                .map(RoomResponse::fromProjectionWithResources)
                .collect(Collectors.toList());
    }

    /**
     * Atualizar dados de uma sala
     */
    @Transactional
    public RoomResponse updateRoom(UUID id, RoomRequest request) {

        log.debug("Atualizando sala ID: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com ID: " + id));

        if (!room.getName().equals(request.name()) ||
                (room.getLocation() != null && !room.getLocation().equals(request.location()))) {
            validateRoomUniqueness(request.name(), request.location());
        }

        User currentUser = authService.getCurrentUser();

        room.setName(request.name());
        room.setLocation(request.location());
        room.setCapacity(request.capacity());
        room.setType(request.type());
        room.setStatus(request.status());
        room.setDescription(request.description());
        room.setUpdatedBy(currentUser);

        updateRoomResources(room, request.resources());

        Room updatedRoom = roomRepository.save(room);
        log.info("Sala atualizada com sucesso: {} (ID: {})", updatedRoom.getName(), updatedRoom.getId());

        return RoomResponse.fromEntity(updatedRoom);
    }

    /**
     * Alterar status de uma sala
     */
    @Transactional
    public RoomResponse updateRoomStatus(UUID id, RoomStatusRequest request) {

        log.debug("Alterando status da sala ID: {} para {}", id, request.status());

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com ID: " + id));

        validateStatusChange(room.getStatus(), request.status());

        User currentUser = authService.getCurrentUser();

        room.setStatus(request.status());
        room.setUpdatedBy(currentUser);

        Room updatedRoom = roomRepository.save(room);
        log.info("Status da sala {} alterado de {} para {}",
                updatedRoom.getName(), room.getStatus(), request.status());

        return RoomResponse.fromEntity(updatedRoom);
    }

    /**
     * Excluir sala (soft delete - apenas muda status para DISABLED)
     */
    @Transactional
    public void deleteRoom(UUID id) {

        log.debug("Desabilitando sala ID: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com ID: " + id));

        if (hasUpcomingEvents(room)) {
            throw new BusinessValidationException(
                    "Não é possível desabilitar sala com eventos futuros agendados");
        }

        User currentUser = authService.getCurrentUser();

        room.setStatus(RoomStatus.DISABLED);
        room.setUpdatedBy(currentUser);

        roomRepository.save(room);
        log.info("Sala desabilitada com sucesso: {} (ID: {})", room.getName(), room.getId());
    }


    private void validateRoomUniqueness(String name, String location) {
        Optional<Room> existing = roomRepository.findByNameAndLocation(name, location);
        if (existing.isPresent()) {
            throw new BusinessValidationException(
                    String.format("Já existe uma sala com o nome '%s' na localização '%s'",
                            name, location != null ? location : "não especificada"));
        }
    }

    private void validateStatusChange(RoomStatus currentStatus, RoomStatus newStatus) {

        if (currentStatus == RoomStatus.DISABLED && newStatus != RoomStatus.UNDER_MAINTENANCE) {
            throw new BusinessValidationException(
                    "Sala desabilitada só pode ser alterada para manutenção antes de voltar a ficar disponível");
        }

        if (currentStatus == newStatus) {
            throw new BusinessValidationException(
                    "O novo status deve ser diferente do status atual");
        }
    }

    private void updateRoomResources(Room room, Set<RoomResourceRequest> newResources) {
        
        room.getInventory().clear();

        // corrige problema de violação de constraint ao atualizar sala com os mesmos recursos
        // funciona para limpar do banco, pois se remover esse método e ao tentar salvar novamente no final, vai violar unicidade novamente
        // nao sei se é boa prática
        roomRepository.saveAndFlush(room);

        if (newResources != null && !newResources.isEmpty()) {

            for (RoomResourceRequest resourceRequest : newResources) {

                RoomResource resource = new RoomResource();

                resource.setResourceType(resourceRequest.resourceType());
                resource.setQuantity(resourceRequest.quantity());
                resource.setDetails(resourceRequest.details());

                room.addResource(resource);
            }
        }
    }

    private boolean hasUpcomingEvents(Room room) {
        // TODO: Implementar verificação quando o EventRepository estiver disponível
        // Por enquanto, retorna false para permitir testes
        return false;
    }
}