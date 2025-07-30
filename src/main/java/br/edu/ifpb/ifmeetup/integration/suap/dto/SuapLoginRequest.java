package br.edu.ifpb.ifmeetup.integration.suap.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(
    description = "Dados de login para autenticação SUAP",
    example = """
            {
              "username": "1323726",
              "password": "minhasenha123"
            }
            """
)
public record SuapLoginRequest(
    
    @Schema(
        description = "Matrícula do usuário no SUAP (7 a 12 dígitos)",
        example = "1323726",
        pattern = "^\\d{7,12}$",
        minLength = 7,
        maxLength = 12
    )
    @NotBlank(message = "Username é obrigatório")
    @Pattern(regexp = "^\\d{7,12}$", message = "Username deve conter entre 7 e 12 dígitos")
    String username,
    
    @Schema(
        description = "Senha do usuário no SUAP",
        example = "minhasenha123",
        format = "password"
    )
    @NotBlank(message = "Password é obrigatório")
    String password
) {
    
    public SuapLoginRequest {
        if (username != null) {
            username = username.trim();
        }
        if (password != null) {
            password = password.trim();
        }
    }
}