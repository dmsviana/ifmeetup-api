package br.edu.ifpb.ifmeetup.domain.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import br.edu.ifpb.ifmeetup.domain.entity.base.BaseEntity;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import br.edu.ifpb.ifmeetup.exception.ValidationException;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Room extends BaseEntity {

    @NotBlank(message = "O nome da sala é obrigatório")
    @Size(min = 3, max = 100, message = "O nome da sala deve ter entre 3 e 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 255, message = "A localização da sala deve ter no máximo 255 caracteres")
    @Column(name = "location", length = 255)
    private String location; // Ex: Bloco D, Laboratório com os pcs bom kjdsffk

    @NotNull(message = "A capacidade da sala é obrigatória")
    @Min(value = 1, message = "A capacidade da sala deve ser maior que 0")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomResource> inventory = new HashSet<>();

    @NotNull(message = "O tipo da sala é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RoomType type;

    @NotNull(message = "O status da sala é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RoomStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @CreatedBy
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @LastModifiedBy
    private User updatedBy;

    @Lob
    @Column(name = "description", length = 1000)
    private String description;


    public void addResource(RoomResource resource) {
        if (resource == null) {
            throw new ValidationException("Recurso não pode ser nulo");
        }
        
        this.inventory.add(resource);
        resource.setRoom(this);
    }

    public void removeResource(RoomResource resource) {
        this.inventory.remove(resource);
        resource.setRoom(null);
    }

}
