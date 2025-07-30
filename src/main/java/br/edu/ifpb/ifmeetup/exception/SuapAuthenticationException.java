package br.edu.ifpb.ifmeetup.exception;

import java.util.Map;

/**
 * Exceção para falhas de autenticação específicas do SUAP.
 * Usada quando há problemas com credenciais inválidas ou falhas na autenticação SUAP.
 */
public class SuapAuthenticationException extends SuapIntegrationException {
    
    private static final String DEFAULT_ERROR_CODE = "SUAP_AUTHENTICATION_ERROR";
    
    public SuapAuthenticationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
    
    public SuapAuthenticationException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
    
    public SuapAuthenticationException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public SuapAuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    public SuapAuthenticationException(String message, String errorCode, Map<String, Object> details) {
        super(message, errorCode, details);
    }
    
    public SuapAuthenticationException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, errorCode, details, cause);
    }
    
    /**
     * Override do método addDetail para retornar o tipo correto.
     */
    @Override
    public SuapAuthenticationException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }
    
    /**
     * Override do método addDetails para retornar o tipo correto.
     */
    @Override
    public SuapAuthenticationException addDetails(Map<String, Object> details) {
        super.addDetails(details);
        return this;
    }
    
    /**
     * Cria uma exceção para credenciais inválidas.
     * 
     * @param matricula a matrícula que falhou na autenticação
     * @return nova instância de SuapAuthenticationException
     */
    public static SuapAuthenticationException invalidCredentials(String matricula) {
        return new SuapAuthenticationException(
                "Credenciais SUAP inválidas", 
                "SUAP_INVALID_CREDENTIALS")
                .addDetail("matricula", matricula);
    }
    
    /**
     * Cria uma exceção para credenciais inválidas com causa específica.
     * 
     * @param matricula a matrícula que falhou na autenticação
     * @param cause a causa da falha
     * @return nova instância de SuapAuthenticationException
     */
    public static SuapAuthenticationException invalidCredentials(String matricula, Throwable cause) {
        return new SuapAuthenticationException(
                "Credenciais SUAP inválidas", 
                "SUAP_INVALID_CREDENTIALS", 
                cause)
                .addDetail("matricula", matricula);
    }
    
    /**
     * Cria uma exceção para token SUAP inválido ou expirado.
     * 
     * @param tokenType tipo do token (access, refresh)
     * @return nova instância de SuapAuthenticationException
     */
    public static SuapAuthenticationException invalidToken(String tokenType) {
        return new SuapAuthenticationException(
                "Token SUAP inválido ou expirado", 
                "SUAP_INVALID_TOKEN")
                .addDetail("tokenType", tokenType);
    }
    
    /**
     * Cria uma exceção para falha na renovação de token.
     * 
     * @param cause a causa da falha
     * @return nova instância de SuapAuthenticationException
     */
    public static SuapAuthenticationException tokenRefreshFailure(Throwable cause) {
        return new SuapAuthenticationException(
                "Falha ao renovar token SUAP", 
                "SUAP_TOKEN_REFRESH_FAILURE", 
                cause);
    }
}