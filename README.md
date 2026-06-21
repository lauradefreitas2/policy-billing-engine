# Policy Billing Engine

O **Policy Billing Engine** é um serviço backend para uma Insurtech focada em seguros de dispositivos móveis. Ele gerencia emissão de apólices, regras de faturamento mensal recorrente, transições automáticas de cobrança, cancelamento por inadimplência, persistência e observabilidade operacional.

O projeto é construído com **Java 21** e **Spring Boot**, seguindo **Arquitetura Hexagonal (Ports & Adapters)**. O domínio permanece independente de Spring, JPA, Quartz, APIs web e ferramentas de observabilidade.

Versão atual do projeto: **1.1.0-SNAPSHOT**.

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
  - `CANCELED`
- Toda apólice nasce ativa com cobertura de reposição por aparelho novo.
- Apólices ativas podem ser marcadas como pendentes de pagamento.
- Apólices pendentes de pagamento podem ser canceladas por inadimplência.
- O cancelamento comum é idempotente para apólices já canceladas.

### Persistência

- Adaptador de persistência com Spring Data JPA.
- Versionamento de schema com Flyway.
- Migração inicial `V1__create_policies_table.sql` para criação da tabela `policies`.
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

### Automação de Cancelamento por Inadimplência

- `CancellationJob` agendado com Quartz.
- Agendamento local: a cada 45 segundos.
- Referência de cron para produção: `0 15 0 * * ?`.
- Apólices em `PENDING_PAYMENT` são avaliadas em Java.
- Apólices com atraso de 10 dias ou mais são canceladas.
- O cálculo de atraso com virada de mês está coberto por testes.

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
- `CancelOverduePoliciesUseCase`
- `PolicyRepositoryPort`

### Infrastructure

Contém os adaptadores e configurações técnicas:

- Controller REST.
- Tratamento global de exceções.
- Adaptador de persistência JPA.
- Jobs do Quartz.
- Configurações Spring.
- Decorator de observabilidade para métricas de criação de apólices.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Runtime e versão da linguagem |
| Spring Boot 3.5.x | Framework da aplicação |
| Spring Web | API REST |
| Spring Data JPA | Persistência |
| Flyway | Versionamento de schema do banco |
| PostgreSQL | Banco local/runtime |
| H2 | Banco em memória para testes |
| Quartz Scheduler | Jobs automatizados |
| Springdoc OpenAPI | Documentação da API |
| Spring Boot Actuator | Endpoints de health, info e métricas |
| Micrometer Prometheus | Exportação de métricas para Prometheus |
| JUnit 5 | Testes automatizados |
| Mockito | Test doubles |
| Docker Compose | PostgreSQL local |

## Como Executar Localmente

Subir o PostgreSQL:

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

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

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
- Contratos do controller web.
- Adaptador de persistência JPA.
- Execução das migrations Flyway em banco H2 durante os testes.
- Verificação de fronteira arquitetural.
- Decorator de observabilidade.

## Roadmap

- Integração com gateway de pagamento real para cobranças recorrentes.
- Publicação de eventos RabbitMQ para tentativas de cobrança e resultados de pagamento.
- Configuração OAuth2 Resource Server com validação JWT e RBAC.
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
- Versionamento de banco com Flyway.
- Testes em múltiplas camadas.
- Observabilidade orientada a produção.
