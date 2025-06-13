package br.edu.ifpb.ifmeetup.domain.entity;

import java.util.Set;

import br.edu.ifpb.ifmeetup.domain.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {
    
    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    
    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;
} 