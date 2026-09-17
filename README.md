# Policy Billing Engine

O **Policy Billing Engine** é um serviço backend para uma Insurtech focada em seguros de dispositivos móveis. Ele gerencia emissão de apólices, regras de faturamento mensal recorrente, transições automáticas de cobrança, cancelamento por inadimplência, persistência e observabilidade operacional.

O projeto é construído com **Java 21** e **Spring Boot**, seguindo **Arquitetura Hexagonal (Ports & Adapters)**. O domínio permanece independente de Spring, JPA, Quartz, APIs web e ferramentas de observabilidade.

Versão atual do projeto: **1.3.0-SNAPSHOT**.

## Funcionalidades Implementadas

### Emissão de Apólices

- Endpoint REST para criação de apólices de seguro mobile.
- Validação de entrada com Jakarta Validation.
- Respostas de erro padronizadas com `ProblemDetail`, seguindo o estilo RFC 7807.
- Documentação da API via Swagger/OpenAPI com Springdoc.

### Regras de Domínio

- Modelo de domínio rico com `Policy`.
- `MobileDevice` modelado como record com validações.
- Cobertura suportada: `NEW_DEVICE_REPLACEMENT`.
- Status suportados para apólice:
  - `ACTIVE`
  - `PENDING_PAYMENT`
  - `SUSPENDED`
  - `CANCELED`
- Toda apólice nasce ativa com cobertura de reposição por aparelho novo.
- Apólices ativas podem ser marcadas como pendentes de pagamento.
- Apólices ativas ou pendentes de pagamento podem ser suspensas por inadimplência a partir de 1 dia de atraso.
- Apólices suspensas por 10 dias ou mais podem ser canceladas por inadimplência.
- O cancelamento comum é idempotente para apólices já canceladas.

### Persistência

- Adaptador de persistência com Spring Data JPA.
- Versionamento de schema com Flyway.
- Migração inicial `V1__create_policies_table.sql` para criação da tabela `policies`.
- Migração `V2__add_suspended_at_column.sql` para registrar `suspended_at`.
- Hibernate configurado com `ddl-auto=validate`; a aplicação valida o schema, mas não cria nem altera tabelas automaticamente.
- Driver PostgreSQL configurado para execução local/runtime.
- H2 configurado para testes de integração.
- `PolicyEntity` isolada na camada de infraestrutura.
- Mapper dedicado entre `Policy` do domínio e `PolicyEntity` da infraestrutura.
- Docker Compose com PostgreSQL para desenvolvimento local.
- Constraint de banco para garantir `due_day` entre 1 e 28.

### Automação de Faturamento

- `BillingJob` agendado com Quartz.
- Agendamento local: a cada 30 segundos.
- Referência de cron para produção: `0 0 0 * * ?`.
- O caso de uso diário busca apólices ativas vencendo no dia atual.
- Apólices devidas são marcadas como `PENDING_PAYMENT` e persistidas.

### Automação de Suspensão por Inadimplência

- `SuspensionJob` agendado com Quartz.
- Agendamento local: a cada 40 segundos.
- Referência de cron para produção: `0 5 0 * * ?`.
- O caso de uso busca apólices `ACTIVE` e `PENDING_PAYMENT`.
- Apólices com pelo menos 1 dia de atraso são marcadas como `SUSPENDED`.
- A data/hora da suspensão é registrada em `suspended_at`.

### Automação de Cancelamento por Inadimplência

- `CancellationJob` agendado com Quartz.
- Agendamento local: a cada 45 segundos.
- Referência de cron para produção: `0 15 0 * * ?`.
- Apólices em `SUSPENDED` são avaliadas em Java.
- Apólices suspensas por 10 dias ou mais são canceladas.
- Ao cancelar uma apólice por inadimplência, a aplicação publica um evento `PolicyCanceledEvent`.
- O cálculo de atraso e de tempo em suspensão está coberto por testes.

### Eventos e Mensageria

