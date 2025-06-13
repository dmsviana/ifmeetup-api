package br.edu.ifpb.ifmeetup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Value("${application.url:http://localhost:8080}")
    private String applicationUrl;

    public String getApplicationUrl() {
        return applicationUrl;
    }
} 