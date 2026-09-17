package br.com.insurtech.policybilling.infrastructure.adapter.in.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Ambiente local")))
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
}
