package br.edu.ifpb.ifmeetup.exception;

import java.util.Map;

/**
 * Exceção para falhas em serviços externos.
 * Usada quando há problemas de comunicação com APIs externas, serviços de email, etc.
 */
public class ExternalServiceException extends BaseBusinessException {
    
    private static final String DEFAULT_ERROR_CODE = "EXTERNAL_SERVICE_ERROR";
    
    public ExternalServiceException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
    
    public ExternalServiceException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public ExternalServiceException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    public ExternalServiceException(String message, String errorCode, Map<String, Object> details) {
        super(message, errorCode, details);
    }
    
    public ExternalServiceException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, errorCode, details, cause);
    }
    
    /**
     * Override do método addDetail para retornar o tipo correto.
     */
    @Override
    public ExternalServiceException addDetail(String key, Object value) {
        super.addDetail(key, value);
        return this;
    }
    
    /**
     * Cria uma exceção para falha no envio de email.
     * 
     * @param emailAddress o endereço de email que falhou
     * @param cause a causa da falha
     * @return nova instância de ExternalServiceException
     */
    public static ExternalServiceException emailFailure(String emailAddress, Throwable cause) {
        return new ExternalServiceException(
                "Falha ao enviar email", 
                "EMAIL_SEND_FAILURE", 
                cause)
                .addDetail("emailAddress", emailAddress);
    }
    
    /**
     * Cria uma exceção para falha no envio de email com tipo específico.
     * 
     * @param emailAddress o endereço de email que falhou
     * @param emailType o tipo de email (verification, welcome, password-reset)
     * @param cause a causa da falha
     * @return nova instância de ExternalServiceException
     */
    public static ExternalServiceException emailFailure(String emailAddress, String emailType, Throwable cause) {
        return new ExternalServiceException(
                "Falha ao enviar email de " + emailType, 
                "EMAIL_SEND_FAILURE", 
                cause)
                .addDetail("emailAddress", emailAddress)
                .addDetail("emailType", emailType);
    }
    
    /**
     * Cria uma exceção para falha em API externa.
     * 
     * @param serviceName nome do serviço externo
     * @param statusCode código de status retornado (se aplicável)
     * @param cause a causa da falha
     * @return nova instância de ExternalServiceException
     */
    public static ExternalServiceException apiFailure(String serviceName, Integer statusCode, Throwable cause) {
        ExternalServiceException exception = new ExternalServiceException(
                "Falha na comunicação com serviço externo: " + serviceName, 
                "API_FAILURE", 
                cause)
                .addDetail("serviceName", serviceName);
        
        if (statusCode != null) {
            exception.addDetail("statusCode", statusCode);
        }
        
        return exception;
    }
    
    /**
     * Cria uma exceção para timeout em serviço externo.
     * 
     * @param serviceName nome do serviço externo
     * @param timeoutSeconds timeout em segundos
     * @return nova instância de ExternalServiceException
     */
    public static ExternalServiceException timeout(String serviceName, long timeoutSeconds) {
        return new ExternalServiceException(
                "Timeout ao comunicar com serviço externo: " + serviceName, 
                "SERVICE_TIMEOUT")
                .addDetail("serviceName", serviceName)
                .addDetail("timeoutSeconds", timeoutSeconds);
    }
} 