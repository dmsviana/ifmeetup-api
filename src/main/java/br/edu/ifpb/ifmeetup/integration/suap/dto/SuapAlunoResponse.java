package br.edu.ifpb.ifmeetup.integration.suap.dto;

public record SuapAlunoResponse(
    String uuid,
    String nome,
    String matricula,
    SuapCursoResponse curso,
    String situacao
) {}