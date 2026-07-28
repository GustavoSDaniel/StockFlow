# 📦 StockFlow — Painel de Administração

<p align="center">
  <img src="https://img.shields.io/badge/Angular-21.2-0F7ECA?style=flat-square&logo=angular&logoColor=white" alt="Angular 21.2" />
  <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?style=flat-square&logo=typescript&logoColor=white" alt="TypeScript 5.9" />
  <img src="https://img.shields.io/badge/Angular_Material-21.2-FF4081?style=flat-square&logo=angular&logoColor=white" alt="Angular Material 21.2" />
  <img src="https://img.shields.io/badge/Keycloak-26.0-00B9F1?style=flat-square&logo=keycloak&logoColor=white" alt="Keycloak 26.0" />
  <img src="https://img.shields.io/badge/Test-Vitest-6E9F18?style=flat-square&logo=vitest&logoColor=white" alt="Vitest" />
</p>

<p align="center">
  <em>Dashboard completo para gestão de estoque, produtos, fornecedores e categorias — com autenticação Keycloak e controle de acesso hierárquico.</em>
</p>

---

## 📖 Visão Geral

O **StockFlow Frontend** é a interface web do sistema StockFlow — uma SPA (*Single Page Application*) construída com Angular 21 para **gestão completa do fluxo de mercadorias**. O painel oferece controle total sobre estoque, produtos, fornecedores e categorias, com dashboards analíticos em tempo real e suporte a múltiplos perfis de acesso.

### 🎯 Funcionalidades

| Módulo | Descrição |
|:-------|:----------|
| 📊 **Dashboards** | Visão geral (KPI financeiro e de produtos), estoques, movimentações e fornecedores |
| 📦 **Produtos** | CRUD completo com SKU, código de barras, preço de custo/venda e margem |
| 🔄 **Movimentações** | Entradas, saídas, transferências, devoluções e ajustes — com histórico completo |
| 🏭 **Fornecedores** | Cadastro completo com múltiplos contatos e endereços (consulta ViaCEP) |
| 📂 **Categorias** | Organização hierárquica com subcategorias aninhadas |
| 👥 **Usuários** | Listagem e administração de perfis do realm Keycloak |
| 🔔 **Notificações** | Alertas inteligentes de estoque (baixo, crítico, excesso) com priorização |
| 🛡️ **RBAC** | Controle de acesso baseado em roles com hierarquia: `ADMIN` > `MANAGER` > `EMPLOYEE` |

---

## 🧱 Stack Tecnológica

<table>
  <tr>
    <th>Camada</th>
    <th>Tecnologia</th>
    <th>Versão</th>
    <th>Propósito</th>
  </tr>
  <tr>
    <td><strong>Framework</strong></td>
    <td><a href="https://angular.dev/">Angular</a></td>
    <td><code>^21.2</code></td>
    <td>SPA com Standalone Components, Lazy Loading e signals</td>
  </tr>
  <tr>
    <td><strong>Linguagem</strong></td>
    <td><a href="https://www.typescriptlang.org/">TypeScript</a></td>
    <td><code>~5.9</code></td>
    <td>Tipagem estática e tooling avançado</td>
  </tr>
  <tr>
    <td><strong>UI Toolkit</strong></td>
    <td><a href="https://material.angular.dev/">Angular Material</a> + <a href="https://material.angular.io/cdk/categories">CDK</a></td>
    <td><code>^21.2</code></td>
    <td>Design system Material 3 com tema customizado</td>
  </tr>
  <tr>
    <td><strong>Autenticação</strong></td>
    <td><a href="https://www.npmjs.com/package/keycloak-angular">keycloak-angular</a> + <a href="https://www.npmjs.com/package/keycloak-js">keycloak-js</a></td>
    <td><code>^16.0</code> / <code>^26.0</code></td>
    <td>OAuth2 / OpenID Connect via Keycloak</td>
  </tr>
  <tr>
    <td><strong>Reatividade</strong></td>
    <td><a href="https://rxjs.dev/">RxJS</a></td>
    <td><code>~7.8</code></td>
    <td>Programação reativa com Observables e BehaviorSubject</td>
  </tr>
  <tr>
    <td><strong>Build Tool</strong></td>
    <td><a href="https://angular.dev/tools/cli">Angular CLI</a></td>
    <td><code>^21.2</code></td>
    <td>Bundling, otimização e tree-shaking</td>
  </tr>
  <tr>
    <td><strong>Estilização</strong></td>
    <td>SCSS + Angular Material Theming</td>
    <td>—</td>
    <td>Tema azul/indigo com tipografia Inter e suporte a dark/light mode</td>
  </tr>
  <tr>
    <td><strong>Testes</strong></td>
    <td><a href="https://vitest.dev/">Vitest</a> + <a href="https://github.com/jsdom/jsdom">jsdom</a></td>
    <td><code>^4.0</code> / <code>^28.0</code></td>
    <td>Testes unitários rápidos com ambiente DOM simulado</td>
  </tr>
  <tr>
    <td><strong>Formatação</strong></td>
    <td><a href="https://prettier.io/">Prettier</a></td>
    <td><code>^3.8</code></td>
    <td>Code style consistente em todo o projeto</td>
  </tr>
