package br.edu.ifpb.ifmeetup.integration.suap.dto;

import java.util.List;

public record SuapAlunosResponse(
    List<SuapAlunoResponse> results
) {}