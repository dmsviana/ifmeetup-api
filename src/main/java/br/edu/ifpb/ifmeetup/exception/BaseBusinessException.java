package br.edu.ifpb.ifmeetup.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe base para todas as exceções de negócio da aplicação.
 * Fornece estrutura padronizada com código de erro e detalhes adicionais.
 */
@Getter
public abstract class BaseBusinessException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, Object> details;
    
    protected BaseBusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    protected BaseBusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    protected BaseBusinessException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }
    
    protected BaseBusinessException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }
    
    /**
     * Adiciona um detalhe adicional à exceção.
     * 
     * @param key chave do detalhe
     * @param value valor do detalhe
     * @return esta instância para fluent interface
     */
    public BaseBusinessException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
    
    /**
     * Adiciona múltiplos detalhes à exceção.
     * 
     * @param details mapa com detalhes a serem adicionados
     * @return esta instância para fluent interface
     */
    public BaseBusinessException addDetails(Map<String, Object> details) {
        if (details != null) {
            this.details.putAll(details);
        }
        return this;
    }
} 