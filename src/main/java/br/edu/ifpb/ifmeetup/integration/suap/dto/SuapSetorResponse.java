package br.edu.ifpb.ifmeetup.integration.suap.dto;

public record SuapSetorResponse(
    String uuid,
    String sigla,
    String nome
) {}