</table>

---

## 🔐 Autenticação e Autorização

O StockFlow **não armazena credenciais** — toda autenticação é delegada a um servidor **[Keycloak](https://www.keycloak.org/)** via protocolo **OAuth2 / OpenID Connect**. O frontend atua como um *public client* que redireciona o fluxo de login para a tela do Keycloak e consome tokens JWT.

### Fluxo Resumido

```
┌──────────┐                        ┌────────────┐
│  Browser  │ ──① Login/Registro──→ │  Keycloak   │
│  (Angular)│ ←──② JWT + Refresh── │   Server    │
└─────┬─────┘                       └────────────┘
      │                                    │
      │ ③ Requisição c/ Bearer Token       │
      └────────────────────────────────────┘
                        ↓
                ┌──────────────┐
                │   StockFlow  │
                │    Backend   │
                └──────────────┘
```

### Camadas de Segurança

| Camada | Arquivo | Responsabilidade |
|:-------|:--------|:-----------------|
| **AuthService** | `core/auth/auth.service.ts` | Inicialização do Keycloak (`check-sso`), extração de roles via `realm_access`, renovação automática do token (30s antes da expiração), sincronização JIT do usuário com o backend via `GET /api/v1/users/me` |
| **apiInterceptor** | `core/http/api.interceptor.ts` | Prefixa a URL base da API e anexa `Authorization: Bearer <token>` em toda requisição HTTP |
| **errorInterceptor** | `core/http/error.interceptor.ts` | Tratamento centralizado de erros — redireciona ao Keycloak em caso de `401 Unauthorized` |
| **AuthGuard** | `core/auth/auth.guard.ts` | Protege rotas que exigem autenticação — redireciona para o Keycloak se não houver sessão ativa |
| **RoleGuard** | `core/auth/role.guard.ts` | Protege rotas sensíveis por perfil — redireciona para `/dashboard/stocks` se o usuário não tiver o nível necessário |

### Hierarquia de Perfis (RBAC)

O sistema opera com **três níveis hierárquicos de acesso**, onde cada nível herda as permissões do nível inferior:

```
ADMIN (nível 3)
  └── MANAGER (nível 2)
        └── EMPLOYEE (nível 1)
```

> 💡 Um usuário `ADMIN` pode acessar **todas** as rotas. Um `MANAGER` pode acessar rotas de `MANAGER` e `EMPLOYEE`. Um `EMPLOYEE` acessa apenas as rotas sem proteção de role.

### Matriz de Acesso por Rota

| Rota | EMPLOYEE | MANAGER | ADMIN |
|:-----|:--------:|:-------:|:-----:|
| `/dashboard/stocks` | ✅ | ✅ | ✅ |
| `/dashboard/movements` | ✅ | ✅ | ✅ |
| `/dashboard/overview` | ❌ | ❌ | ✅ |
| `/dashboard/suppliers` | ❌ | ✅ | ✅ |
| `/products` | ✅ | ✅ | ✅ |
| `/stocks` | ✅ | ✅ | ✅ |
| `/categories` | ✅ | ✅ | ✅ |
| `/suppliers` | ✅ | ✅ | ✅ |
| `/users` | ❌ | ✅ | ✅ |
| `/notifications` | ❌ | ✅ | ✅ |
| `/profile` | ✅ | ✅ | ✅ |

> 🔒 As roles são mapeadas a partir das *realm roles* do Keycloak. A lógica de hierarquia está implementada em `AuthService.hasRole()` e segue o princípio de *"nível suficiente"* — se `requiredRole = MANAGER`, tanto `MANAGER` quanto `ADMIN` passam.

---

## 🏗️ Arquitetura do Projeto

```
src/app/
├── core/                          # Infraestrutura e serviços transversais
│   ├── auth/                      # auth.service.ts, auth.guard.ts, role.guard.ts
│   ├── http/                      # api.interceptor.ts, error.interceptor.ts
│   ├── models/                    # domain.models.ts, enums.ts, page.model.ts, problem-detail.model.ts
│   └── services/                  # product, stock, supplier, category, user, notification, dashboard services
│
├── features/                      # Módulos funcionais (Lazy Loading)
│   ├── dashboard/                 # 4 dashboards: overview, stocks, movements, suppliers
│   ├── products/                  # CRUD de produtos (lista, formulário, detalhe)
│   ├── stocks/                    # Estoque e movimentações (entrada/saída/transferência)
│   ├── categories/                # Categorias hierárquicas
│   ├── suppliers/                 # Fornecedores com endereços e contatos
│   ├── users/                     # Gestão de usuários e perfil pessoal
│   ├── notifications/             # Central de alertas com filtros
│   └── landing/                   # Landing page pública (rota `/`)
│
└── shared/                        # Componentes reutilizáveis
    ├── layout/                    # main-layout, toolbar (com sino de notificações), sidebar
    ├── components/                # data-table, filter-bar, confirm-dialog, status-badge, loading-spinner, etc.
    └── pipes/                     # currency (BRL), enum-label (tradução de enums)
```

### Decisões Arquiteturais

- **Standalone Components** — sem `NgModule`. Cada componente declara suas próprias dependências, seguindo o paradigma moderno do Angular 19+.
- **Lazy Loading** completo — `loadChildren` para feature modules e `loadComponent` para dashboards individuais. O bundle inicial é mínimo.
- **APP_INITIALIZER** — a inicialização do Keycloak é resolvida **antes** da renderização da aplicação, garantindo que guards e services tenham o token disponível desde o primeiro frame.
- **Services injetáveis** — todos os services usam `providedIn: 'root'` e se comunicam com a API via `HttpClient`, retornando observables tipados.
- **Interceptors HTTP** — chain de dois interceptors: `apiInterceptor` (prefixa URL + anexa token) → `errorInterceptor` (trata erros).
- **Guards encadeados** — `AuthGuard` bloqueia acesso não autenticado; `RoleGuard` refina por permissão com hierarquia de roles.
- **RxJS BehaviorSubject** — estado reativo da sessão (`isAuthenticated$`, `userProfile$`, `token$`) consumido por guards e componentes.
- **Locale pt-BR** — aplicação internacionalizada em português brasileiro com pipes para moeda (BRL) e labels de enums.

---

## 🚀 Como Rodar

### Pré-requisitos

| Ferramenta | Versão Mínima | Verificação |
|:-----------|:-------------|:------------|
| **Node.js** | `>= 20.x` | `node --version` |
| **npm** | `>= 10.x` | `npm --version` |
| **Keycloak** | Servidor acessível | Definir em `src/environments/environment.ts` |
| **StockFlow Backend** | API rodando | Endpoint configurável em `environment.apiUrl` |

### Configuração de Ambiente

Edite `src/environments/environment.ts` com os endpoints do seu ambiente:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:6060',          // URL da API StockFlow
  keycloak: {
    url: 'http://localhost:6062',            // URL do Keycloak
    realm: 'stock-flow-realm',               // Realm configurado
    clientId: 'stock-flow-app',              // Client ID (public)
  },
};
```

### Rodando em Desenvolvimento

```bash
# 1. Acesse a pasta do frontend
cd stockflow-frontend

