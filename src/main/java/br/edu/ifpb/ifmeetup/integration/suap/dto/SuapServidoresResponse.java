package br.edu.ifpb.ifmeetup.integration.suap.dto;

import java.util.List;

public record SuapServidoresResponse(
    List<SuapServidorResponse> results
) {}