package br.edu.ifpb.ifmeetup.dto.auth.response;

import br.edu.ifpb.ifmeetup.dto.user.response.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta padrão para operações de autenticação")
public record AuthResponse(
    @Schema(description = "Mensagem informativa sobre o resultado da operação")
    String message,
    
    @Schema(description = "Dados do usuário, quando aplicável")
    UserResponse user,
    
    @Schema(description = "Identificador único da sessão, quando aplicável")
    UUID sessionId,
    
    @Schema(description = "Data e hora de expiração da sessão, quando aplicável")
    LocalDateTime expiresAt,

    @Schema(description = "Token JWT de autenticação, quando aplicável")
    String token
) {
    public static AuthResponse success(String message, UserResponse user, UUID sessionId, LocalDateTime expiresAt, String token) {
        return new AuthResponse(message, user, sessionId, expiresAt, token);
    }

    public static AuthResponse success(String message) {
        return new AuthResponse(message, null, null, null, null);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(message, null, null, null, null);
    }
} 