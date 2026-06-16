# Policy Billing Engine 🚀

O **Policy Billing Engine** é o motor transacional e de faturamento automático de apólices de uma **Insurtech** focada em seguros para dispositivos móveis.

O sistema foi projetado com base nos princípios de **Arquitetura Hexagonal (Ports & Adapters)** e **Domain-Driven Design (DDD)**, garantindo uma aplicação desacoplada de frameworks, altamente testável, manutenível e extensível.

---

## 🌟 Diferenciais de Negócio e Funcionalidades

O motor foi construído para suportar um modelo moderno de seguros por assinatura:

1. **Cobrança Inteligente por Assinatura**

   * Em vez de comprometer o limite total do cartão de crédito com o valor anual do seguro, o sistema realiza cobranças recorrentes mensais (`monthlyPremium`) em um dia fixo de vencimento (`dueDay`).

2. **Cobertura Premium (`NEW_DEVICE_REPLACEMENT`)**

   * Em caso de sinistro, a indenização garante a substituição por um dispositivo novo, eliminando a necessidade de aparelhos recondicionados ou processos de manutenção prolongados.

3. **Ciclo de Vida Automatizado**

   * Gerenciamento automático dos estados da apólice (`ACTIVE`, `PENDING_PAYMENT`, `SUSPENDED`, `CANCELLED`) através de processamento agendado.

---

## 🏗️ Arquitetura do Sistema

A aplicação adota a **Arquitetura Hexagonal**, isolando as regras de negócio dos detalhes de infraestrutura.

### Camadas

### `domain` (Core)

Contém:

* Modelos ricos de domínio (`Policy`, `MobileDevice`)
* Enums (`PolicyStatus`, `CoverageType`)
* Regras de negócio
* Validações de domínio
* Fábricas expressivas (`Policy.issue(...)`)

A camada possui **zero dependências de frameworks**.

### `application` (Ports & Use Cases)

#### Inbound Ports

Interfaces que definem os casos de uso da aplicação:

* `CreatePolicyUseCase`
* `ProcessDailyBillingUseCase`

#### Outbound Ports

Interfaces que representam dependências externas:

* `PolicyRepositoryPort`

#### Use Cases

Implementações da lógica de aplicação:

* `CreatePolicyUseCaseImpl`
* `CreatePolicyCommand`

### `infrastructure` (Adapters & Config)

#### Inbound Adapters

* API REST (`PolicyController`, `PolicyApi`)
* Agendamentos com Quartz (`BillingJob`)

#### Outbound Adapters

* Persistência JPA (`PolicyPersistenceAdapter`)
* Mapeamento entre domínio e banco (`PolicyMapper`)

#### Configurações

* `SecurityConfig`
* `OpenApiConfig`
* `UseCaseConfig`

---

## 🛠️ Tecnologias Utilizadas

* Java 17 / 21+
* Spring Boot 3.4+
* Spring Web
* Spring Data JPA
* Spring Security
* PostgreSQL 16
* Docker & Docker Compose
* Springdoc OpenAPI
* Quartz Scheduler
* Jakarta Validation
* JUnit 5
* Mockito

---

## 📁 Estrutura de Pastas

```text
src/main/java/br/com/insurtech/policybilling/
├── PolicyBillingEngineApplication.java
│
├── domain/
│   ├── exception/
│   └── model/
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── usecase/
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── web/
    │   │   └── scheduler/
    │   └── out/
    │       └── persistence/
    └── config/
```

---

## 🔒 Tratamento de Erros (RFC 7807)

A API implementa o padrão **RFC 7807 (Problem Details for HTTP APIs)** utilizando a classe `ProblemDetail` do Spring Boot 3.

Erros de validação e regras de negócio retornam respostas padronizadas e previsíveis.

### Exemplo

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/policies",
  "invalid_params": [
    {
      "field": "deviceImei",
      "message": "deve ter exatamente 15 dígitos"
    }
  ]
}
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos

* Docker Desktop
* Java JDK 17+

### 1. Subir o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d
```

O comando irá:

* Baixar a imagem do PostgreSQL
* Criar o banco `policy_db`
* Configurar usuário e senha
* Criar volume persistente

### 2. Executar a Aplicação

```bash
./mvnw spring-boot:run
```

O Hibernate criará automaticamente as tabelas necessárias através do `ddl-auto=update`.

### 3. Acessar o Swagger

```
http://localhost:8080/swagger-ui.html
```

Utilize o botão **Try it out** para testar os endpoints.

---

## 🧪 Executando os Testes

A suíte de testes cobre diferentes níveis da aplicação.

### Testes Unitários

Validam:

* Regras de domínio
* Casos de uso
* Invariantes de negócio

### Testes Web

Utilizam `@WebMvcTest` para validar:

* Contratos HTTP
* Validações Jakarta
* Respostas RFC 7807

### Testes de Arquitetura

Garantem que:

* O domínio não dependa do Spring
* O domínio não dependa do Jakarta
* O domínio permaneça isolado da infraestrutura

### Executar todos os testes

```bash
./mvnw test
```

---

## 🛠️ Próximos Passos

* [ ] Automação completa do faturamento com Quartz
* [ ] Integração com gateway de pagamento
* [ ] Notificações de cobrança
* [ ] Métricas e observabilidade
* [ ] Cobertura de testes ampliada
* [ ] Integração via mensageria

```
```
