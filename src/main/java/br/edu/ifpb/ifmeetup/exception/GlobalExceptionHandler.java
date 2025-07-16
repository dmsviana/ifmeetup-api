package br.edu.ifpb.ifmeetup.exception;

import br.edu.ifpb.ifmeetup.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler global para tratamento de exceções da aplicação.
 * Implementa logging estruturado e respostas padronizadas.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Trata erros de validação de argumentos do método (Bean Validation).
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex, 
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, 
            @NonNull WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        String path = extractPath(request);
        
        // Log de nível DEBUG para erros de validação
        log.debug("Validation errors in request to {}: {}", path, errors);

        ErrorResponse errorResponse = ErrorResponse.validation(
                "Erros de validação encontrados", 
                path, 
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata exceções de validação customizadas.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            @NonNull ValidationException ex, 
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível DEBUG para validações customizadas
        log.debug("Custom validation error in request to {}: {} - Details: {}", 
                path, ex.getMessage(), sanitizeDetails(ex.getDetails()));

        ErrorResponse errorResponse = ErrorResponse.of(
                400,
                "Validation Error",
                ex.getMessage(),
                ex.getErrorCode(),
                path,
                sanitizeDetails(ex.getDetails())
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata exceções de autenticação customizadas.
     */
    @ExceptionHandler(br.edu.ifpb.ifmeetup.exception.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(
            @NonNull br.edu.ifpb.ifmeetup.exception.AuthenticationException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        // Log de nível WARNING para tentativas de autenticação falhadas
        log.warn("Authentication failure from IP {}: {} - Error code: {}", 
                clientIp, ex.getMessage(), ex.getErrorCode());

        ErrorResponse errorResponse = ErrorResponse.authentication(
                ex.getMessage(),
                ex.getErrorCode(),
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata exceções de credenciais inválidas do Spring Security.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            @NonNull BadCredentialsException ex, 
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        // Log de nível WARNING para credenciais inválidas
        log.warn("Bad credentials attempt from IP {}: {}", clientIp, path);

        ErrorResponse errorResponse = ErrorResponse.authentication(
                "Credenciais inválidas",
                "INVALID_CREDENTIALS",
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata exceções de autenticação do Spring Security.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSpringAuthenticationException(
            @NonNull AuthenticationException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        // Log de nível WARNING para problemas de autenticação
        log.warn("Spring authentication error from IP {}: {}", clientIp, ex.getClass().getSimpleName());

        ErrorResponse errorResponse = ErrorResponse.authentication(
                "Falha na autenticação",
                "AUTHENTICATION_FAILED",
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata exceções de acesso negado.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            @NonNull AccessDeniedException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        // Log de nível WARNING para tentativas de acesso não autorizado
        log.warn("Access denied from IP {} to path {}", clientIp, path);

        ErrorResponse errorResponse = ErrorResponse.authorization(
                "Acesso negado. Você não tem permissão para acessar este recurso.",
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Trata exceções de usuário já existente.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            @NonNull UserAlreadyExistsException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível WARNING para tentativas de registro duplicado
        log.warn("User registration conflict in request to {}: {}", path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.conflict(ex.getMessage(), path);

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Trata exceções de validação de negócio.
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(
            @NonNull BusinessValidationException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível WARNING para regras de negócio violadas
        log.warn("Business rule violation in request to {}: {}", path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.business(
                ex.getMessage(),
                "BUSINESS_VALIDATION_ERROR",
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Trata exceções de email não verificado.
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(
            @NonNull EmailNotVerifiedException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível INFO para tentativas de login com email não verificado
        log.info("Login attempt with unverified email in request to {}", path);

        ErrorResponse errorResponse = ErrorResponse.authentication(
                ex.getMessage(),
                "EMAIL_NOT_VERIFIED",
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata exceções de recurso não encontrado.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            @NonNull ResourceNotFoundException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível INFO para recursos não encontrados
        log.info("Resource not found in request to {}: {}", path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.notFound(ex.getMessage(), path);

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Trata exceções de serviços externos.
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            @NonNull ExternalServiceException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível ERROR para falhas em serviços externos
        log.error("External service failure in request to {}: {} - Error code: {} - Details: {}", 
                path, ex.getMessage(), ex.getErrorCode(), sanitizeDetails(ex.getDetails()), ex);

        ErrorResponse errorResponse = ErrorResponse.externalService(
                ex.getMessage(),
                ex.getErrorCode(),
                path,
                sanitizeDetails(ex.getDetails())
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Trata exceções de envio de email (compatibilidade com código existente).
     */
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendException(
            @NonNull EmailSendException ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível ERROR para falhas de email
        log.error("Email send failure in request to {}: {}", path, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.externalService(
                "Falha ao enviar email. Tente novamente mais tarde.",
                "EMAIL_SEND_FAILURE",
                path,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Trata todas as exceções não mapeadas especificamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            @NonNull Exception ex,
            @NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        
        // Log de nível ERROR para exceções não tratadas
        log.error("Unexpected error in request to {}: {} - Exception type: {}", 
                path, ex.getMessage(), ex.getClass().getSimpleName(), ex);

        ErrorResponse errorResponse = ErrorResponse.internal(
                "Ocorreu um erro interno no servidor. Tente novamente mais tarde.",
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extrai o caminho da requisição de forma segura.
     */
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }

    /**
     * Obtém o endereço IP do cliente de forma segura.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Sanitiza detalhes removendo informações sensíveis.
     */
    private Map<String, Object> sanitizeDetails(Map<String, Object> details) {
        if (details == null) {
            return null;
        }
        
        return details.entrySet().stream()
                .filter(entry -> !isSensitiveKey(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> sanitizeValue(entry.getValue())
                ));
    }

    /**
     * Verifica se uma chave contém informação sensível.
     */
    private boolean isSensitiveKey(String key) {
        if (key == null) return false;
        
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password") ||
               lowerKey.contains("token") ||
               lowerKey.contains("secret") ||
               lowerKey.contains("key") ||
               lowerKey.contains("credential");
    }

    /**
     * Sanitiza valores removendo informações sensíveis.
     */
    private Object sanitizeValue(Object value) {
        if (value instanceof String str) {
            // Mascarar emails parcialmente
            if (str.contains("@") && str.contains(".")) {
                String[] parts = str.split("@");
                if (parts.length == 2 && parts[0].length() > 2) {
                    return parts[0].substring(0, 2) + "***@" + parts[1];
                }
            }
        }
        return value;
    }
}
