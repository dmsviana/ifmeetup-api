package br.edu.ifpb.ifmeetup.integration.suap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SuapTokenResponse(
    String access,
    String refresh,
    @JsonProperty("access_expires_in") Long accessExpiresIn
) {}