package br.edu.ifpb.ifmeetup.exception;

import java.util.List;
import java.util.Map;

/**
 * Exceção para erros de validação de dados de entrada.
 * Usada quando dados fornecidos pelo usuário não atendem aos critérios de validação.
 */
public class ValidationException extends BaseBusinessException {
    
    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";
    
    public ValidationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
    
    public ValidationException(String message, Map<String, Object> details) {
        super(message, DEFAULT_ERROR_CODE, details);
    }
    
    public ValidationException(String message, String field, Object rejectedValue) {
        super(message, DEFAULT_ERROR_CODE);
        addDetail("field", field);
        addDetail("rejectedValue", rejectedValue);
    }
    
    public ValidationException(String message, List<String> validationErrors) {
        super(message, DEFAULT_ERROR_CODE);
        addDetail("validationErrors", validationErrors);
    }
    
    /**
     * Override do método addDetail para retornar o tipo correto.
     */
    @Override
    public ValidationException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }
    
    /**
     * Cria uma exceção de validação para um campo específico.
     * 
     * @param field o campo que falhou na validação
     * @param rejectedValue o valor rejeitado
     * @param message a mensagem de erro
     * @return nova instância de ValidationException
     */
    public static ValidationException forField(String field, Object rejectedValue, String message) {
        return new ValidationException(message, field, rejectedValue);
    }
    
    /**
     * Cria uma exceção de validação para múltiplos erros.
     * 
     * @param validationErrors lista de erros de validação
     * @return nova instância de ValidationException
     */
    public static ValidationException forMultipleErrors(List<String> validationErrors) {
        return new ValidationException("Erros de validação encontrados", validationErrors);
    }
} 