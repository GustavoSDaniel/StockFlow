# 📊 Relatório Completo de Análise — StockFlow API

**Data:** 2026-06-09  
**Stack:** Java 21, Spring Boot 4.0.6, WebFlux, R2DBC, PostgreSQL, Redis, Keycloak  
**Analisado por:** Claude Code (especialista Java/Spring Boot/WebFlux)

---


## 🟠 ALTO (P2) — Performance e Resiliência


### 2.3 — N+1 Queries no `findSupplierByCnpj` e `createSupplier`

**Arquivo:** `SupplierService.java:117-142`

**Problema:** Para buscar um fornecedor completo, são feitas 3 consultas separadas:
1. `suppliersRepository.findByCnpj(cnpj)`
2. `supplierContactRepository.findAllBySupplierId(supplerId)`
3. `addressRepository.findAllBySupplierId(supplerId)`

Embora executadas em paralelo via `Mono.zip`, cada uma é uma query separada ao banco. Para um sistema com muitos acessos, isso gera sobrecarga.

**Sugestão:** Criar uma query customizada com JOIN quando necessário, ou cachear os contatos e endereços no Redis.

---



## 🟡 MÉDIO (P3) — Arquitetura e Boas Práticas

### 3.1 — SupplierMapper com chamada HTTP (violação SRP)

**Arquivo:** `SupplierMapper.java:60-103`

**Problema:** O método `toAddress()` no mapper faz uma chamada HTTP ao ViaCEP. Um mapper deve ser **transformação pura** (dados → dados), sem efeitos colaterais de I/O.

**Solução:** Mover a lógica do ViaCEP para o `SupplierService` ou criar um `AddressEnrichmentService`:

```java
// No SupplierService:
return addressEnrichmentService.enrich(request)
    .map(enriched -> supplierMapper.toAddress(supplierId, request, enriched));
```


---

## 🟢 BAIXO (P4) — Código Limpo e Manutenibilidade

### 4.1 — Typos e erros de grafia

| Arquivo | Linha | Erro | Correção |
|---------|-------|------|----------|
| `UserService.java` | 52, 217 | `createUSer` | `createUser` |
| `UserService.java` | 59 | `saveUSer` | `savedUser` |
| `SupplierService.java` | 73 | `salvedContacts` | `savedContacts` |
| `SecurityConfig.java` | 102 | `resourceAccess` com nome errado `stock-flow-app` | pode não bater com configuração do Keycloak |
| `CategoryService.java` | 82 | `Adiconando` | `Adicionando` |
| `CategoryService.java` | 218 | `encontradaso` | `encontradas` |
| `SupplierService.java` | 205 | `náo` (acento agudo) | `não` (til) |
| Vários lugares | vários | `validadte` | `validate` |
| `GlobalExceptionHandler.java` | 24 | `localDateTime.now()` | deveria usar `Instant.now()` para UTC |

### 4.2 — Logger: inconsistência `doOnNext` vs `doOnSuccess`

**Arquivos:** `CategoryService.java`, `SupplierService.java`, `UserService.java`

**Problema:** Para métodos retornando `Mono<Void>`:
- `doOnNext` **nunca dispara** (não há elemento para emitir quando o tipo é Void)
- `doOnSuccess` dispara corretamente

Em `activeCategory()` (linha 249), `doOnNext` é usado mas nunca será chamado porque o retorno é `.then()` (Mono<Void>). O log "Categoria ativada com sucesso" nunca aparece.

Em `disableCategory()` (linha 309), `doOnNext` funciona porque é chamado antes do `.then()` (ainda há um elemento Category).

**Verificação sistemática necessária:** Todo `doOnNext` em fluxo que termina com `.then()` deve ser `doOnSuccess`.

---

### 4.3 — Logs em português misturados com código em inglês

O código (nomes de classes, métodos, variáveis) está em inglês (correto para um projeto Java), mas os logs estão em português. Isso não é um problema grave, mas dificulta ferramentas de agregação de logs (ELK, Grafana) que esperam inglês.

---

### 4.4 — `PageUtils` como utility class — alternativa com extensão Reativa

**Arquivo:** `PageUtils.java`

**Problema:** O padrão `PageUtils.toPage(flux, count, mapper, pageable)` já é uma melhoria sobre a duplicação anterior, mas ainda requer passar 4 argumentos em cada chamada.

**Sugestão:** Criar uma extensão reativa customizada:
```java
public class ReactivePageUtils {
    public static <T, R> Function<Flux<T>, Mono<Page<R>>> toPage(
            Mono<Long> count, Function<T, R> mapper, Pageable pageable) {
        return data -> data.map(mapper)
            .collectList()
            .zipWith(count)
            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }
}
```

---

### 4.5 — Repositórios: método `count()` nativo vs derivado

**Arquivos:** `CategoryRepository.java`, `SuppliersRepository.java`, `UserRepository.java`

**Problema:** Métodos como `countByIsActiveTrue()`, `countByParentId()`, etc., são derivados pelo Spring Data — funcionam, mas geram queries com performance imprevisível.