- Publicação de eventos via RabbitMQ usando Spring AMQP.
- Exchange configurada: `policy.events.exchange`.
- Fila configurada: `policy.canceled.queue`.
- Routing key configurada: `policy.canceled.key`.
- Mensagens trafegam em JSON com `Jackson2JsonMessageConverter`.
- O domínio e os casos de uso dependem apenas da porta `PolicyEventPublisherPort`; o adapter RabbitMQ fica isolado na infraestrutura.

### Segurança

- API protegida como OAuth2 Resource Server com validação de JWT.
- Authorization Server local com Keycloak via Docker Compose.
- Swagger UI integrado ao Keycloak com fluxo OAuth2 Authorization Code + PKCE.
- Swagger/OpenAPI liberado para facilitar testes locais:
  - `/v3/api-docs/**`
  - `/swagger-ui/**`
  - `/swagger-ui.html`
- Actuator liberado para health checks e observabilidade local:
  - `/actuator/**`
- Rotas da API, como `/api/v1/**`, exigem autenticação Bearer JWT.
- Issuer URI configurado para o realm local `policy-realm`.

### Observabilidade

- Spring Boot Actuator habilitado.
- Exportação de métricas Prometheus via Micrometer.
- Endpoints expostos:
  - `/actuator/health`
  - `/actuator/info`
  - `/actuator/metrics`
  - `/actuator/prometheus`
- Detalhes do health habilitados para facilitar debug local.
- Métrica customizada para criação bem-sucedida de apólices:
  - Nome no Actuator: `policies.created`
  - Nome no Prometheus: `policies_created_total`

## Arquitetura

```text
src/main/java/br/com/insurtech/policybilling/
├── domain
│   ├── exception
│   └── model
├── application
│   ├── port
│   │   ├── in
│   │   └── out
│   └── usecase
└── infrastructure
    ├── adapter
    │   ├── in
    │   │   ├── scheduler
    │   │   └── web
    │   └── out
    │       ├── messaging
    │       └── persistence
    ├── config
    └── observability
```

### Domain

Contém as regras de negócio e os modelos em Java puro. Não depende de Spring, JPA, Quartz, RabbitMQ, Security ou Micrometer.

### Application

Contém os casos de uso e as portas da aplicação:

- `CreatePolicyUseCase`
- `ProcessDailyBillingUseCase`
- `SuspendOverduePoliciesUseCase`
- `CancelOverduePoliciesUseCase`
- `PolicyRepositoryPort`
- `PolicyEventPublisherPort`

### Infrastructure

Contém os adaptadores e configurações técnicas:

- Controller REST.
- Tratamento global de exceções.
- Adaptador de persistência JPA.
- Adaptador RabbitMQ para publicação de eventos.
- Jobs do Quartz.
- Configurações Spring.
- Decorator de observabilidade para métricas de criação de apólices.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Runtime e versão da linguagem |
| Spring Boot 3.5.x | Framework da aplicação |
| Spring Web | API REST |
| Spring Security | Proteção das rotas HTTP |
| OAuth2 Resource Server | Validação de JWT |
| Spring Data JPA | Persistência |
| Spring AMQP | Publicação de eventos no RabbitMQ |
| Flyway | Versionamento de schema do banco |
| PostgreSQL | Banco local/runtime |
| RabbitMQ | Broker de mensagens para eventos |
| Keycloak | Authorization Server local para emissão de JWT |
| H2 | Banco em memória para testes |
| Quartz Scheduler | Jobs automatizados |
| Springdoc OpenAPI | Documentação da API |
| Spring Boot Actuator | Endpoints de health, info e métricas |
| Micrometer Prometheus | Exportação de métricas para Prometheus |
| JUnit 5 | Testes automatizados |
| Mockito | Test doubles |
| Docker Compose | PostgreSQL, RabbitMQ e Keycloak locais |

## Como Executar Localmente

Subir PostgreSQL, RabbitMQ e Keycloak:

