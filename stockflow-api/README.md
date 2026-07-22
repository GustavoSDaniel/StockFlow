# 📦 StockFlow API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![WebFlux](https://img.shields.io/badge/Reactive-WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-26.0-4D4D4D?style=for-the-badge&logo=keycloak&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-4.0-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/DragonflyDB-Cache-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-✓-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Zipkin](https://img.shields.io/badge/Zipkin-Tracing-FF6B00?style=for-the-badge)
![Resilience4j](https://img.shields.io/badge/Resilience4j-CB_RT_TO-81C784?style=for-the-badge)
![License](https://img.shields.io/badge/License-Apache_2.0-D22128?style=for-the-badge)

</div>

---

## 📋 Visão Geral

**StockFlow API** é um backend reativo de alta performance para gerenciamento de estoques, construído sobre **Spring WebFlux** e **R2DBC**. Projetada para operar em ambientes de alta concorrência, a API oferece controle completo sobre o ciclo de vida de produtos, fornecedores, categorias e movimentações de estoque — desde a criação até a geração de relatórios em PDF.

### 🔑 Principais Funcionalidades

- **CRUD completo** de produtos, fornecedores, categorias e usuários
- **Controle de estoque** com entradas, saídas, ajustes e transferências entre almoxarifados
- **Rastreabilidade** total via histórico de movimentações com razão e tipo documentados
- **Dashboards analíticos** com métricas de estoque (ruptura, baixo estoque, excesso)
- **Relatórios em PDF** de produtos e estoque gerados sob demanda
- **Sistema de notificações** para alertas de estoque crítico
- **Autenticação/autorização** baseada em JWT via Keycloak com RBAC (roles: `USER`, `MANAGER`, `ADMIN`)
- **Mensageria assíncrona** via Apache Kafka com pattern Outbox para resiliência de eventos
- **Notificações em tempo real** via SSE (Server-Sent Events) com backpressure buffer
- **Cache distribuído** com DragonflyDB (compatível com Redis)
- **Integração com ViaCEP** para consulta de endereços por CEP com Circuit Breaker
- **Resiliência** com Resilience4j (Circuit Breaker, Retry, Timeout)

---

## 🛠️ Tecnologias Utilizadas

| Categoria | Tecnologia | Versão | Descrição |
|-----------|-----------|--------|-----------|
| **Linguagem** | Java | 21 | LTS com Virtual Threads |
| **Framework** | Spring Boot | 4.0.6 | Configuração autoconfigurada e produtiva |
| **Runtime** | Spring WebFlux | — | Stack reativa não-bloqueante (Netty) |
| **Persistência** | R2DBC + PostgreSQL | 16 | Acesso reativo ao banco com pool de conexões integrado |
| **Migrations** | Flyway | — | Versionamento de schema (`inventory`) |
| **Autenticação** | Keycloak | 26.0 | OAuth2 / OpenID Connect com JWT |
| **Autorização** | Spring Security | — | Resource Server OAuth2 + RBAC |
| **Mensageria** | Apache Kafka | 4.0 | Producer/Consumer reativos com Reactor Kafka |
| **Cache** | DragonflyDB | 1.37 | Cache Redis-compatible de alta performance |
| **Resiliência** | Resilience4j | 2.4 | Circuit Breaker, Retry e Timeout |
| **Notificações** | SSE (Sinks.Many) | — | Push em tempo real com backpressure buffer |
| **PDF** | OpenPDF | 3.0.5 | Geração de relatórios em PDF |
| **Documentação** | SpringDoc OpenAPI | 2.8.10 | Swagger UI interativo |
| **Containerização** | Docker + Compose | — | Orquestração completa do ecossistema |

---

## 📊 Observabilidade (Stack LGTM)

> A API exporta métricas, traces e logs estruturados para uma stack completa de observabilidade — **Prometheus**, **Loki**, **Promtail** e **Zipkin** — todos centralizados e visualizados no **Grafana**.

### 🔭 Diagrama da Stack

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  StockFlow   │────▶│  Prometheus  │────▶│              │
│     API      │     │  :6065       │     │   Grafana    │
│   :6060      │     └──────────────┘     │   :6066      │
│              │     ┌──────────────┐     │              │
│   metrics    │     │    Loki      │────▶│  Dashboards  │
│   traces     │     │  :6067       │     │  + Alerts    │
│   logs       │     └──────┬───────┘     └──────────────┘
└──────┬───────┘            │
       │            ┌──────┴───────┐     ┌──────────────┐
       │            │   Promtail   │     │ AlertManager  │
       └───────────▶│              │     │   :6069       │
         Zipkin     │ coleta logs  │     │  notificações │
         :6068      └──────────────┘     └──────────────┘
```

### 📈 Métricas — Prometheus

Métricas expostas no endpoint `/actuator/prometheus`, coletadas a cada **15 segundos**:

- **Latência HTTP** com histogramas e percentis configurados (`p50`, `p90`, `p95`, `p99`)
- **Contadores de requisições** por status e endpoint
- **Métricas de JVM** (memória heap/non-heap, GC, threads)
- **Pool de conexões R2DBC** (ativo, idle, pendente)
- **Métricas customizadas** de negócio (produtos sem estoque, eventos Kafka enviados)
- **Alertas configurados** no AlertManager:
  - 🚨 `ApiDown` — API indisponível por mais de 1 minuto
  - ⚠️ `HighErrorRate` — taxa de erros 5xx acima de 10%
  - ⚠️ `KafkaEventsStopped` — sem eventos Kafka por 5+ minutos
  - ⚠️ `HighOutOfStockCount` — mais de 10 produtos em ruptura

### 📝 Logs Estruturados — Loki + Promtail

- **Loki4j Appender** envia logs diretamente para o Loki via HTTP (push)
- **Formato JSON** com labels indexáveis: `application`, `host`, `level`
- **TraceId e SpanId** incluídos em cada linha de log (correlação com Zipkin)
- **Promtail** coleta logs de sistema adicionais (`/var/log/*log`)
- Padrão de log colorido no console: `HH:mm:ss.SSS [thread] LEVEL logger - [traceId=...,spanId=...] - mensagem`

### 🔍 Tracing Distribuído — Zipkin

- **Micrometer Tracing Bridge** com exportador **OpenTelemetry → Zipkin**
- Sampling de **100%** (ajustável para produção)
- Correlação completa entre **logs, métricas e traces** via `traceId`

### 📊 Dashboards — Grafana

Grafana provisionado automaticamente com:
- **Prometheus** como datasource padrão de métricas
- **Loki** como datasource de logs
- Dashboards pré-configurados (provisionados via `docker/grafana/provisioning`)

Acessos:
- **Grafana**: http://localhost:6066 (admin / admin)
- **Prometheus**: http://localhost:6065
- **Zipkin**: http://localhost:6068
- **Kafka UI**: http://localhost:6064

---

## ⚡ Performance (Testes de Carga com k6)

> A API foi submetida a testes de estresse usando **k6**, demonstrando excelente desempenho em cenários realistas de alta carga.

### Cenário de Teste

| Parâmetro | Valor |
|-----------|-------|
| **Ferramenta** | k6 (Grafana) |
| **VUs (Virtual Users)** | 50 simultâneos |
| **Duração** | 30 segundos |
| **Endpoint testado** | `GET /api/v1/products` (com join em múltiplas tabelas) |
| **Autenticação** | JWT Bearer Token |
| **Carga total** | ~30.000 requisições |

### Resultados

| Métrica | Valor |
|---------|-------|
| **Throughput** | ~1.000 requisições/segundo |
| **p(99) Latência** | < 150ms |
| **p(95) Latência** | < 100ms |
| **Status 200** | 100% |
| **Falhas** | 0 |

> ⚡ **Destaque:** Mesmo sob 50 VUs concorrentes batendo em um endpoint que realiza joins entre `products`, `stocks`, `categories` e `suppliers` — com autenticação JWT completa — a API mantém latência abaixo de 150ms no percentil 99, sem uma única falha.

### Executar o teste localmente

```bash
# Com a API rodando via Docker Compose
k6 run teste-carga.js
```

---

## 📦 Pré-requisitos

| Ferramenta | Versão Mínima | Obrigatório |
|-----------|---------------|-------------|
| **Docker** | 24+ | ✅ Sim |
| **Docker Compose** | 2.20+ | ✅ Sim |
| **Java** | 21 | ❌ (apenas dev local) |
| **Maven Wrapper** | incluso (`./mvnw`) | ❌ (apenas dev local) |

---

## 🚀 Como Rodar

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/stockflow.git
cd stockflow/stockflow-api
```

### 2. Configure as variáveis de ambiente

```bash
cp variaveis-de-ambiente.example.env .env
```

Edite o arquivo `.env` com suas credenciais. As variáveis principais:

```bash
# Banco de Dados Principal
POSTGRES_DB=db_stockflow
POSTGRES_USER=seu_usuario
POSTGRES_PASSWORD=sua_senha

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_REALM=stock-flow-realm
KEYCLOAK_CLIENT_ID=stock-flow-api
KEYCLOAK_CLIENT_SECRET=seu_client_secret
```

### 3. Suba toda a stack com Docker Compose

```bash
docker compose up -d
```

Isso sobe **todos os 14 serviços** automaticamente:

```
✔ Network stockflow-network     Created
✔ Container db-keycloak         Healthy
✔ Container db-stockflow        Healthy
✔ Container keycloak            Started
✔ Container kafka               Started
✔ Container dragonfly           Started
✔ Container stockflow-api       Started
✔ Container prometheus          Started
✔ Container loki                Started
✔ Container promtail            Started
✔ Container zipkin              Started
✔ Container alertmanager        Started
✔ Container grafana             Started
✔ Container kafka-ui            Started
✔ Container nginx               Started
```

### 4. Verifique os serviços

```bash
# Health check da API
curl http://localhost:6060/actuator/health

# Swagger UI
open http://localhost:6060/swagger-ui.html

# Painel Keycloak
open http://localhost:6062  # admin / admin
```

### Portas

| Serviço | Porta | URL |
|---------|-------|-----|
| **API** | `6060` | http://localhost:6060 |
| **Keycloak** | `6062` | http://localhost:6062 |
| **PostgreSQL** | `6061` | `localhost:6061` |
| **Kafka** | `6063` | `localhost:6063` |
| **Kafka UI** | `6064` | http://localhost:6064 |
| **Prometheus** | `6065` | http://localhost:6065 |
| **Grafana** | `6066` | http://localhost:6066 |
| **Loki** | `6067` | http://localhost:6067 |
| **Zipkin** | `6068` | http://localhost:6068 |
| **AlertManager** | `6069` | http://localhost:6069 |
| **DragonflyDB** | `6070` | `localhost:6070` |

---

## 📚 Documentação da API

A documentação interativa dos endpoints está disponível via **Swagger/OpenAPI 3.0**.

### Acessar

```
http://localhost:6060/swagger-ui.html
```

### Autenticar no Swagger

1. Acesse o Swagger UI
2. Clique no botão **Authorize** 🔒
3. Cole seu token JWT (obtido via fluxo OAuth2 do Keycloak)
4. Todos os endpoints agora exibem o cadeado fechado — você pode testá-los diretamente

### Endpoints Disponíveis

| Tag | Base Path | Descrição |
|-----|-----------|-----------|
| **Users** | `/api/v1/users` | Gerenciamento de usuários e promoção de roles |
| **Products** | `/api/v1/products` | CRUD de produtos + busca + relatório PDF |
| **Stocks** | `/api/v1/stocks` | Controle de estoque, movimentações e transferências |
| **Suppliers** | `/api/v1/suppliers` | Gerenciamento de fornecedores e contatos |
| **Categories** | `/api/v1/categories` | Gerenciamento de categorias de produtos |
| **Movements** | `/api/v1/stocks/{id}/movements` | Histórico de movimentações |
| **Notifications** | `/api/v1/notifications` | Alertas e notificações do sistema |
| **Dashboard** | `/api/v1/dashboards` | Métricas analíticas de estoque |
| **Errors** | `/api/v1/errors` | Catálogo documentado de erros da API |

---

## 🏗️ Arquitetura

### Estrutura de Pacotes

```
com.gustavosdaniel.stock_flow_api
├── App.java                          # Entry point
├── config/                           # Configurações (Security, R2DBC, Kafka, Cache, etc.)
├── controller/                       # Controllers REST reativos
│   └── OpenApi/                      # Interfaces de documentação Swagger
├── domain/
│   ├── dto/
│   │   ├── request/                  # DTOs de entrada com Bean Validation
│   │   └── response/                 # DTOs de saída
│   │       └── dashboard/            # DTOs específicos de dashboards
│   ├── enums/                        # Enumerações do domínio
│   ├── mapping/                      # Conversores Entity ↔ DTO
│   └── po/                           # Persistent Objects (entidades R2DBC)
├── exception/                        # Exceções de domínio
│   └── handler/                      # Global Exception Handler
├── messaging/
│   ├── consumer/                     # Kafka Consumers reativos
│   ├── event/                        # Schemas de eventos
│   ├── producer/                     # Kafka Producers
│   └── outbox/                       # Transactional Outbox Pattern
├── repository/                       # Repositories reativos (R2DBC)
├── service/                          # Lógica de negócio
├── util/                             # Utilitários (SKU generator, PDF helper, etc.)
│   └── cache/                        # Gerenciamento de cache keys
└── client/
    └── viacep/                       # Client HTTP para consulta de CEP (WebClient)
```

### Stack Reativa

Toda a stack é **não-bloqueante** de ponta a ponta:

```
Netty (HTTP) → WebFlux (Controllers) → R2DBC (PostgreSQL)
                                      → ReactiveRedis (DragonflyDB)
                                      → Reactor Kafka (Mensageria)
                                      → WebClient (HTTP Clients)
```

### Pool de Conexões

O **R2DBC** utiliza pool reativo nativo configurado via `application.yaml`:

```yaml
spring:
  r2dbc:
    pool:
      enabled: true
      initial-size: 10      # Conexões iniciais
      max-size: 50           # Máximo de conexões
      max-idle-time: 30m     # TTL de conexão ociosa
      max-acquire-time: 10s  # Timeout para adquirir conexão
```

> O **HikariCP** também está presente — utilizado pelo **Flyway** durante as migrações (JDBC), enquanto o runtime é puramente R2DBC.

---

## 🔐 Segurança

### Fluxo de Autenticação

```
Cliente → Keycloak (Authorization Code / Password Grant)
           ↓
      JWT (RS256)
           ↓
Cliente → StockFlow API (Authorization: Bearer <jwt>)
           ↓
      Spring Security OAuth2 Resource Server
      Validação da assinatura via JWKS
      Extração de roles do claim "realm_access"
           ↓
      RBAC: USER | MANAGER | ADMIN
```

### Regras de Autorização (RBAC)

| Role | Permissões |
|------|-----------|
| **USER** | Leitura de produtos, estoque, categorias, fornecedores; registro de entradas/saídas |
| **MANAGER** | Tudo de USER + criar/editar produtos, categorias, fornecedores; promover usuários; acessar notificações e dashboards |
| **ADMIN** | Tudo de MANAGER + excluir recursos; acessar dashboard overview; gerenciar usuários |

### Rotas Públicas

As seguintes rotas **não exigem autenticação**:

```
/swagger-ui/**
/v3/api-docs/**
/actuator/health
/actuator/prometheus
```

---

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes de um serviço específico
./mvnw test -Dtest=ProductServiceTest

# Build sem testes
./mvnw clean package -DskipTests
```

---

## 📄 Licença

Este projeto está licenciado sob a **Apache License 2.0**. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">

**StockFlow API** — Gerenciamento de estoque reativo, resiliente e observável.

Feito com por **[Gustavo Silva Daniel](mailto:gustavosdaniel@hotmail.com)**

</div>
