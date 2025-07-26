package br.edu.ifpb.ifmeetup.integration.suap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SuapLoginRequest(
    
    @NotBlank(message = "Username é obrigatório")
    @Pattern(regexp = "^\\d{7,12}$", message = "Username deve conter entre 7 e 12 dígitos")
    String username,
    
    @NotBlank(message = "Password é obrigatório")
    String password
) {
    
    public SuapLoginRequest {
        if (username != null) {
            username = username.trim();
        }
        if (password != null) {
            password = password.trim();
        }
    }
}