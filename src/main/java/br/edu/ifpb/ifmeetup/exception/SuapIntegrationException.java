package br.edu.ifpb.ifmeetup.exception;

import java.util.Map;

/**
 * Exceção base para todas as exceções relacionadas à integração com SUAP.
 * Fornece estrutura padronizada para erros específicos da integração SUAP.
 */
public class SuapIntegrationException extends BaseBusinessException {
    
    private static final String DEFAULT_ERROR_CODE = "SUAP_INTEGRATION_ERROR";
    
    public SuapIntegrationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
    
    public SuapIntegrationException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
    
    public SuapIntegrationException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public SuapIntegrationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    public SuapIntegrationException(String message, String errorCode, Map<String, Object> details) {
        super(message, errorCode, details);
    }
    
    public SuapIntegrationException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, errorCode, details, cause);
    }
    
    /**
     * Override do método addDetail para retornar o tipo correto.
     */
    @Override
    public SuapIntegrationException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }
    
    /**
     * Override do método addDetails para retornar o tipo correto.
     */
    @Override
    public SuapIntegrationException addDetails(Map<String, Object> details) {
        super.addDetails(details);
        return this;
    }
}