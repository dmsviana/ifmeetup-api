package br.edu.ifpb.ifmeetup.controller.contract;

import br.edu.ifpb.ifmeetup.dto.auth.request.ForgotPasswordRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.LoginRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.PasswordResetRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.RegisterRequest;
import br.edu.ifpb.ifmeetup.dto.auth.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Autenticação", description = "Endpoints relacionados à autenticação de usuários")
public interface AuthApiContract {

    @Operation(summary = "Login de usuário", description = "Autentica um usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "403", description = "Email não verificado",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response);

    @Operation(summary = "Registro de usuário", description = "Registra um novo usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos ou email já cadastrado")
    })
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "Logout", description = "Realiza o logout do usuário atualmente autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    })
    ResponseEntity<AuthResponse> logout();

    @Operation(summary = "Recuperação de senha", description = "Envia um email com instruções para recuperação de senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email enviado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "422", description = "Email não encontrado")
    })
    ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @Operation(summary = "Redefinição de senha", description = "Redefine a senha de um usuário usando um token de recuperação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "422", description = "Token inválido ou expirado")
    })
    ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request);

    @Operation(summary = "Verificação de conta", description = "Verifica uma conta de usuário usando um token enviado por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta verificada com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "422", description = "Token inválido ou expirado")
    })
    ResponseEntity<AuthResponse> verifyAccount(@RequestParam("token") String token);

    @Operation(summary = "Obter usuário atual", description = "Retorna os dados do usuário atualmente autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário recuperados com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    ResponseEntity<AuthResponse> me();
} 