# 2. Instale as dependências (usa npm@10 conforme packageManager)
npm install

# 3. Inicie o servidor de desenvolvimento com hot reload
npm start
```

> 🌐 A aplicação estará disponível em **`http://localhost:4200`**

O servidor de desenvolvimento do Angular CLI oferece:
- **Hot Module Replacement** (HMR) — alterações refletem instantaneamente sem reload completo
- **Source Maps** — debugging direto no TypeScript original
- **Compilação incremental** — rebuilds rápidos apenas dos arquivos alterados

---

## 📦 Build de Produção

```bash
npm run build
```

O build de produção aplica as seguintes otimizações:

| Otimização | Descrição |
|:-----------|:----------|
| **File Replacement** | Substitui `environment.ts` por `environment.prod.ts` |
| **Minificação** | HTML, CSS e JavaScript minificados |
| **Tree Shaking** | Eliminação de código não utilizado |
| **Output Hashing** | Nomes de arquivos com hash para cache busting |
| **Bundle Budgets** | Alerta em 500 kB, erro em 1 MB (bundle inicial) |

O artefato final é gerado em **`dist/stockflow-frontend/`**, pronto para deploy em qualquer servidor HTTP (NGINX, Apache, S3 + CloudFront, etc.).

> 💡 Para servir os arquivos localmente e validar o build:
> ```bash
> npm run build && npx http-server dist/stockflow-frontend/browser -p 8080
> ```