```bash
docker compose up -d
```

Se o banco local já tiver tabelas criadas por versões antigas com Hibernate `ddl-auto=update`, limpe o volume antes de subir novamente:

```bash
docker compose down -v
docker compose up -d
```

Executar a aplicação:

```bash
./mvnw spring-boot:run
```

Na inicialização, o Flyway aplica as migrations em `src/main/resources/db/migration` antes do Hibernate validar o schema.

Painel visual do RabbitMQ:

```text
http://localhost:15672
```

Credenciais locais:

```text
guest / guest
```

Painel administrativo do Keycloak:

```text
http://localhost:8081
```

Credenciais locais:

```text
admin / admin
```

Realm importado automaticamente:

```text
policy-realm
```

Usuário de teste:

```text
teste / 123
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

### Como Testar a API

1. Suba a infraestrutura local:

```bash
docker compose up -d
```

2. Execute a aplicação:

```bash
./mvnw spring-boot:run
```

3. Abra o Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

4. Clique em `Authorize` no Swagger, faça login no Keycloak com `teste / 123` e autorize o client `policy-engine-swagger`.

5. Use o endpoint `POST /api/v1/policies` com o exemplo pronto do Swagger. O Swagger enviará o header `Authorization: Bearer <token>` automaticamente.

6. Para criar uma apólice válida, informe:

- `deviceImei` com exatamente 15 dígitos.
- `dueDay` entre 1 e 28.
- `deviceInvoiceValue` maior que zero.
- `monthlyPremium` maior que zero.

7. Confira os exemplos de erro `400`, `401` e `422` no Swagger para entender autenticação, validações de entrada e regras de domínio.

Observação: os jobs Quartz processam faturamento e cancelamento automaticamente em intervalos curtos no ambiente local. Quando uma apólice é cancelada por inadimplência, um evento `PolicyCanceledEvent` é publicado no RabbitMQ.

Health da aplicação:

```text
http://localhost:8080/actuator/health
```

Informações da aplicação:

```text
http://localhost:8080/actuator/info
```

Métricas Prometheus:

```text
http://localhost:8080/actuator/prometheus
```

Métrica customizada de criação de apólices:

```text
http://localhost:8080/actuator/metrics/policies.created
```

## Testes

Executar todos os testes:

```bash
./mvnw test
```

A cobertura atual inclui:

- Regras de domínio.
- Casos de uso da aplicação.
- Automação de faturamento.
- Automação de cancelamento por inadimplência.
- Publicação de evento quando uma apólice é cancelada por inadimplência.
- Regras de segurança HTTP.
- Contratos do controller web.
- Adaptador de persistência JPA.
- Execução das migrations Flyway em banco H2 durante os testes.
- Verificação de fronteira arquitetural.
- Decorator de observabilidade.

## Roadmap

- Integração com gateway de pagamento real para cobranças recorrentes.
- Publicação de eventos para tentativas de cobrança e resultados de pagamento.
- RBAC baseado em roles/scopes do JWT.
- Política de retry para falhas de pagamento.
- Fluxo de suspensão de apólice antes do cancelamento definitivo.
- Novas migrations Flyway conforme o modelo de dados evoluir.
- Perfis de produção para Quartz usando cron em vez dos intervalos curtos locais.
- Dashboards e alertas com Prometheus/Grafana.
- Testes de carga e resiliência.

## Objetivo do Projeto

Este projeto demonstra práticas de engenharia backend aplicadas a um domínio realista de faturamento de seguros:

- Arquitetura Hexagonal.
- Domain-Driven Design.
- Orquestração clara de casos de uso.
- Regras de domínio ricas.
- Jobs automatizados.
- Adaptador real de persistência.
- Publicação de eventos com RabbitMQ via porta de saída.
- Proteção HTTP com OAuth2 Resource Server e JWT.
- Versionamento de banco com Flyway.
- Testes em múltiplas camadas.
- Observabilidade orientada a produção.
