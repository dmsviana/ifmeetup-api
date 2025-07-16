package br.edu.ifpb.ifmeetup.exception;

import java.util.Map;

/**
 * Exceção para erros relacionados à autenticação.
 * Usada quando há problemas com credenciais, tokens ou processo de autenticação.
 */
public class AuthenticationException extends BaseBusinessException {
    
    private static final String DEFAULT_ERROR_CODE = "AUTHENTICATION_ERROR";
    
    public AuthenticationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
    
    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public AuthenticationException(String message, String errorCode, Map<String, Object> details) {
        super(message, errorCode, details);
    }
    
    /**
     * Cria uma exceção para credenciais inválidas.
     * 
     * @return nova instância de AuthenticationException
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Credenciais inválidas", "INVALID_CREDENTIALS");
    }
    
    /**
     * Override do método addDetail para retornar o tipo correto.
     */
    @Override
    public AuthenticationException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }
    
    /**
     * Cria uma exceção para token inválido.
     * 
     * @param tokenType o tipo de token (JWT, verificação, reset, etc.)
     * @return nova instância de AuthenticationException
     */
    public static AuthenticationException invalidToken(String tokenType) {
        return new AuthenticationException("Token inválido", "INVALID_TOKEN")
                .addDetail("tokenType", tokenType);
    }
    
    /**
     * Cria uma exceção para token expirado.
     * 
     * @param tokenType o tipo de token (JWT, verificação, reset, etc.)
     * @return nova instância de AuthenticationException
     */
    public static AuthenticationException expiredToken(String tokenType) {
        return new AuthenticationException("Token expirado", "EXPIRED_TOKEN")
                .addDetail("tokenType", tokenType);
    }
    
    /**
     * Cria uma exceção para token já utilizado.
     * 
     * @param tokenType o tipo de token (verificação, reset, etc.)
     * @return nova instância de AuthenticationException
     */
    public static AuthenticationException tokenAlreadyUsed(String tokenType) {
        return new AuthenticationException("Token já utilizado", "TOKEN_ALREADY_USED")
                .addDetail("tokenType", tokenType);
    }
    
    /**
     * Cria uma exceção para email não verificado.
     * 
     * @param email o email que não foi verificado
     * @return nova instância de AuthenticationException
     */
    public static AuthenticationException emailNotVerified(String email) {
        return new AuthenticationException(
                "Email não verificado. Por favor, verifique seu email antes de fazer login.", 
                "EMAIL_NOT_VERIFIED")
                .addDetail("email", email);
    }
} 