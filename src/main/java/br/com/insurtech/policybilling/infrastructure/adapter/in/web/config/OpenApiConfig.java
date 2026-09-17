package br.com.insurtech.policybilling.infrastructure.adapter.in.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.ExternalDocumentation;
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
                        .description("Ambiente local - use com PostgreSQL e RabbitMQ rodando via Docker Compose")))
                .tags(List.of(new Tag()
                        .name("Apólices")
                        .description("""
                                Endpoints para emissão e evolução do ciclo de vida de apólices mobile.
                                Comece por POST /api/v1/policies para criar uma apólice de teste.
                                """)))
                .info(new Info()
                        .title("Policy Billing Engine API")
                        .version("v1")
                        .contact(new Contact()
                                .name("Policy Billing Engine")
                                .url("http://localhost:8080/swagger-ui.html"))
                        .description("""
                                API para emissão, faturamento recorrente e cancelamento por inadimplência de apólices
                                de seguro mobile.

                                Como testar:
                                1. Suba a infraestrutura local com docker compose up -d.
                                2. Use o endpoint POST /api/v1/policies com o exemplo pronto do Swagger.
                                3. Informe um IMEI com exatamente 15 dígitos.
                                4. Use dueDay entre 1 e 28.
                                5. Confira os exemplos de erro 400 e 422 para entender validações de entrada e regras de domínio.

                                Observação: os jobs Quartz processam faturamento e cancelamento automaticamente em intervalos
                                curtos no ambiente local. Quando uma apólice é cancelada por inadimplência, um evento
                                PolicyCanceledEvent é publicado no RabbitMQ.
                                """))
                .externalDocs(new ExternalDocumentation()
                        .description("RabbitMQ Management - painel local para acompanhar filas e mensagens")
                        .url("http://localhost:15672"));
    }
}
