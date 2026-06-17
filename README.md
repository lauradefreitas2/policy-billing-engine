# 🚀 Policy Billing Engine

O **Policy Billing Engine** é o motor transacional e de faturamento automático de apólices de uma **Insurtech** focada em seguros para dispositivos móveis.

O sistema foi projetado utilizando **Arquitetura Hexagonal (Ports & Adapters)** e **Domain-Driven Design (DDD)**, garantindo uma aplicação desacoplada de frameworks, altamente testável, manutenível e extensível.

---

## 🌟 Funcionalidades

### 💳 Cobrança Inteligente por Assinatura

* Processamento automático de cobranças recorrentes mensais (`monthlyPremium`);
* Controle de vencimento através do campo `dueDay`;
* Otimização do fluxo de caixa da operação.

### 📱 Cobertura Premium

Suporte à cobertura **NEW_DEVICE_REPLACEMENT**, permitindo a substituição do dispositivo segurado por um novo em caso de sinistro.

### 🔄 Ciclo de Vida Automatizado

Gerenciamento automático dos estados da apólice:

* `ACTIVE`
* `PENDING_PAYMENT`
* `SUSPENDED`
* `CANCELLED`

As transições são executadas por processos automatizados de alta confiabilidade.

---

## 🏗️ Arquitetura

A aplicação segue o padrão **Hexagonal Architecture**, isolando completamente o domínio das tecnologias externas.

### Camadas

#### Domain (Core)

Responsável pelas regras de negócio:

* Entidades ricas (`Policy`, `MobileDevice`);
* Enums;
* Validações;
* Regras de domínio.

> Não possui dependência de frameworks.

#### Application (Ports & Use Cases)

Responsável pela orquestração dos casos de uso:

* `CreatePolicyUseCase`
* `ProcessDailyBillingUseCase`

Além das portas de entrada e saída da aplicação.

#### Infrastructure (Adapters)

Responsável pelos detalhes técnicos:

* API REST;
* Persistência JPA;
* Quartz Scheduler;
* Configurações da aplicação.

---

## 🛠️ Tecnologias

| Tecnologia        | Versão    |
| ----------------- | --------- |
| Java              | 21        |
| Spring Boot       | 3.4+      |
| PostgreSQL        | 16        |
| Quartz Scheduler  | Latest    |
| Swagger / OpenAPI | Springdoc |
| JUnit 5           | Latest    |
| Mockito           | Latest    |
| Docker            | Latest    |
| Docker Compose    | Latest    |

---

## 📂 Estrutura do Projeto

```text
src/main/java/br/com/insurtech/policybilling/
├── domain/              # Modelos de negócio e regras puras
├── application/         # Casos de uso e portas (In/Out)
└── infrastructure/      # Adaptadores (Web, Scheduler, Persistence) e Config
```

---

## 🔒 Tratamento de Erros

A API implementa o padrão **RFC 7807 (Problem Details for HTTP APIs)**, fornecendo respostas padronizadas para:

* Erros de validação;
* Violações de regras de negócio;
* Recursos não encontrados;
* Requisições inválidas.

---

## 🚀 Como Executar

### 1. Subir o banco de dados

```bash
docker compose up -d
```

### 2. Executar a aplicação

```bash
./mvnw spring-boot:run
```

### 3. Acessar a documentação da API

```text
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Testes

O projeto possui testes em diferentes níveis:

### Testes Unitários

Validam:

* Regras de domínio;
* Entidades;
* Casos de uso;
* Serviços de aplicação.

Utilizando:

* JUnit 5
* Mockito

### Testes Web

Garantem os contratos da API REST.

### Testes de Arquitetura

Verificam o isolamento das camadas e a aderência à Arquitetura Hexagonal.

### Executar todos os testes

```bash
./mvnw test
```

---

## 🛣️ Roadmap

* [ ] Integração com gateway de pagamento real
* [ ] Cancelamento automático por inadimplência
* [ ] Observabilidade com Spring Boot Actuator
* [ ] Testes de carga e performance
* [ ] Integração via mensageria para notificações de cobrança

---

## 📌 Objetivo do Projeto

Este projeto foi desenvolvido com foco em demonstrar boas práticas de desenvolvimento backend utilizando:

* Arquitetura Hexagonal;
* Domain-Driven Design (DDD);
* Clean Architecture;
* Testes automatizados;
* APIs REST robustas;
* Processamento assíncrono e agendado.

A proposta simula um cenário real de uma Insurtech moderna, com regras de negócio voltadas para faturamento recorrente e gestão automatizada de apólices.
