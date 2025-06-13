package br.edu.ifpb.ifmeetup.domain.entity;

import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "room_inventory_resources",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "resource_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"resourceType"})
public class RoomResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "O tipo do recurso é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private ResourceType resourceType;

    @NotNull(message = "A quantidade do recurso é obrigatória")
    @Min(value = 0, message = "A quantidade do recurso não pode ser negativa.")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 255, message = "Os detalhes do recurso devem ter no máximo 255 caracteres")
    @Column(name = "details")
    private String details;
}