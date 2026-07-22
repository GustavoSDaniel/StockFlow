# 📦 StockFlow — Painel de Administração

<p align="center">
  <em>Dashboard completo para gestão de estoque, produtos, fornecedores e categorias.</em>
</p>

---

## 📖 Visão Geral

O **StockFlow Frontend** é o painel de administração (*Dashboard*) do sistema StockFlow — uma aplicação web moderna para **gestão de estoque e produtos**. A interface oferece controle total sobre o fluxo de mercadorias, permitindo que administradores, gerentes e funcionários acompanhem entradas e saídas, monitorem níveis de estoque, gerenciem fornecedores e categorias, e visualizem dashboards analíticos em tempo real.

**Principais funcionalidades:**

- 📊 **Dashboards interativos** — visão geral, estoques, movimentações e fornecedores
- 📦 **CRUD de Produtos** — cadastro completo com imagens, categorias e fornecedores vinculados
- 🏭 **Gestão de Fornecedores** — listagem, cadastro e detalhamento
- 📂 **Categorias** — organização hierárquica dos produtos
- 🔄 **Movimentações de Estoque** — registro de entradas e saídas com histórico
- 👥 **Gestão de Usuários** — listagem e administração de perfis
- 🔔 **Notificações** — alertas de estoque baixo e eventos do sistema
- 🛡️ **Controle de Acesso por Perfil** — ADMIN, MANAGER e EMPLOYEE

---

## 🧱 Stack Tecnológica

