package br.edu.ifpb.ifmeetup.integration.suap.dto;

import jakarta.validation.constraints.NotBlank;

public record SuapRefreshRequest(
    
    @NotBlank(message = "Refresh token é obrigatório")
    String refresh
) {
    
    public SuapRefreshRequest {
        if (refresh != null) {
            refresh = refresh.trim();
        }
    }
}