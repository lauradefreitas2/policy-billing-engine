package br.com.insurtech.policybilling.infrastructure.adapter.in.web.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.openapi")
public record OpenApiProperties(
        @NotBlank String serverUrl,
        @NotBlank String serverDescription,
        @NotBlank String oauthIssuerUri
) {
}
