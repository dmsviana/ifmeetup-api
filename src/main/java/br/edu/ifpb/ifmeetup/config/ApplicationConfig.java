package br.edu.ifpb.ifmeetup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Value("${application.backend.url:http://localhost:8080}")
    private String applicationUrl;

    @Value("${application.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }
} 