**Sugestão:** Para queries de contagem críticas, use `@Query` explícito com índices adequados.

---

## 📊 Tabelas de Avaliação

### Arquitetura Limpa: 7.5/10

| Critério | Nota | Comentário |
|----------|------|------------|
| Separação de camadas | 9/10 | Controller → Service → Repository bem definida |
| Domínio rico | 8/10 | Entidades com comportamento, enums inteligentes |
| DTOs/Records | 9/10 | Separação clara request/response, imutabilidade |
| Interfaces de serviço | 3/10 | Inexistentes — serviços são concretos |
| Inversão de dependência | 6/10 | Boa injeção, mas sem abstrações |
| Eventos de domínio | 7/10 | Iniciado mas não integrado |

### Código Limpo: 7.0/10

| Critério | Nota | Comentário |
|----------|------|------------|
| Nomenclatura | 7/10 | Bons nomes, mas com typos |
| Imutabilidade | 9/10 | Records, BaseImmutableEntity — excelente |
| DRY | 7/10 | PageUtils reduziu bastante a duplicação |
| Tratamento de erros | 7/10 | Bom, mas AccessDeniedException errado |
| Testabilidade | 6/10 | Testes existem mas incompletos |
| Documentação | 8/10 | JavaDoc detalhado, OpenAPI bem configurada |

### Performance: 5.5/10

| Critério | Nota | Comentário |
|----------|------|------------|
| Stack reativa | 9/10 | WebFlux + R2DBC é o caminho certo |
| Cache | 2/10 | Infraestrutura pronta mas não utilizada |
| Resiliência | 3/10 | resilience4j no pom mas sem uso |
| Índices de banco | 8/10 | Flyway com índices parciais |
| Transações | 4/10 | @Transactional com R2DBC é incerto |
| Pool de conexões | 5/10 | Sem tuning explícito |

---

## 🎯 Plano de Ação Priorizado

| # | Prioridade | Ação | Esforço | Impacto |
|---|-----------|------|---------|---------|
| 1 | 🔴 P1 | Substituir `@Transactional` por `TransactionalOperator` | 4h | Integridade de dados |
| 2 | 🔴 P1 | Adicionar `@EnableCaching` no `App.java` | 1min | Cache funcional |
| 3 | 🔴 P1 | Corrigir indentação do `application.yaml` (`security` sob `spring`) | 5min | Configuração funcional |
| 4 | 🔴 P1 | Corrigir import `AccessDeniedException` em `SecurityUtils` | 1min | Bug fix |
| 5 | 🟠 P2 | Ativar `@Cacheable` nos serviços (agora com `@EnableCaching`) | 3h | Performance leitura |
| 6 | 🟠 P2 | Adicionar `@CircuitBreaker` no ViaCEP | 1h | Resiliência |
| 7 | 🟡 P3 | Corrigir dependência SpringDoc (webmvc → webflux) | 5min | Compatibilidade |
| 8 | 🟡 P3 | Mover chamada ViaCEP do Mapper para Service | 2h | Arquitetura limpa |
| 9 | 🟡 P3 | Integrar Domain Events com ApplicationEventPublisher | 3h | Desacoplamento |
| 10 | 🟡 P3 | Adicionar interfaces de serviço | 2h | Clean Architecture |
| 11 | 🟡 P3 | Configurar pool de conexões R2DBC | 30min | Performance |
| 12 | 🟢 P4 | Corrigir typos e nomenclatura | 1h | Profissionalismo |
| 13 | 🟢 P4 | Revisar `doOnNext` vs `doOnSuccess` em fluxos Void | 30min | Logs corretos |
| 14 | 🟢 P4 | Adicionar testes para SupplierService e controllers | 6h | Qualidade |

---

## ✅ O que está Excelente

1. **Domínio rico e bem modelado** — `Category` com `addSubCategory`/`removeSubCategory`, `UserRole` com hierarquia `canManage()`, enums com comportamento
2. **Uso de Records para DTOs** — imutabilidade, validação Jakarta, construtor compacto para sanitização
3. **BaseEntity/BaseImmutableEntity** — reuso de auditoria com `@CreatedBy`, `@CreatedDate`, `@Version`
4. **Conversores R2DBC customizados** — mapeamento limpo entre enums Java e VARCHAR no PostgreSQL
5. **Flyway** para versionamento de schema (embora as migrations não estejam visíveis no diretório analisado)
6. **OpenAPI/Swagger** bem documentada com tags, schemas de segurança e documentação de erros
7. **Segurança bem granular** — regras específicas por endpoint e método HTTP no `SecurityConfig`
8. **JWT role extraction** com fallback hierárquico (realm_access → resource_access → EMPLOYEE)
9. **KeycloakService** com sincronização JIT de roles — evita Dual Write problem
10. **ReactorContextConfig** com `Hooks.enableAutomaticContextPropagation()` — necessário para transações reativas
