package br.edu.ifpb.ifmeetup.dto.room.request;

import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Set;

@Schema(description = "Dados para criação ou atualização de sala")
public record RoomRequest(
    
    @Schema(description = "Nome da sala", example = "Laboratório de Informática 01")
    @NotBlank(message = "Nome da sala é obrigatório")
    @Size(min = 3, max = 100, message = "Nome da sala deve ter entre 3 e 100 caracteres")
    String name,
    
    @Schema(description = "Localização da sala", example = "Bloco D, 2º andar")
    @Size(max = 255, message = "Localização deve ter no máximo 255 caracteres")
    String location,
    
    @Schema(description = "Capacidade máxima de pessoas", example = "40")
    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser maior que 0")
    @Max(value = 1000, message = "Capacidade deve ser no máximo 1000")
    Integer capacity,
    
    @Schema(description = "Tipo da sala", example = "LABORATORY")
    @NotNull(message = "Tipo da sala é obrigatório")
    RoomType type,
    
    @Schema(description = "Status operacional da sala", example = "AVAILABLE")
    @NotNull(message = "Status da sala é obrigatório")
    RoomStatus status,
    
    @Schema(description = "Descrição adicional da sala", example = "Laboratório equipado com 40 computadores e projetor")
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    String description,
    
    @Schema(description = "Recursos disponíveis na sala")
    Set<RoomResourceRequest> resources
) {
    @Override
    public String toString() {
        return "RoomRequest{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                ", type=" + type +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", resourcesCount=" + (resources != null ? resources.size() : 0) +
                '}';
    }
}