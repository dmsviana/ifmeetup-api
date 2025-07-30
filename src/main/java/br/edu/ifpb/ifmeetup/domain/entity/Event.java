package br.edu.ifpb.ifmeetup.domain.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import br.edu.ifpb.ifmeetup.domain.entity.base.BaseEntity;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) 
public class Event extends BaseEntity {

    

    @NotBlank(message = "O título do evento é obrigatório")
    @Size(min = 5, max = 150, message = "O título do evento deve ter entre 5 e 150 caracteres")
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank(message = "A descrição do evento é obrigatória")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "O organizador do evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_user_id", nullable = false, updatable = false) 
    private User organizer;

    @NotNull(message = "A sala para o evento é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "A data e hora de início são obrigatórias")
    @Future(message = "A data de início do evento deve ser no futuro")
    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @NotNull(message = "A data e hora de término são obrigatórias")
    @Future(message = "A data de término do evento deve ser no futuro")
    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @NotNull(message = "O número máximo de participantes é obrigatório")
    @Min(value = 1, message = "O evento deve permitir no mínimo 1 participante")
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @NotNull(message = "O status do evento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status;

    @NotNull(message = "O tipo do evento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Column(name = "public_event", nullable = false)
    private boolean publicEvent = true;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id") 
    private User approvedBy;

    @Column(name = "approval_date_time")
    private LocalDateTime approvalDateTime;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EventParticipant> participants = new HashSet<>();

    @AssertTrue(message = "A data de término deve ser posterior à data de início")
    public boolean isEndDateTimeAfterStartDateTime() {
        if (startDateTime == null || endDateTime == null) {
            return true;
        }
        return endDateTime.isAfter(startDateTime);
    }
    
    /**
     * Verifica se houve alteração na sala ou nos horários do evento.
     * 
     * <p>Este método é útil para determinar se é necessário revalidar
     * conflitos de agendamento ao atualizar um evento.
     * 
     * @param newRoomId ID da nova sala
     * @param newStartDateTime novo horário de início
     * @param newEndDateTime novo horário de término
     * @return {@code true} se houve mudança na sala ou horários, {@code false} caso contrário
     */
    public boolean hasRoomOrTimeChanged(UUID newRoomId, LocalDateTime newStartDateTime, LocalDateTime newEndDateTime) {
    	
    	return !this.room.getId().equals(newRoomId) ||
    		   !this.startDateTime.equals(newStartDateTime) ||
    		   !this.endDateTime.equals(newEndDateTime);
    	
    }
    
    /**
     * Verifica se o evento permite atualizações.
     * 
     * @return {@code true} se o evento pode ser atualizado, {@code false} caso contrário
     */
    public boolean isUpdatable() {
    	return status.isUpdatable();
    }
    
    /**
     * Aprova o evento.
     * 
     * @param approver usuário que está aprovando o evento
     * @param approvalTime momento da aprovação
     * @throws IllegalStateException se o evento não estiver pendente de aprovação
     */
    public void approve(User approver, LocalDateTime approvalTime) {
    	
        if (this.status != EventStatus.PENDING_APPROVAL) {
        	
            throw new IllegalStateException(
                "Apenas eventos com status PENDING_APPROVAL podem ser aprovados"
            );
            
        }
        this.status = EventStatus.APPROVED;
        this.approvedBy = approver;
        this.approvalDateTime = approvalTime;
    }

    /**
     * Rejeita o evento.
     * 
     * @param reason motivo da rejeição
     * @throws IllegalArgumentException se o motivo estiver vazio
     */
    public void reject(String reason) {
    	
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da rejeição é obrigatório");
        }
        
        this.status = EventStatus.REJECTED;
        this.rejectionReason = reason;
        this.approvedBy = null;
        this.approvalDateTime = null;
    }

    /**
     * Cancela o evento pelo organizador.
     */
    public void cancelByOrganizer() {
    	
        if (!this.isUpdatable()) {
            throw new IllegalStateException("Este evento não pode ser cancelado");
        }
        
        this.status = EventStatus.CANCELED_BY_ORGANIZER;
    }

    /**
     * Cancela o evento pelo administrador.
     * 
     * @param admin usuário administrador que está cancelando
     */
    public void cancelByAdmin(User admin) {
        this.status = EventStatus.CANCELED_BY_ADMIN;
    }

    /**
     * Altera o status do evento diretamente.
     * Use este método apenas para transições simples sem lógica adicional.
     * 
     * @param newStatus novo status
     */
    public void changeStatus(EventStatus newStatus) {
        this.status = newStatus;
    }
    
}