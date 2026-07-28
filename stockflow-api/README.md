# 📦 StockFlow API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![WebFlux](https://img.shields.io/badge/Reactive-WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-26.0-4D4D4D?style=for-the-badge&logo=keycloak&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-4.0-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![DragonflyDB](https://img.shields.io/badge/DragonflyDB-Cache-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-✓-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Zipkin](https://img.shields.io/badge/Zipkin-Tracing-FF6B00?style=for-the-badge)
![Resilience4j](https://img.shields.io/badge/Resilience4j-CB_RT_TO-81C784?style=for-the-badge)
![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)
![License](https://img.shields.io/badge/License-Apache_2.0-D22128?style=for-the-badge)

</div>

---

## 📋 Visão Geral

**StockFlow API** é um backend reativo de alta performance para gerenciamento de estoques, construído sobre **Spring WebFlux** e **R2DBC**. Projetada para operar em ambientes de alta concorrência, a API oferece controle completo sobre o ciclo de vida de produtos, fornecedores, categorias e movimentações de estoque — desde a criação até a geração de relatórios em PDF, com autenticação robusta via Keycloak e RBAC.

### 🔑 Principais Funcionalidades

- **CRUD completo** de produtos, fornecedores, categorias e usuários
- **Controle de estoque** com entradas, saídas, ajustes e transferências entre almoxarifados
- **Rastreabilidade** total via histórico de movimentações com razão e tipo documentados
- **Dashboards analíticos** com métricas de estoque (ruptura, baixo estoque, excesso)
- **Relatórios em PDF** de produtos e estoque gerados sob demanda
- **Sistema de notificações** para alertas de estoque crítico
- **Autenticação/autorização** baseada em JWT via Keycloak com RBAC (roles: `EMPLOYEE`, `MANAGER`, `ADMIN`)
- **Promoção de usuários** via Service Account Keycloak (client-credentials grant)
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
| **Autenticação** | Keycloak | 26.0.8 | OAuth2 / OpenID Connect com JWT |
| **Admin Client** | Keycloak Admin Client | 26.0.8 | Comunicação server-to-server para gestão de usuários |
| **Autorização** | Spring Security | — | Resource Server OAuth2 + RBAC |
| **Mensageria** | Apache Kafka | 4.0 | Producer/Consumer reativos com Reactor Kafka |
| **Cache** | DragonflyDB | 1.37 | Cache Redis-compatible de alta performance |
| **Resiliência** | Resilience4j | 2.4 | Circuit Breaker, Retry e Timeout |
| **Notificações** | SSE (Sinks.Many) | — | Push em tempo real com backpressure buffer |
| **PDF** | OpenPDF | 3.0.5 | Geração de relatórios em PDF |
| **Documentação** | SpringDoc OpenAPI | 2.8.10 | Swagger UI interativo |
| **Containerização** | Docker + Compose | — | Orquestração completa do ecossistema |
| **Infraestrutura** | AWS EC2 | — | Deploy em nuvem com Docker Compose |

---

## 🔐 Arquitetura de Segurança

> O StockFlow implementa um modelo de segurança de **dupla autenticação**: os usuários finais autenticam-se com seus tokens JWT pessoais, enquanto operações administrativas que exigem comunicação com a API do Keycloak utilizam uma **Service Account** dedicada.

### Modelo de Dupla Autenticação

```
┌──────────────────────────────────────────────────────────────────────┐
│                        FLUXO DO USUÁRIO FINAL                        │
│                                                                      │
│  Frontend (Angular)                                                   │
│       │                                                               │
│       │  1. Authorization Code Grant (PKCE)                          │
│       ▼                                                               │
│  ┌──────────┐     2. JWT (RS256)     ┌──────────────────┐           │
│  │ Keycloak │ ───────────────────────▶│  StockFlow API   │           │
│  │  :6062   │                         │  Resource Server │           │
│  └──────────┘                         │  :6060           │           │
│                                       └──────────────────┘           │
│                                         │                             │
│                                         │ Valida assinatura via JWKS  │
│                                         │ Extrai roles do realm_access│
│                                         │ Aplica RBAC por endpoint    │
│                                         ▼                             │
│                                       ┌──────────────────┐           │
│                                       │  Controller      │           │
│                                       │  (EMPLOYEE /     │           │
│                                       │   MANAGER /      │           │
│                                       │   ADMIN)         │           │
│                                       └──────────────────┘           │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│                   FLUXO SERVER-TO-SERVER (SERVICE ACCOUNT)           │
│                                                                      │
│  StockFlow API                                                        │
│       │                                                               │
│       │  3. Client Credentials Grant                                 │
│       │     (client_id + client_secret)                              │
│       ▼                                                               │
│  ┌──────────┐     4. Admin Access Token    ┌──────────────────┐     │
│  │ Keycloak │ ◄────────────────────────────│  KeycloakService │     │
│  │  :6062   │                              │  (Admin Client)  │     │
│  └──────────┘                              └──────────────────┘     │
│       │                                         │                     │
│       │  5. Admin REST API calls                │                     │
│       │     (assign/remove realm roles)         │                     │
│       ▼                                         │                     │
│  ┌──────────────────────────────────────┐       │                     │
│  │  PUT /admin/realms/{realm}/users/    │◄──────┘                     │
│  │      {id}/role-mappings/realm        │                             │
│  └──────────────────────────────────────┘                             │
└──────────────────────────────────────────────────────────────────────┘
```

### Por que duas autenticações?

| Autenticação | Tipo | Propósito | Token |
|---|---|---|---|
| **Usuário final** | Authorization Code (PKCE) | Autenticar o usuário no frontend; autorizar requests à API | JWT do usuário (roles: `employee`, `manager`, `admin`) |
| **Service Account** | Client Credentials | Permite que o backend chame a Admin REST API do Keycloak para gerenciar roles de usuários | Token do client `stock-flow-api` |

A API **nunca usa o token do usuário final** para chamar o Keycloak. Em vez disso, utiliza uma **Service Account** — um client OAuth2 configurado no Keycloak com `client_credentials` grant e permissões de admin no realm. Isso garante que:

1. **Separação de privilégios**: o usuário final não precisa (nem deve) ter permissões administrativas no Keycloak.
2. **Segurança**: o `client_secret` da service account nunca sai do backend.
3. **Auditoria**: as alterações de role são feitas em nome do client `stock-flow-api`, não do usuário logado.

### Fluxo Completo de Promoção de Usuário

```
1. MANAGER faz POST /api/v1/users/{id}/promote  (com JWT do Manager)
                    │
2. Spring Security valida JWT (assinatura, expiração)
                    │
3. @PreAuthorize / .hasAnyRole("MANAGER", "ADMIN") → autorizado
                    │
4. UserService.promoteUser()
   ├── Valida que o manager não está se auto-promovendo
   ├── Valida hierarquia de roles (manager pode promover employee, não admin)
   ├── Atualiza role no banco local (R2DBC)
   │
   └── KeycloakService.updateUserRoleInKeycloak()
        ├── Keycloak Admin Client autentica com client_credentials
        ├── Obtém lista de roles atuais do usuário
        ├── Remove roles antigas (ex: "employee")
        ├── Busca a nova role no realm (ex: RoleRepresentation de "manager")
        └── Adiciona a nova role ao usuário
                    │
5. Sucesso → transação confirmada no DB local + Keycloak
   Falha  → rollback da role no DB local, erro propagado ao cliente
```

### Configuração da Service Account no Keycloak

O client `stock-flow-api` é criado diretamente no painel do Keycloak com as seguintes configurações:

| Campo | Valor | Descrição |
|---|---|---|
| **Client ID** | `stock-flow-api` | Identificador do client |
| **Client Authentication** | `On` | Habilita autenticação confidencial |
| **Authorization** | `On` | Habilita controle de acesso |
| **Authentication Flow** | `Service Account` | Permite client_credentials grant |
| **Service Account Roles** | `realm-management` → `manage-users`, `view-users`, `view-realm` | Permissões para gerenciar roles de usuários |

> ⚠️ **Importante**: As roles de realm (`employee`, `manager`, `admin`) devem existir no Keycloak em **Realm Roles** para que o `KeycloakService` consiga atribuí-las. A busca é case-insensitive (ex: `equalsIgnoreCase`), mas as roles precisam existir.

### Roles e Hierarquia

| Role no Keycloak | Enum Java | Nível | Permissões |
|---|---|---|---|
| `employee` | `EMPLOYEE` | 1 | Leitura de produtos, estoque, categorias, fornecedores; registro de entradas/saídas |
| `manager` | `MANAGER` | 2 | Tudo de EMPLOYEE + criar/editar recursos; promover usuários até MANAGER; acessar notificações e dashboards |
| `admin` | `ADMIN` | 3 | Tudo de MANAGER + excluir recursos; promover qualquer usuário; acessar dashboard overview |

A hierarquia é numérica: `ADMIN(3) > MANAGER(2) > EMPLOYEE(1)`. Um cargo só pode gerenciar cargos de nível estritamente inferior.

### Conversão de JWT → Spring Security

A classe `SecurityConfig` implementa um `ReactiveJwtAuthenticationConverter` que:

1. Lê o claim `realm_access.roles` do JWT
2. Lê o claim `resource_access.stock-flow-app.roles` (roles de client)
3. Concatena ambas as listas
4. Prefixa cada role com `ROLE_` (ex: `manager` → `ROLE_MANAGER`)
5. Registra como `GrantedAuthority` no `SecurityContext`

Isso permite que as regras `.hasRole("MANAGER")` no `SecurityWebFilterChain` casem com as roles do token.

### Rotas Públicas

As seguintes rotas **não exigem autenticação**:

```
/swagger-ui/**
/v3/api-docs/**
/actuator/health
/actuator/prometheus
```

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
- Sampling de **100%** (ajustável em produção via `management.tracing.sampling.probability`)
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

| Ferramenta | Versão Mínima | Obrigatório | Uso |
|-----------|---------------|-------------|-----|
| **Docker** | 24+ | ✅ Sim | Containerização de todos os serviços |
| **Docker Compose** | 2.20+ | ✅ Sim | Orquestração local e produção |
| **Java** | 21 | ❌ (apenas dev) | Compilação e execução local com Maven |
| **Maven Wrapper** | incluso (`./mvnw`) | ❌ (apenas dev) | Build sem instalação global do Maven |

---

## 🚀 Como Rodar Localmente

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/stockflow.git
cd stockflow/stockflow-api
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```bash
# ============================================
# Banco de Dados Principal (StockFlow)
# ============================================
POSTGRES_DB=db_stockflow
POSTGRES_USER=gustavo
POSTGRES_PASSWORD=uma_senha_forte

# ============================================
# Banco de Dados do Keycloak
# ============================================
KEYCLOAK_DB=keycloak
KEYCLOAK_DB_USER=keycloak
KEYCLOAK_DB_PASSWORD=uma_senha_forte

# ============================================
# Keycloak — Admin Console
# ============================================
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# ============================================
# Keycloak — Realm & Client (Service Account)
# ============================================
KEYCLOAK_REALM=stock-flow-realm
KEYCLOAK_CLIENT_ID=stock-flow-api
KEYCLOAK_CLIENT_SECRET=seu_client_secret_aqui
```

> ⚠️ O `KEYCLOAK_CLIENT_SECRET` deve ser copiado da aba **Credentials** do client `stock-flow-api` no painel do Keycloak. Sem ele, o `KeycloakAdminClient` não consegue autenticar e operações como promoção de usuários falharão.

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

### 4. Configure o Keycloak (primeira execução)

Após subir a stack, acesse o painel do Keycloak e configure os itens necessários:

1. Acesse **http://localhost:6062** e faça login com `admin` / `admin`
2. Crie o realm **`stock-flow-realm`**
3. Crie o client **`stock-flow-app`** (para o frontend Angular) — `public`, com `Standard Flow` e `Direct Access Grants`
4. Crie o client **`stock-flow-api`** (para a Service Account) — `confidential`, com `Client Authentication`, `Authorization` e `Service Account` habilitados
5. Nas **Service Account Roles** do client `stock-flow-api`, atribua:
   - `realm-management` → `manage-users`
   - `realm-management` → `view-users`
   - `realm-management` → `view-realm`
6. Em **Realm Roles**, crie as três roles:
   - `employee`
   - `manager`
   - `admin`
7. Copie o **Client Secret** do client `stock-flow-api` e cole no `.env` como `KEYCLOAK_CLIENT_SECRET`
8. Reinicie o container da API: `docker compose restart stockflow-api`

### 5. Verifique os serviços

```bash
# Health check da API
curl http://localhost:6060/actuator/health

# Swagger UI
open http://localhost:6060/swagger-ui.html

# Painel Keycloak
open http://localhost:6062
```

### Portas

| Serviço | Porta | URL |
|---------|-------|-----|
| **API** | `6060` | http://localhost:6060 |
| **Nginx** | `80` / `443` | http://localhost |
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

## ☁️ Estrutura de Deploy (AWS EC2 + Docker Compose)

> O StockFlow é implantado em uma instância **AWS EC2** utilizando **Docker Compose** como orquestrador, com **Nginx** como proxy reverso e **Certbot** para certificados SSL/TLS.

### Arquitetura de Produção

```
                              Internet
                                  │
                                  ▼
                         ┌────────────────┐
                         │   Cloudflare   │
                         │  (DNS + Proxy) │
                         └───────┬────────┘
                                 │
                                 ▼
                   ┌─────────────────────────┐
                   │  AWS EC2 (t3.medium)    │
                   │  Ubuntu 24.04 LTS       │
                   │                         │
                   │  ┌───────────────────┐  │
                   │  │  Nginx :80/:443   │  │
                   │  │  (proxy reverso,  │  │
                   │  │   SSL via Certbot)│  │
                   │  └────────┬──────────┘  │
                   │           │             │
                   │  ┌────────▼──────────┐  │
                   │  │  stockflow-api    │  │
                   │  │  :6060            │  │
                   │  │  (-Xmx350m)       │  │
                   │  └────────┬──────────┘  │
                   │           │             │
                   │  ┌────────▼──────────┐  │
                   │  │  Docker Compose   │  │
                   │  │  ┌─────────────┐  │  │
                   │  │  │ PostgreSQL  │  │  │
                   │  │  │ Kafka       │  │  │
                   │  │  │ Keycloak    │  │  │
                   │  │  │ DragonflyDB │  │  │
                   │  └──┴─────────────┴──┘  │
                   └─────────────────────────┘
```

### Estrutura de Diretórios na EC2

```
/home/ubuntu/stockflow/
├── docker-compose.prod.yml    # Compose de produção (com resource limits)
├── .env                        # Variáveis de ambiente (não versionado)
├── nginx/
│   └── conf.d/
│       ├── stockflow.conf      # Virtual host + proxy reverso
│       └── nginx.conf          # Configuração global do Nginx
├── keycloak/
│   └── themes/
│       └── stockflow/          # Tema customizado do login
├── certbot/
│   └── www/                    # Desafios HTTP do Let's Encrypt
└── backup/
    └── postgres/               # Scripts de backup do banco
```

### docker-compose.prod.yml (principais diferenças do ambiente local)

| Recurso | Local (dev) | Produção (EC2) |
|---|---|---|
| **stockflow-api** | `build: .` | `image: gustavosdaniel/stockflow-api:latest` (imagem pré-buildada) |
| **Keycloak** | `start-dev` | `start --proxy-headers forwarded` com `KC_HOSTNAME` configurado |
| **Nginx** | Sem SSL | Com SSL (Certbot + Let's Encrypt) |
| **Limites de memória** | Não configurados | `deploy.resources.limits.memory` em cada serviço |
| **Kafka UI** | Presente | Removido (não exposto em produção) |
| **Grafana/Prometheus** | Presente | Removido (stack de observabilidade separada) |
| **Loki/Promtail** | Presente | Desativado (logs vão para o CloudWatch) |

### Limites de Recursos (EC2 t3.medium — 4 GB RAM)

| Serviço | Limite de Memória | Justificativa |
|---|---|---|
| **stockflow-api** | 450 MB | `-Xmx350m` + overhead Netty |
| **PostgreSQL (app)** | 200 MB | Apenas uma aplicação |
| **PostgreSQL (Keycloak)** | 150 MB | Uso leve; apenas users/roles/sessions |
| **Keycloak** | 400 MB | Overhead do Quarkus + cache de sessões |
| **Kafka** | 400 MB | `-Xmx256m` + overhead do broker |
| **DragonflyDB** | 700 MB | `--maxmemory=600mb` para cache em memória |
| **Nginx** | 50 MB | Proxy reverso leve |

> Com ~2.35 GB alocados para os containers, resta ~1.65 GB para o OS e buffers do sistema.

### Fluxo de Deploy

```bash
# 1. Build local da imagem
./mvnw clean package -DskipTests
docker build -t gustavosdaniel/stockflow-api:latest .

# 2. Push para o Docker Hub
docker push gustavosdaniel/stockflow-api:latest

# 3. Na EC2: pull e restart
ssh ec2-user@api.stockflow.gustavosdaniel.com
cd /home/ubuntu/stockflow
docker compose -f docker-compose.prod.yml pull stockflow-api
docker compose -f docker-compose.prod.yml up -d stockflow-api
```

### Domínios e Rotas

| URL | Serviço | Acesso |
|---|---|---|
| `https://api.stockflow.gustavosdaniel.com/*` | StockFlow API REST | Público (autenticado) |
| `https://api.stockflow.gustavosdaniel.com/auth/*` | Keycloak | Público (login/cadastro) |
| `https://api.stockflow.gustavosdaniel.com/swagger-ui.html` | Swagger UI | Público |
| `https://stockflow.gustavosdaniel.com` | Frontend Angular | Público |

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

> **Nota sobre o Keycloak Admin Client:** O `KeycloakService` é a única classe que utiliza chamada **bloqueante** (API HTTP síncrona do Keycloak Admin Client). Essas chamadas são isoladas em `Schedulers.boundedElastic()` via `Mono.fromRunnable().subscribeOn(boundedElastic)`, garantindo que não bloqueiem as threads do event loop do Netty.

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

> O **HikariCP** está presente para o **Flyway** (migrações JDBC), enquanto o runtime é puramente R2DBC.

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

Feito por **[Gustavo Silva Daniel](mailto:gustavosdaniel@hotmail.com)**

</div>
