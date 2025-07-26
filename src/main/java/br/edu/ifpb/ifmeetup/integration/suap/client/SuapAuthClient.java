package br.edu.ifpb.ifmeetup.integration.suap.client;

import br.edu.ifpb.ifmeetup.integration.suap.config.SuapClientConfig;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapLoginRequest;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapRefreshRequest;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente OpenFeign para autenticação com a API do SUAP.
 * 
 * Responsável por:
 * - Autenticação no SUAP via credenciais (matrícula/senha)
 * - Renovação de tokens de acesso
 * - Tratamento de erros de autenticação
 */
@FeignClient(
    name = "suap-auth-client", 
    url = "${suap.api.base-url}",
    configuration = SuapClientConfig.class
)
public interface SuapAuthClient {
    
    /**
     * Obtém token de acesso através de credenciais SUAP.
     * 
     * @param request Credenciais de login (matrícula e senha)
     * @return Resposta contendo tokens de acesso e refresh
     */
    @PostMapping("/jwt/obtain_token/")
    SuapTokenResponse obtainToken(@RequestBody SuapLoginRequest request);
    
    /**
     * Renova token de acesso usando refresh token.
     * 
     * @param request Requisição contendo refresh token
     * @return Nova resposta com tokens atualizados
     */
    @PostMapping("/jwt/refresh_token/")
    SuapTokenResponse refreshToken(@RequestBody SuapRefreshRequest request);
}