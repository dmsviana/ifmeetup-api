package br.edu.ifpb.ifmeetup.domain.entity;

import br.edu.ifpb.ifmeetup.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TokenBlacklist extends BaseEntity {

    @Column(nullable = false, unique = true, length = 1024)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "invalidated_at", nullable = false)
    @CreatedDate
    private LocalDateTime invalidatedAt;

    
} 