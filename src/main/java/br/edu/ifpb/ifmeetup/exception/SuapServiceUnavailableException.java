package br.edu.ifpb.ifmeetup.exception;

import java.util.Map;

/**
 * Exceção para quando o serviço SUAP está indisponível ou com problemas.
 * Usada para erros de conectividade, timeouts e erros internos do SUAP.
 */
public class SuapServiceUnavailableException extends SuapIntegrationException {
    
    private static final String DEFAULT_ERROR_CODE = "SUAP_SERVICE_UNAVAILABLE";
    
    public SuapServiceUnavailableException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
    
    public SuapServiceUnavailableException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
    
    public SuapServiceUnavailableException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public SuapServiceUnavailableException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    public SuapServiceUnavailableException(String message, String errorCode, Map<String, Object> details) {
        super(message, errorCode, details);
    }
    
    public SuapServiceUnavailableException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, errorCode, details, cause);
    }
    
    /**
     * Override do método addDetail para retornar o tipo correto.
     */
    @Override
    public SuapServiceUnavailableException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }
    
    /**
     * Override do método addDetails para retornar o tipo correto.
     */
    @Override
    public SuapServiceUnavailableException addDetails(Map<String, Object> details) {
        super.addDetails(details);
        return this;
    }
    
    /**
     * Cria uma exceção para timeout na comunicação com SUAP.
     * 
     * @param endpoint o endpoint que teve timeout
     * @param timeoutMs timeout em milissegundos
     * @return nova instância de SuapServiceUnavailableException
     */
    public static SuapServiceUnavailableException timeout(String endpoint, long timeoutMs) {
        return new SuapServiceUnavailableException(
                "Timeout na comunicação com SUAP", 
                "SUAP_TIMEOUT")
                .addDetail("endpoint", endpoint)
                .addDetail("timeoutMs", timeoutMs);
    }
    
    /**
     * Cria uma exceção para timeout na comunicação com SUAP com causa específica.
     * 
     * @param endpoint o endpoint que teve timeout
     * @param timeoutMs timeout em milissegundos
     * @param cause a causa do timeout
     * @return nova instância de SuapServiceUnavailableException
     */
    public static SuapServiceUnavailableException timeout(String endpoint, long timeoutMs, Throwable cause) {
        return new SuapServiceUnavailableException(
                "Timeout na comunicação com SUAP", 
                "SUAP_TIMEOUT", 
                cause)
                .addDetail("endpoint", endpoint)
                .addDetail("timeoutMs", timeoutMs);
    }
    
    /**
     * Cria uma exceção para erro interno do SUAP (5xx).
     * 
     * @param endpoint o endpoint que retornou erro
     * @param statusCode código de status HTTP
     * @return nova instância de SuapServiceUnavailableException
     */
    public static SuapServiceUnavailableException internalError(String endpoint, int statusCode) {
        return new SuapServiceUnavailableException(
                "Erro interno do SUAP", 
                "SUAP_INTERNAL_ERROR")
                .addDetail("endpoint", endpoint)
                .addDetail("statusCode", statusCode);
    }
    
    /**
     * Cria uma exceção para erro interno do SUAP (5xx) com causa específica.
     * 
     * @param endpoint o endpoint que retornou erro
     * @param statusCode código de status HTTP
     * @param cause a causa do erro
     * @return nova instância de SuapServiceUnavailableException
     */
    public static SuapServiceUnavailableException internalError(String endpoint, int statusCode, Throwable cause) {
        return new SuapServiceUnavailableException(
                "Erro interno do SUAP", 
                "SUAP_INTERNAL_ERROR", 
                cause)
                .addDetail("endpoint", endpoint)
                .addDetail("statusCode", statusCode);
    }
    
    /**
     * Cria uma exceção para falha de conectividade com SUAP.
     * 
     * @param endpoint o endpoint que falhou
     * @param cause a causa da falha de conectividade
     * @return nova instância de SuapServiceUnavailableException
     */
    public static SuapServiceUnavailableException connectivityFailure(String endpoint, Throwable cause) {
        return new SuapServiceUnavailableException(
                "Falha de conectividade com SUAP", 
                "SUAP_CONNECTIVITY_FAILURE", 
                cause)
                .addDetail("endpoint", endpoint);
    }
    
    /**
     * Cria uma exceção para serviço SUAP temporariamente indisponível.
     * 
     * @param endpoint o endpoint indisponível
     * @param statusCode código de status HTTP (503, 502, etc.)
     * @return nova instância de SuapServiceUnavailableException
     */
    public static SuapServiceUnavailableException temporarilyUnavailable(String endpoint, int statusCode) {
        return new SuapServiceUnavailableException(
                "Serviço SUAP temporariamente indisponível", 
                "SUAP_TEMPORARILY_UNAVAILABLE")
                .addDetail("endpoint", endpoint)
                .addDetail("statusCode", statusCode);
    }
}