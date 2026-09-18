package br.com.insurtech.policybilling.infrastructure.adapter.in.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "keycloakOAuth2";

    private static final String OPENID_SCOPE = "openid";
    private static final String PROFILE_SCOPE = "profile";

    private final OpenApiProperties properties;

    public OpenApiConfig(OpenApiProperties properties) {
        this.properties = properties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, keycloakOAuth2SecurityScheme()))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME, List.of(OPENID_SCOPE, PROFILE_SCOPE)))
                .servers(List.of(new Server()
                        .url(properties.serverUrl())
                        .description(properties.serverDescription())))
                .tags(List.of(new Tag()
                        .name("Apólices")
                        .description("Emissão e acompanhamento de apólices de seguro mobile.")))
                .info(new Info()
                        .title("Policy Billing Engine API")
                        .version("v1")
                        .contact(new Contact()
                                .name("Policy Billing Engine"))
                        .description("""
                                API para emissão, faturamento recorrente e cancelamento por inadimplência de apólices
                                de seguro mobile.

                                Os exemplos nesta documentação mostram payloads válidos e respostas de erro esperadas
                                para facilitar testes manuais pela interface do Swagger.
                                """));
    }

    private SecurityScheme keycloakOAuth2SecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Autenticação OAuth2 via Keycloak. Use o botão Authorize do Swagger.")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(properties.oauthIssuerUri() + "/protocol/openid-connect/auth")
                                .tokenUrl(properties.oauthIssuerUri() + "/protocol/openid-connect/token")
                                .scopes(new Scopes()
                                        .addString(OPENID_SCOPE, "Identificação OpenID Connect")
                                        .addString(PROFILE_SCOPE, "Dados básicos do usuário autenticado"))));
    }
}
