package br.edu.ifpb.ifmeetup.domain.entity;

import java.time.LocalDateTime;

import br.edu.ifpb.ifmeetup.domain.entity.base.BaseEntity;
import br.edu.ifpb.ifmeetup.domain.enums.ProfileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile extends BaseEntity {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false)
    private ProfileType profileType;

    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "verified")
    private boolean verified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;
    
} 