package br.edu.ifpb.ifmeetup.controller.contract;

import br.edu.ifpb.ifmeetup.dto.auth.request.ForgotPasswordRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.LoginRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.PasswordResetRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.RegisterRequest;
import br.edu.ifpb.ifmeetup.dto.auth.response.AuthResponse;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Autenticação", description = "Endpoints relacionados à autenticação de usuários")
public interface AuthApiContract {

        @Operation(summary = "Login de usuário", description = "Autentica um usuário no sistema")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Email não verificado", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
        })
        ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response);

        @Operation(summary = "Registro de usuário", description = "Registra um novo usuário no sistema")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "422", description = "Dados inválidos ou email já cadastrado")
        })
        ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request);

        @Operation(summary = "Logout", description = "Realiza o logout do usuário atualmente autenticado e invalida o token JWT")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso", content = @Content(schema = @Schema(implementation = AuthResponse.class)))
        })
        ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response);

        @Operation(summary = "Recuperação de senha", description = "Envia um email com instruções para recuperação de senha")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email enviado com sucesso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "422", description = "Email não encontrado")
        })
        ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

        @Operation(summary = "Redefinição de senha", description = "Redefine a senha de um usuário usando um token de recuperação")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "422", description = "Token inválido ou expirado")
        })
        ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request);

        @Operation(summary = "Verificação de conta", description = "Verifica uma conta de usuário usando um token enviado por email")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Conta verificada com sucesso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "422", description = "Token inválido ou expirado")
        })
        ResponseEntity<AuthResponse> verifyAccount(@RequestParam("token") String token);

        @Operation(summary = "Obter usuário atual", description = "Retorna os dados do usuário atualmente autenticado")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Dados do usuário recuperados com sucesso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
        })
        ResponseEntity<AuthResponse> me();

        @Operation(
                summary = "Login SUAP", 
                description = """
                        Autentica um usuário do IFPB usando credenciais SUAP.
                        
                        Este endpoint permite que servidores e alunos do IFPB façam login no sistema usando suas credenciais 
                        institucionais. O sistema automaticamente:
                        - Valida as credenciais no SUAP
                        - Busca os dados do usuário (servidor ou aluno)
                        - Cria ou atualiza a conta no IFMeetup
                        - Atribui o perfil apropriado (STUDENT, TEACHER, COORDINATOR)
                        - Retorna um token JWT válido para o IFMeetup
                        
                        **Tipos de usuário suportados:**
                        - **Alunos**: Recebem automaticamente o perfil STUDENT
                        - **Professores**: Recebem perfil TEACHER ou COORDINATOR baseado na função
                        - **Servidores administrativos**: Recebem perfil baseado no cargo
                        
                        **Mapeamento de perfis:**
                        - STUDENT: Todos os alunos (userType = ALUNO)
                        - COORDINATOR: Professores com função de coordenação ou cargos administrativos de coordenação
                        - TEACHER: Professores sem função de coordenação e outros servidores
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(
                                responseCode = "200", 
                                description = "Login SUAP realizado com sucesso",
                                content = @Content(
                                        schema = @Schema(implementation = AuthResponse.class),
                                        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                name = "Login bem-sucedido",
                                                summary = "Resposta de login SUAP bem-sucedido",
                                                value = """
                                                        {
                                                          "message": "Login realizado com sucesso",
                                                          "user": {
                                                            "id": "123e4567-e89b-12d3-a456-426614174000",
                                                            "name": "Felipe Omena",
                                                            "email": "1323726@ifpb.edu.br",
                                                            "profileType": "COORDINATOR",
                                                            "verified": true,
                                                            "createdAt": "2024-01-15T10:30:00"
                                                          },
                                                          "sessionId": "987fcdeb-51a2-43d7-b123-987654321000",
                                                          "expiresAt": "2024-01-16T10:30:00",
                                                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                        }
                                                        """
                                        )
                                )
                        ),
                        @ApiResponse(
                                responseCode = "400", 
                                description = "Dados de entrada inválidos",
                                content = @Content(
                                        schema = @Schema(implementation = br.edu.ifpb.ifmeetup.dto.error.ErrorResponse.class),
                                        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                name = "Validação de entrada",
                                                summary = "Erro de validação dos dados de entrada",
                                                value = """
                                                        {
                                                          "timestamp": "2024-01-15T10:30:00",
                                                          "status": 400,
                                                          "error": "Validation Error",
                                                          "message": "Erros de validação encontrados",
                                                          "errorCode": "VALIDATION_ERROR",
                                                          "path": "/auth/suap/login",
                                                          "details": {
                                                            "validationErrors": [
                                                              "Username deve conter entre 7 e 12 dígitos",
                                                              "Password é obrigatório"
                                                            ]
                                                          }
                                                        }
                                                        """
                                        )
                                )
                        ),
                        @ApiResponse(
                                responseCode = "401", 
                                description = "Credenciais SUAP inválidas",
                                content = @Content(
                                        schema = @Schema(implementation = br.edu.ifpb.ifmeetup.dto.error.ErrorResponse.class),
                                        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                name = "Credenciais inválidas",
                                                summary = "Matrícula ou senha incorreta",
                                                value = """
                                                        {
                                                          "timestamp": "2024-01-15T10:30:00",
                                                          "status": 401,
                                                          "error": "Authentication Error",
                                                          "message": "Matrícula ou senha incorreta. Verifique suas credenciais SUAP e tente novamente. Você pode tentar fazer login usando seu email e senha cadastrados no sistema.",
                                                          "errorCode": "SUAP_INVALID_CREDENTIALS",
                                                          "path": "/auth/suap/login"
                                                        }
                                                        """
                                        )
                                )
                        ),
                        @ApiResponse(
                                responseCode = "404", 
                                description = "Usuário não encontrado no SUAP",
                                content = @Content(
                                        schema = @Schema(implementation = br.edu.ifpb.ifmeetup.dto.error.ErrorResponse.class),
                                        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                name = "Usuário não encontrado",
                                                summary = "Usuário não existe no SUAP",
                                                value = """
                                                        {
                                                          "timestamp": "2024-01-15T10:30:00",
                                                          "status": 404,
                                                          "error": "Not Found",
                                                          "message": "Usuário não encontrado no SUAP. Verifique se sua matrícula está correta. Você pode tentar fazer login usando seu email e senha cadastrados no sistema.",
                                                          "errorCode": "SUAP_USER_NOT_FOUND",
                                                          "path": "/auth/suap/login"
                                                        }
                                                        """
                                        )
                                )
                        ),
                        @ApiResponse(
                                responseCode = "502", 
                                description = "Erro de comunicação com SUAP",
                                content = @Content(
                                        schema = @Schema(implementation = br.edu.ifpb.ifmeetup.dto.error.ErrorResponse.class),
                                        examples = {
                                                @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                        name = "Timeout",
                                                        summary = "Timeout na comunicação com SUAP",
                                                        value = """
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 502,
                                                                  "error": "External Service Error",
                                                                  "message": "O SUAP está demorando para responder. Tente novamente em alguns minutos. Você pode tentar fazer login usando seu email e senha cadastrados no sistema.",
                                                                  "errorCode": "SUAP_TIMEOUT",
                                                                  "path": "/auth/suap/login"
                                                                }
                                                                """
                                                ),
                                                @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                        name = "Serviço indisponível",
                                                        summary = "SUAP temporariamente indisponível",
                                                        value = """
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 502,
                                                                  "error": "External Service Error",
                                                                  "message": "O SUAP está temporariamente indisponível. Tente novamente em alguns minutos. Você pode tentar fazer login usando seu email e senha cadastrados no sistema.",
                                                                  "errorCode": "SUAP_TEMPORARILY_UNAVAILABLE",
                                                                  "path": "/auth/suap/login"
                                                                }
                                                                """
                                                )
                                        }
                                )
                        )
        })
        ResponseEntity<AuthResponse> loginWithSuap(@Valid @RequestBody SuapLoginRequest request,
                        HttpServletResponse response);
}