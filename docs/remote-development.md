# Ambiente remoto de desenvolvimento

Este documento descreve o ambiente `development` recomendado no Railway. Ele mantém PostgreSQL e RabbitMQ privados e publica somente a API e o Keycloak.

## Topologia

```text
Internet
  |-- HTTPS --> App
  `-- HTTPS --> Keycloak

Rede privada Railway
  App ------> PostgreSQL
   |--------> RabbitMQ
   `--------> Keycloak (JWK Set)

  Keycloak -> PostgreSQL (schema keycloak)
```

Use uma única réplica da aplicação. Os jobs Quartz usam armazenamento em memória e seriam executados por todas as réplicas.

## 1. Publicação da aplicação

Depois de um push aprovado na `main`, o GitHub Actions publica duas tags no GitHub Container Registry:

- `ghcr.io/lauradefreitas2/policy-billing-engine:development`
- `ghcr.io/lauradefreitas2/policy-billing-engine:sha-<commit>`

A tag por SHA é imutável e deve ser usada para rollback. A tag `development` acompanha o último build aprovado da `main`.

No Railway Hobby, conecte o serviço App diretamente ao repositório GitHub e habilite a espera pelos checks da CI. Nesse modo, o Railway recompila o mesmo commit aprovado. Deploy direto da imagem privada do GHCR exige um plano que aceite credenciais de registry.

## 2. Projeto Railway

Crie um projeto `policy-billing-engine` e um ambiente `development` na região `US East`. Adicione estes serviços:

1. `Postgres`: banco PostgreSQL do catálogo Railway.
2. `RabbitMQ`: imagem `rabbitmq:4.3.6-management`.
3. `Keycloak`: este repositório GitHub, usando `docker/keycloak/Dockerfile.remote`.
4. `App`: este repositório GitHub, usando o `Dockerfile` da raiz.

Anexe um volume ao RabbitMQ em `/var/lib/rabbitmq`. Não habilite acesso público para PostgreSQL nem para a porta AMQP do RabbitMQ.

Antes de iniciar o Keycloak, crie o schema dedicado no Postgres:

```sql
CREATE SCHEMA IF NOT EXISTS keycloak;
```

## 3. Domínios

Gere um domínio Railway para `App` na porta `8080` e outro para `Keycloak` na porta `8080`. Nos exemplos abaixo, substitua:

- `<APP_PUBLIC_URL>` pela URL HTTPS da aplicação, sem barra final.
- `<KEYCLOAK_PUBLIC_URL>` pela URL HTTPS do Keycloak, sem barra final.

## 4. Variáveis do RabbitMQ

Gere valores fortes e armazene-os somente nas variáveis do Railway:

```dotenv
RABBITMQ_DEFAULT_USER=policy_app
RABBITMQ_DEFAULT_PASS=<secret>
```

## 5. Variáveis do Keycloak

```dotenv
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
KC_DB_SCHEMA=keycloak
KC_DB_USERNAME=${{Postgres.PGUSER}}
KC_DB_PASSWORD=${{Postgres.PGPASSWORD}}
KC_HTTP_ENABLED=true
KC_PROXY_HEADERS=xforwarded
KC_HOSTNAME=<KEYCLOAK_PUBLIC_URL>
KC_BOOTSTRAP_ADMIN_USERNAME=<admin-user>
KC_BOOTSTRAP_ADMIN_PASSWORD=<secret>
APP_PUBLIC_URL=<APP_PUBLIC_URL>
DEV_TEST_USERNAME=<test-user>
DEV_TEST_EMAIL=<test-email>
DEV_TEST_USER_PASSWORD=<secret>
JAVA_OPTS_KC_HEAP=-Xms128m -Xmx512m
```

O realm é importado somente quando ainda não existe no banco. Alterar essas variáveis depois da primeira inicialização não atualiza automaticamente usuários ou clients existentes.

## 6. Variáveis da aplicação

```dotenv
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}
SPRING_RABBITMQ_HOST=${{RabbitMQ.RAILWAY_PRIVATE_DOMAIN}}
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=${{RabbitMQ.RABBITMQ_DEFAULT_USER}}
SPRING_RABBITMQ_PASSWORD=${{RabbitMQ.RABBITMQ_DEFAULT_PASS}}
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=<KEYCLOAK_PUBLIC_URL>/realms/policy-realm
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=http://${{Keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080/realms/policy-realm/protocol/openid-connect/certs
APP_OPENAPI_SERVER_URL=<APP_PUBLIC_URL>
APP_OPENAPI_OAUTH_ISSUER_URI=<KEYCLOAK_PUBLIC_URL>/realms/policy-realm
JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75.0
```

O outbox usa lotes de 50 eventos, cinco tentativas e retry exponencial iniciado em 10 segundos. Ajuste apenas se a carga exigir:

```dotenv
APP_OUTBOX_BATCH_SIZE=50
APP_OUTBOX_MAX_ATTEMPTS=5
APP_OUTBOX_RETRY_DELAY=10s
```

Configure o health check do serviço App como `/actuator/health`, com timeout de pelo menos 120 segundos, e a política de reinício como `ON_FAILURE`. Mantenha apenas uma réplica.

## 7. Validação

Depois do deploy, valide nesta ordem:

1. `<KEYCLOAK_PUBLIC_URL>/realms/policy-realm/.well-known/openid-configuration` retorna o documento OpenID Connect.
2. `<APP_PUBLIC_URL>/actuator/health` retorna `UP` sem detalhes internos.
3. `<APP_PUBLIC_URL>/swagger-ui.html` abre e autentica pelo Keycloak.
4. `POST /api/v1/policies` persiste uma apólice no PostgreSQL.
5. RabbitMQ recebe o evento depois do fluxo de cancelamento e o registro correspondente fica `PUBLISHED` em `outbox_events`.
6. `/actuator/info` sem token retorna `401`.

## Releases

O ambiente `development` acompanha a `main`; GitHub Releases não devem disparar esse deploy.

Quando `1.3.0-SNAPSHOT` estiver pronto para estabilização, remova o sufixo `SNAPSHOT`, crie a tag `v1.3.0` e publique a primeira GitHub Release. Uma futura produção deve aceitar somente imagens versionadas e exigir aprovação por GitHub Environment.
