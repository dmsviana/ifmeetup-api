package br.edu.ifpb.ifmeetup.integration.suap.client;

import br.edu.ifpb.ifmeetup.integration.suap.config.SuapClientConfig;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapAlunosResponse;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapServidoresResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente OpenFeign para busca de dados na API do SUAP.
 * 
 * Responsável por:
 * - Busca de dados de servidores através da API de recursos humanos
 * - Busca de dados de alunos através da API de ensino
 * - Tratamento de erros de API e autorização
 */
@FeignClient(
    name = "suap-data-client", 
    url = "${suap.api.base-url}",
    configuration = SuapClientConfig.class
)
public interface SuapDataClient {
    
    /**
     * Busca dados de servidores na API do SUAP.
     * 
     * @param token Token de autorização no formato "Bearer {token}"
     * @param matricula Matrícula do servidor para busca
     * @return Resposta contendo lista de servidores encontrados
     */
    @GetMapping("/recursos-humanos/servidores/v1")
    SuapServidoresResponse getServidores(
        @RequestHeader("Authorization") String token, 
        @RequestParam("search") String matricula
    );
    
    /**
     * Busca dados de alunos na API do SUAP.
     * 
     * @param token Token de autorização no formato "Bearer {token}"
     * @param matricula Matrícula do aluno para busca
     * @return Resposta contendo lista de alunos encontrados
     */
    @GetMapping("/ensino/alunos/v1")
    SuapAlunosResponse getAlunos(
        @RequestHeader("Authorization") String token,
        @RequestParam("search") String matricula
    );
}