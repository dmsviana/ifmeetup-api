package br.edu.ifpb.ifmeetup.integration.suap.config;

import br.edu.ifpb.ifmeetup.exception.SuapAuthenticationException;
import br.edu.ifpb.ifmeetup.exception.SuapDataNotFoundException;
import br.edu.ifpb.ifmeetup.exception.SuapServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Decodificador de erros específico para APIs do SUAP.
 * Mapeia códigos de erro HTTP para exceções de negócio apropriadas.
 */
@Component
@Slf4j
public class SuapErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultErrorDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        String endpoint = extractEndpoint(methodKey);
        int status = response.status();
        String responseBody = extractResponseBody(response);
        
        log.debug("SUAP API error - Method: {}, Status: {}, Body: {}", methodKey, status, responseBody);
        
        return switch (status) {
            case 401 -> handleUnauthorized(endpoint, responseBody);
            case 404 -> handleNotFound(endpoint, responseBody);
            case 500, 502, 503, 504 -> handleServerError(endpoint, status, responseBody);
            default -> {
                log.warn("Unmapped SUAP error - Status: {}, Method: {}, Body: {}", status, methodKey, responseBody);
                yield defaultErrorDecoder.decode(methodKey, response);
            }
        };
    }
    
    /**
     * Trata erros de autenticação (401).
     */
    private Exception handleUnauthorized(String endpoint, String responseBody) {
        if (endpoint.contains("jwt/obtain_token") || endpoint.contains("jwt/refresh_token")) {
            log.warn("SUAP authentication failed for endpoint: {}", endpoint);
            return SuapAuthenticationException.invalidCredentials("unknown")
                    .addDetail("endpoint", endpoint)
                    .addDetail("responseBody", responseBody);
        } else {
            log.warn("SUAP token invalid or expired for endpoint: {}", endpoint);
            return SuapAuthenticationException.invalidToken("access")
                    .addDetail("endpoint", endpoint)
                    .addDetail("responseBody", responseBody);
        }
    }
    
    /**
     * Trata erros de recurso não encontrado (404).
     */
    private Exception handleNotFound(String endpoint, String responseBody) {
        log.info("SUAP data not found for endpoint: {}", endpoint);
        
        if (endpoint.contains("servidores")) {
            return SuapDataNotFoundException.servidorNotFound("unknown")
                    .addDetail("endpoint", endpoint)
                    .addDetail("responseBody", responseBody);
        } else if (endpoint.contains("alunos")) {
            return SuapDataNotFoundException.alunoNotFound("unknown")
                    .addDetail("endpoint", endpoint)
                    .addDetail("responseBody", responseBody);
        } else {
            return SuapDataNotFoundException.userNotFound("unknown", "unknown")
                    .addDetail("endpoint", endpoint)
                    .addDetail("responseBody", responseBody);
        }
    }
    
    /**
     * Trata erros de servidor (5xx).
     */
    private Exception handleServerError(String endpoint, int status, String responseBody) {
        log.error("SUAP server error - Status: {}, Endpoint: {}, Body: {}", status, endpoint, responseBody);
        
        return switch (status) {
            case 500 -> SuapServiceUnavailableException.internalError(endpoint, status)
                    .addDetail("responseBody", responseBody);
            case 502, 503, 504 -> SuapServiceUnavailableException.temporarilyUnavailable(endpoint, status)
                    .addDetail("responseBody", responseBody);
            default -> SuapServiceUnavailableException.internalError(endpoint, status)
                    .addDetail("responseBody", responseBody);
        };
    }
    
    /**
     * Extrai o endpoint da chave do método.
     */
    private String extractEndpoint(String methodKey) {
        if (methodKey == null) {
            return "unknown";
        }
        
        // methodKey format: "ClassName#methodName(paramTypes)"
        int hashIndex = methodKey.indexOf('#');
        if (hashIndex > 0) {
            return methodKey.substring(hashIndex + 1);
        }
        
        return methodKey;
    }
    
    /**
     * Extrai o corpo da resposta de forma segura.
     */
    private String extractResponseBody(Response response) {
        try {
            if (response.body() != null) {
                byte[] bodyBytes = response.body().asInputStream().readAllBytes();
                String body = new String(bodyBytes, StandardCharsets.UTF_8);
                
                // Limitar tamanho do corpo da resposta nos logs
                if (body.length() > 500) {
                    return body.substring(0, 500) + "... [truncated]";
                }
                
                return body;
            }
        } catch (IOException e) {
            log.debug("Failed to read response body", e);
        }
        
        return "Unable to read response body";
    }
}