---

## 📜 Scripts Disponíveis

| Comando | Descrição |
|:--------|:----------|
| `npm start` | Inicia o servidor de desenvolvimento (`ng serve`) em `localhost:4200` |
| `npm run build` | Gera build de produção otimizado em `dist/` |
| `npm run watch` | Build em modo desenvolvimento com watch (sem servidor) |
| `npm test` | Executa testes unitários com Vitest |
| `npx prettier --check .` | Verifica formatação do código |
| `npx prettier --write .` | Corrige formatação automaticamente |
| `npx ng generate component <path>` | Gera novo componente standalone |
| `npx ng generate service <path>` | Gera novo serviço injetável |

---

## 📁 Estrutura de Arquivos Relevantes

```
stockflow-frontend/
├── angular.json                  # Configuração do Angular CLI e builders
├── package.json                  # Dependências, scripts e packageManager
├── tsconfig.json                 # Configuração base do TypeScript
├── tsconfig.app.json             # Config TS para build da aplicação
├── public/                       # Assets estáticos (favicon, silent-check-sso.html)
├── src/
│   ├── main.ts                   # Bootstrap da aplicação (standalone)
│   ├── styles.scss               # Tema global Angular Material + estilos customizados
│   ├── environments/
│   │   ├── environment.ts        # Config de desenvolvimento
│   │   └── environment.prod.ts   # Config de produção
│   └── app/
│       ├── app.ts                # Root component (router-outlet)
│       ├── app.config.ts         # Providers globais + APP_INITIALIZER
│       └── app.routes.ts         # Árvore de rotas completa com guards
└── dist/                         # Artefatos de build (gerado)
```

---

## 📄 Licença

Projeto privado — todos os direitos reservados.
