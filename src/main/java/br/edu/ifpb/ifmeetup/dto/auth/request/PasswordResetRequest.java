package br.edu.ifpb.ifmeetup.dto.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para redefinição de senha")
public record PasswordResetRequest(
    @Schema(description = "Token de redefinição recebido por email", example = "a1b2c3d4-5678-90ab-cdef-ghijklmnopqr")
    @NotBlank(message = "Token é obrigatório")
    String token,
    
    @Schema(description = "Nova senha (mínimo 8 caracteres)", example = "novaSenhaSecreta123")
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, message = "Nova senha deve ter no mínimo 8 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String newPassword
) {
    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "token='" + token + '\'' +
                ", newPassword='[PROTEGIDO]'" +
                '}';
    }
} 