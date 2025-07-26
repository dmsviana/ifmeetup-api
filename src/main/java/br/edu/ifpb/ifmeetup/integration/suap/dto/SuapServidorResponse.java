package br.edu.ifpb.ifmeetup.integration.suap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SuapServidorResponse(
    String uuid,
    String nome,
    String matricula,
    @JsonProperty("cargo_emprego") String cargoEmprego,
    @JsonProperty("funcao_codigo") Integer funcaoCodigo,
    @JsonProperty("setor_exercicio") SuapSetorResponse setorExercicio,
    SuapSituacaoResponse situacao
) {}