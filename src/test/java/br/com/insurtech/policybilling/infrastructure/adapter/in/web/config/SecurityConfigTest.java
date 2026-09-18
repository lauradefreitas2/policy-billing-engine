package br.com.insurtech.policybilling.infrastructure.adapter.in.web.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.quartz.auto-startup=false")
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("should allow access to OpenAPI docs without authentication")
    void shouldAllowAccessToOpenApiDocsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should expose Keycloak OAuth2 security scheme in OpenAPI docs")
    void shouldExposeKeycloakOAuth2SecuritySchemeInOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.components.securitySchemes.keycloakOAuth2.type").value("oauth2"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.components.securitySchemes.keycloakOAuth2.flows.authorizationCode.authorizationUrl").value("http://localhost:8081/realms/policy-realm/protocol/openid-connect/auth"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.components.securitySchemes.keycloakOAuth2.flows.authorizationCode.tokenUrl").value("http://localhost:8081/realms/policy-realm/protocol/openid-connect/token"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.servers[0].url").value("http://localhost:8080"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.servers[0].description").value("Ambiente de testes"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.security[0].keycloakOAuth2[0]").value("openid"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.security[0].keycloakOAuth2[1]").value("profile"));
    }

    @Test
    @DisplayName("should allow access to actuator health without authentication")
    void shouldAllowAccessToActuatorHealthWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should protect non-health actuator endpoints")
    void shouldProtectNonHealthActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should reject API requests without JWT")
    void shouldRejectApiRequestsWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should allow authenticated API requests to reach controller validation")
    void shouldAllowAuthenticatedApiRequestsToReachControllerValidation() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
