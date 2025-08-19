package br.edu.ifpb.ifmeetup.dto.auth.request;

import br.edu.ifpb.ifmeetup.domain.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para registro de novo usuário")
public record RegisterRequest(
    
    @Schema(description = "Nome do usuário", example = "Maria")
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
    String firstName,
    
    @Schema(description = "Sobrenome do usuário", example = "Silva")
    @NotBlank(message = "Sobrenome é obrigatório") 
    @Size(min = 2, max = 50, message = "Sobrenome deve ter entre 2 e 50 caracteres")
    String lastName,
    
    @Schema(description = "Email do usuário", example = "maria.silva@exemplo.com")
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,
    
    @Schema(description = "Número de telefone do usuário", example = "(83) 99999-9999")
    @Pattern(regexp = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}-?\\d{4}$", message = "Formato de telefone inválido")
    @NotBlank(message = "Telefone é obrigatório")
    String phoneNumber,
    
    @Schema(description = "Senha do usuário (mínimo 8 caracteres)", example = "senhaSecreta123")
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password,
    
    @Schema(description = "Tipo de perfil do usuário", example = "STUDENT")
    @NotNull(message = "Tipo de perfil é obrigatório")
    ProfileType profileType
) {
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='[PROTEGIDO]'" +
                ", profileType=" + profileType +
                '}';
    }
}