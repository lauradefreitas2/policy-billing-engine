package br.com.insurtech.policybilling.infrastructure.adapter.in.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Policy Billing Engine API")
                        .version("v1")
                        .description("API documentation for the Policy Billing Engine"));
    }
}
