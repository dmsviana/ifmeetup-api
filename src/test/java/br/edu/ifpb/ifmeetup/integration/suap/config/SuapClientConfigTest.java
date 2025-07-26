package br.edu.ifpb.ifmeetup.integration.suap.config;

import br.edu.ifpb.ifmeetup.exception.ExternalServiceException;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Testes unitários para SuapClientConfig.
 * 
 * Verifica se as configurações OpenFeign estão sendo aplicadas corretamente,
 * incluindo timeouts, retry, interceptors e mapeamento de erros.
 */
class SuapClientConfigTest {

    private SuapClientConfig config;

    @BeforeEach
    void setUp() {
        config = new SuapClientConfig();
        // Configura valores de teste usando reflection
        ReflectionTestUtils.setField(config, "connectTimeout", 5000);
        ReflectionTestUtils.setField(config, "readTimeout", 15000);
        ReflectionTestUtils.setField(config, "maxAttempts", 3);
        ReflectionTestUtils.setField(config, "retryDelay", 1000L);
    }

    @Test
    void shouldConfigureRequestOptionsWithCorrectTimeouts() {
        // When
        Request.Options options = config.requestOptions();

        // Then
        assertThat(options.connectTimeoutMillis()).isEqualTo(5000);
        assertThat(options.readTimeoutMillis()).isEqualTo(15000);
        assertThat(options.isFollowRedirects()).isTrue();
    }

    @Test
    void shouldConfigureRetryerWithCorrectParameters() {
        // When
        Retryer retryer = config.retryer();

        // Then
        assertInstanceOf(Retryer.Default.class, retryer);
    }

    @Test
    void shouldCreateSuapErrorDecoder() {
        // When
        ErrorDecoder decoder = config.errorDecoder();

        // Then
        assertInstanceOf(SuapClientConfig.SuapErrorDecoder.class, decoder);
    }

    @Test
    void shouldConfigureFeignLoggerLevel() {
        // When
        Logger.Level level = config.feignLoggerLevel();

        // Then
        assertThat(level).isEqualTo(Logger.Level.BASIC);
    }

    @Test
    void shouldMapUnauthorizedErrorToExternalServiceException() {
        // Given
        SuapClientConfig.SuapErrorDecoder decoder = new SuapClientConfig.SuapErrorDecoder();
        Response response = createMockResponse(401, "Unauthorized");

        // When
        Exception exception = decoder.decode("SuapAuthClient#obtainToken(SuapLoginRequest)", response);

        // Then
        assertInstanceOf(ExternalServiceException.class, exception);
        ExternalServiceException externalException = (ExternalServiceException) exception;
        assertThat(externalException.getMessage()).contains("Credenciais inválidas");
    }

    @Test
    void shouldMapNotFoundErrorToExternalServiceException() {
        // Given
        SuapClientConfig.SuapErrorDecoder decoder = new SuapClientConfig.SuapErrorDecoder();
        Response response = createMockResponse(404, "Not Found");

        // When
        Exception exception = decoder.decode("SuapDataClient#getServidores(String,String)", response);

        // Then
        assertInstanceOf(ExternalServiceException.class, exception);
        ExternalServiceException externalException = (ExternalServiceException) exception;
        assertThat(externalException.getMessage()).contains("Recurso não encontrado");
    }

    @Test
    void shouldMapInternalServerErrorToExternalServiceException() {
        // Given
        SuapClientConfig.SuapErrorDecoder decoder = new SuapClientConfig.SuapErrorDecoder();
        Response response = createMockResponse(500, "Internal Server Error");

        // When
        Exception exception = decoder.decode("SuapDataClient#getAlunos(String,String)", response);

        // Then
        assertInstanceOf(ExternalServiceException.class, exception);
        ExternalServiceException externalException = (ExternalServiceException) exception;
        assertThat(externalException.getMessage()).contains("Erro interno do servidor SUAP");
    }

    @Test
    void shouldMapServiceUnavailableToExternalServiceException() {
        // Given
        SuapClientConfig.SuapErrorDecoder decoder = new SuapClientConfig.SuapErrorDecoder();
        Response response = createMockResponse(503, "Service Unavailable");

        // When
        Exception exception = decoder.decode("SuapAuthClient#refreshToken(SuapRefreshRequest)", response);

        // Then
        assertInstanceOf(ExternalServiceException.class, exception);
        ExternalServiceException externalException = (ExternalServiceException) exception;
        assertThat(externalException.getMessage()).contains("temporariamente indisponível");
    }

    @Test
    void shouldMapBadGatewayToExternalServiceException() {
        // Given
        SuapClientConfig.SuapErrorDecoder decoder = new SuapClientConfig.SuapErrorDecoder();
        Response response = createMockResponse(502, "Bad Gateway");

        // When
        Exception exception = decoder.decode("SuapDataClient#getServidores(String,String)", response);

        // Then
        assertInstanceOf(ExternalServiceException.class, exception);
        ExternalServiceException externalException = (ExternalServiceException) exception;
        assertThat(externalException.getMessage()).contains("SUAP indisponível");
    }

    @Test
    void shouldMapGatewayTimeoutToExternalServiceException() {
        // Given
        SuapClientConfig.SuapErrorDecoder decoder = new SuapClientConfig.SuapErrorDecoder();
        Response response = createMockResponse(504, "Gateway Timeout");

        // When
        Exception exception = decoder.decode("SuapAuthClient#obtainToken(SuapLoginRequest)", response);

        // Then
        assertInstanceOf(ExternalServiceException.class, exception);
        ExternalServiceException externalException = (ExternalServiceException) exception;
        assertThat(externalException.getMessage()).contains("Timeout ao comunicar com serviço externo");
    }

    /**
     * Cria uma resposta mock para testes do ErrorDecoder.
     */
    private Response createMockResponse(int status, String reason) {
        return Response.builder()
            .status(status)
            .reason(reason)
            .request(Request.create(Request.HttpMethod.GET, "https://suap.ifpb.edu.br/api/test", 
                Map.of(), null, StandardCharsets.UTF_8, null))
            .headers(Map.of())
            .build();
    }
}