| Camada | Tecnologia | Versão |
|:-------|:-----------|:-------|
| **Framework** | [Angular](https://angular.dev/) | `^21.2.0` |
| **Linguagem** | [TypeScript](https://www.typescriptlang.org/) | `~5.9.2` |
| **UI Toolkit** | [Angular Material](https://material.angular.dev/) | `^21.2.14` |
| **Autenticação** | [Keycloak Angular](https://www.npmjs.com/package/keycloak-angular) + [Keycloak JS](https://www.npmjs.com/package/keycloak-js) | `^16.0.0` / `^26.0.0` |
| **Programação Reativa** | [RxJS](https://rxjs.dev/) | `~7.8.0` |
| **Estilização** | SCSS + Angular Material Theming | — |
| **Testes** | [Vitest](https://vitest.dev/) | `^4.0.8` |
| **Build Tool** | [Angular CLI](https://angular.dev/tools/cli) | `^21.2.19` |
| **Formatador** | [Prettier](https://prettier.io/) | `^3.8.1` |

> 🎨 O design system utiliza **Angular Material** com tema customizado nas cores azul/indigo e tipografia **Inter**, garantindo uma interface limpa, moderna e responsiva.

---

## 🔐 Autenticação e Segurança

O StockFlow **não armazena credenciais** no lado do cliente. Toda autenticação é delegada a um servidor **[Keycloak](https://www.keycloak.org/)** externo, seguindo os padrões **OAuth2 / OpenID Connect**.

### Fluxo de Autenticação

```
┌──────────┐     ① Login/Registro      ┌────────────┐
│  Usuário  │ ──────────────────────────→ │  Keycloak   │
│ (Browser) │ ←────────────────────────── │   Server    │
└──────────┘   ② JWT (Access Token)     └────────────┘
     │                                         │
     │  ③ Requisição com Bearer Token          │
     └─────────────────────────────────────────┘
                       ↓
               ┌──────────────┐
               │  StockFlow   │
               │   Backend    │
               └──────────────┘
```

### Camadas implementadas:

| Mecanismo | Descrição |
|:----------|:----------|
| **`AuthService`** | Gerencia o ciclo de vida da sessão: inicialização do Keycloak via `check-sso`, login, logout, renovação automática do token JWT e extração de perfis (*realm roles*). |
| **`apiInterceptor`** | Intercepta todas as requisições HTTP e anexa automaticamente o header `Authorization: Bearer <token>`, além de prefixar a URL base da API. |
| **`errorInterceptor`** | Trata erros HTTP de forma centralizada, redirecionando para o fluxo de login em caso de `401 Unauthorized`. |
| **`AuthGuard`** | Protege as rotas autenticadas — redireciona o usuário para o Keycloak se não houver sessão ativa. |
| **`RoleGuard`** | Protege rotas sensíveis com base nos perfis do Keycloak (`ADMIN`, `MANAGER`, `EMPLOYEE`), usando hierarquia de permissões. |

> 🔒 **Nenhuma senha trafega ou é armazenada pelo frontend.** Toda a troca de credenciais ocorre exclusivamente entre o navegador e o servidor Keycloak.

---

## 🏗️ Arquitetura do Frontend

O projeto segue uma arquitetura modular baseada em **Standalone Components** do Angular, organizada em três grandes camadas:

```
src/app/
├── core/                     # Infraestrutura e serviços transversais
│   ├── auth/                 # AuthService, AuthGuard, RoleGuard
│   ├── http/                 # Interceptors HTTP (API + Error)
│   ├── models/               # Interfaces, enums e tipos de domínio
│   └── services/             # Serviços de negócio (Product, Stock, Category, etc.)
│
├── features/                 # Módulos funcionais com Lazy Loading
│   ├── dashboard/            # Dashboards (Overview, Stocks, Movements, Suppliers)
│   ├── products/             # CRUD de Produtos (lista, formulário, detalhe)
│   ├── stocks/               # Gestão de Estoque (lista, formulário, movimentações)
│   ├── categories/           # Categorias (lista, formulário)
│   ├── suppliers/            # Fornecedores (lista, formulário, detalhe)
│   ├── users/                # Usuários (lista, perfil)
│   ├── notifications/        # Central de notificações
│   └── landing/              # Landing page pública (não autenticada)
│
└── shared/                   # Componentes e utilitários reutilizáveis
    ├── layout/               # MainLayout, Sidebar, Toolbar
    ├── components/           # DataTable, FilterBar, ConfirmDialog, StatusBadge, etc.
    └── pipes/                # CurrencyPipe, EnumLabelPipe
```

### Padrões adotados:

- **Lazy Loading** completo — cada feature é carregada sob demanda via `loadChildren` / `loadComponent`
- **Services injetáveis** com `providedIn: 'root'` — comunicação com a API centralizada em serviços tipados
- **Componentes *standalone*** — sem uso de `NgModule`, seguindo o paradigma moderno do Angular
- **Route Guards encadeados** — `AuthGuard` → `RoleGuard` para proteção granular das rotas
- **RxJS Observables** — estado reativo da autenticação via `BehaviorSubject`

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos

- [Node.js](https://nodejs.org/) `>= 20.x`
- npm `>= 10.x` (gerenciado via `packageManager` no `package.json`)
- Um servidor [Keycloak](https://www.keycloak.org/) configurado e acessível

### Instalação e Execução

```bash
# 1. Clone o repositório e acesse a pasta do frontend
cd stockflow-frontend

# 2. Instale as dependências
npm install

# 3. Configure as variáveis de ambiente (Keycloak + API)
#    Edite os arquivos em src/environments/ com as URLs do seu ambiente

# 4. Inicie o servidor de desenvolvimento
npm start
# ou explicitamente:
ng serve
```

O servidor de desenvolvimento será iniciado em:

> 🌐 **`http://localhost:4200`**

O *hot reload* está habilitado — qualquer alteração no código-fonte dispara a recompilação automática.

### Build de Produção

```bash
npm run build
```

O artefato otimizado será gerado no diretório `dist/stockflow-frontend/`.

### Executando Testes

```bash
npm test
```

Testes unitários com [Vitest](https://vitest.dev/) para componentes, serviços e pipes.

---

## 📁 Variáveis de Ambiente

As configurações de ambiente estão em `src/environments/`:

| Variável | Descrição |
|:---------|:----------|
| `apiUrl` | URL base da API StockFlow Backend |
| `keycloak.url` | URL do servidor Keycloak |
| `keycloak.realm` | Nome do Realm no Keycloak |
| `keycloak.clientId` | Client ID registrado no Keycloak |

---

## 📄 Licença

Projeto privado — todos os direitos reservados.
