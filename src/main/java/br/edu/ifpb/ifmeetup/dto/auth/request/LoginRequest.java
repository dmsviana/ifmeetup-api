package br.edu.ifpb.ifmeetup.dto.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para autenticação de usuário")
public record LoginRequest(
    @Schema(description = "Email do usuário", example = "usuario@exemplo.com")
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,
    
    @Schema(description = "Senha do usuário", example = "senhaSecreta123")
    @NotBlank(message = "Senha é obrigatória")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password
) {
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTEGIDO]'" +
                '}';
    }
} 