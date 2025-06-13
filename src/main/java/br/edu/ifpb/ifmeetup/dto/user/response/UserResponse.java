package br.edu.ifpb.ifmeetup.dto.user.response;

import br.edu.ifpb.ifmeetup.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta com dados básicos do usuário")
public record UserResponse(
    @Schema(description = "Identificador único do usuário")
    UUID id,
    
    @Schema(description = "Nome completo do usuário")
    String name,
    
    @Schema(description = "Email do usuário")
    String email,
    
    @Schema(description = "Número de telefone do usuário")
    String phoneNumber,
    
    @Schema(description = "Lista de papéis/roles do usuário no sistema")
    Set<String> roles
) {
    public static UserResponse fromEntity(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        
        return new UserResponse(
            user.getId(),
            user.getFirstName() + " " + user.getLastName(),
            user.getEmail(),
            user.getPhoneNumber(),
            roleNames
        );
    }
} 