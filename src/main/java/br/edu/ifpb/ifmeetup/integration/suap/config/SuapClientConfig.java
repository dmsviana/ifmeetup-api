package br.edu.ifpb.ifmeetup.integration.suap.config;

import br.edu.ifpb.ifmeetup.exception.ExternalServiceException;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Response;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Configuração OpenFeign para clientes SUAP.
 * 
 * Responsável por:
 * - Configurar timeouts de conexão e leitura
 * - Implementar estratégia de retry
 * - Configurar interceptors para headers padrão
 * - Mapear erros HTTP específicos do SUAP
 * - Configurar logs detalhados
 */
@Slf4j
@Configuration
public class SuapClientConfig {

    @Value("${suap.api.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${suap.api.timeout.read:15000}")
    private int readTimeout;

    @Value("${suap.api.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${suap.api.retry.delay:1000}")
    private long retryDelay;

    /**
     * Configura timeouts para requisições OpenFeign.
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS,
                true);
    }

    /**
     * Configura estratégia de retry para falhas temporárias.
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(retryDelay, retryDelay * 2, maxAttempts);
    }

    /**
     * Interceptor para adicionar headers padrão nas requisições.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Adiciona headers padrão
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("User-Agent", "IFMeetup-Integration/1.0");

            // Log da requisição
            log.debug("SUAP Request: {} {}",
                    requestTemplate.method(),
                    requestTemplate.url());
        };
    }

    /**
     * Decoder customizado para mapear erros HTTP específicos do SUAP.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new SuapErrorDecoder();
    }

    /**
     * Configura nível de log para requisições Feign.
     * BASIC: logs apenas método HTTP, URL e código de resposta
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Interceptor adicional para logs de performance e debug.
     */
    @Bean
    public RequestInterceptor performanceInterceptor() {
        return requestTemplate -> {
            long startTime = System.currentTimeMillis();
            requestTemplate.header("X-Request-Start-Time", String.valueOf(startTime));

            log.debug("SUAP Request Performance - Start: {} ms", startTime);
        };
    }

    /**
     * Decoder de erros específico para APIs do SUAP.
     * 
     * Mapeia códigos de erro HTTP para exceções de negócio apropriadas,
     * facilitando o tratamento de erros específicos da integração SUAP.
     */
    @Slf4j
    public static class SuapErrorDecoder implements ErrorDecoder {

        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            String requestUrl = response.request().url();
            int status = response.status();
            String reason = response.reason();

            log.error("SUAP API Error - Method: {}, URL: {}, Status: {}, Reason: {}",
                    methodKey, requestUrl, status, reason);

            // Extrai corpo da resposta para logs detalhados
            String responseBody = extractResponseBody(response);
            if (responseBody != null && !responseBody.isEmpty()) {
                log.error("SUAP API Error Body: {}", responseBody);
            }

            return switch (status) {
                case 400 -> new ExternalServiceException(
                        "Requisição inválida: " + reason, "SUAP_BAD_REQUEST")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status)
                        .addDetail("responseBody", responseBody);

                case 401 -> new ExternalServiceException(
                        "Credenciais inválidas ou token expirado", "SUAP_AUTH_FAILED")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                case 403 -> new ExternalServiceException(
                        "Acesso negado pelo SUAP", "SUAP_ACCESS_DENIED")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                case 404 -> new ExternalServiceException(
                        "Recurso não encontrado no SUAP", "SUAP_RESOURCE_NOT_FOUND")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                case 429 -> new ExternalServiceException(
                        "Limite de requisições excedido", "SUAP_RATE_LIMIT")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                case 500 -> new ExternalServiceException(
                        "Erro interno do servidor SUAP", "SUAP_INTERNAL_ERROR")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                case 502 -> new ExternalServiceException(
                        "SUAP indisponível (Bad Gateway)", "SUAP_BAD_GATEWAY")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                case 503 -> new ExternalServiceException(
                        "SUAP temporariamente indisponível", "SUAP_SERVICE_UNAVAILABLE")
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                case 504 -> ExternalServiceException.timeout("SUAP", 15)
                        .addDetail("method", methodKey)
                        .addDetail("url", requestUrl)
                        .addDetail("statusCode", status);

                default -> {
                    log.warn("Unmapped SUAP API error status: {}", status);
                    yield defaultErrorDecoder.decode(methodKey, response);
                }
            };
        }

        /**
         * Extrai o corpo da resposta para logging detalhado.
         */
        private String extractResponseBody(Response response) {
            try {
                if (response.body() != null) {
                    byte[] bodyBytes = response.body().asInputStream().readAllBytes();
                    return new String(bodyBytes, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.warn("Failed to extract response body for error logging", e);
            }
            return null;
        }
    }
}