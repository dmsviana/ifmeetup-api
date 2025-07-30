package br.edu.ifpb.ifmeetup.integration.suap.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public ErrorDecoder errorDecoder(SuapErrorDecoder suapErrorDecoder) {
        return suapErrorDecoder;
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
}
