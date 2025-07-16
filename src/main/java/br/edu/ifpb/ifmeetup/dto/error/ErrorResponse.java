package br.edu.ifpb.ifmeetup.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estrutura padronizada para respostas de erro da API.
 * 
 * @param timestamp momento em que o erro ocorreu
 * @param status código de status HTTP
 * @param error descrição do tipo de erro
 * @param message mensagem descritiva do erro
 * @param errorCode código específico do erro para identificação programática
 * @param path caminho da requisição que gerou o erro
 * @param details detalhes adicionais sobre o erro (opcional)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String errorCode,
        String path,
        Map<String, Object> details
) {

    /**
     * Cria uma resposta de erro básica.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                null,
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro com código de erro específico.
     */
    public static ErrorResponse of(int status, String error, String message, String errorCode, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                errorCode,
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro completa com detalhes.
     */
    public static ErrorResponse of(int status, String error, String message, String errorCode, String path, Map<String, Object> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                errorCode,
                path,
                details != null ? new HashMap<>(details) : null
        );
    }

    /**
     * Cria uma resposta de erro para validação com lista de erros.
     */
    public static ErrorResponse validation(String message, String path, List<String> validationErrors) {
        Map<String, Object> details = new HashMap<>();
        details.put("validationErrors", validationErrors);

        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Validation Error",
                message,
                "VALIDATION_ERROR",
                path,
                details
        );
    }

    /**
     * Cria uma resposta de erro para validação com detalhes de campo.
     */
    public static ErrorResponse validationField(String message, String path, String field, Object rejectedValue) {
        Map<String, Object> details = new HashMap<>();
        details.put("field", field);
        details.put("rejectedValue", rejectedValue);

        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Validation Error",
                message,
                "VALIDATION_ERROR",
                path,
                details
        );
    }

    /**
     * Cria uma resposta de erro de autenticação.
     */
    public static ErrorResponse authentication(String message, String errorCode, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                401,
                "Authentication Error",
                message,
                errorCode,
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro de autorização.
     */
    public static ErrorResponse authorization(String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                403,
                "Authorization Error",
                message,
                "ACCESS_DENIED",
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro para recurso não encontrado.
     */
    public static ErrorResponse notFound(String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "Not Found",
                message,
                "RESOURCE_NOT_FOUND",
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro para conflito (recurso já existe).
     */
    public static ErrorResponse conflict(String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                409,
                "Conflict",
                message,
                "RESOURCE_CONFLICT",
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro para regra de negócio.
     */
    public static ErrorResponse business(String message, String errorCode, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                422,
                "Business Rule Violation",
                message,
                errorCode,
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro para regra de negócio com detalhes.
     */
    public static ErrorResponse business(String message, String errorCode, String path, Map<String, Object> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                422,
                "Business Rule Violation",
                message,
                errorCode,
                path,
                details != null ? new HashMap<>(details) : null
        );
    }

    /**
     * Cria uma resposta de erro interno do servidor.
     */
    public static ErrorResponse internal(String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                500,
                "Internal Server Error",
                message,
                "INTERNAL_ERROR",
                path,
                null
        );
    }

    /**
     * Cria uma resposta de erro de serviço externo.
     */
    public static ErrorResponse externalService(String message, String errorCode, String path, Map<String, Object> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                502,
                "External Service Error",
                message,
                errorCode,
                path,
                details != null ? new HashMap<>(details) : null
        );
    }
} 