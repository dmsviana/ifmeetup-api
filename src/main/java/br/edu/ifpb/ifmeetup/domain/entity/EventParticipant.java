package br.edu.ifpb.ifmeetup.domain.entity;

import br.edu.ifpb.ifmeetup.domain.entity.base.BaseEntity;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate; // Para registrationDateTime
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"})) // Garante que um usuário só se inscreva uma vez no mesmo evento
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) 
public class EventParticipant extends BaseEntity {

    

    @NotNull(message = "O evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull(message = "O usuário participante é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "registration_date_time", nullable = false, updatable = false)
    private LocalDateTime registrationDateTime;

    @NotNull(message = "O status de participação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 30)
    private AttendanceStatus attendanceStatus;

    @Column(name = "certificate_issued", nullable = false)
    private boolean certificateIssued = false;

    @Lob
    @Column(name = "feedback")
    private String feedback; 

    
}