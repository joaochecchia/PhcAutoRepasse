---
name: phc-spring-backend
description: Convenções do backend Spring Boot do PHC Auto (Clean Architecture, monólito modular com Spring Modulith, write/read separados). Use ao criar ou alterar código Java em backend/ — módulos, casos de uso, gateways, controllers, entidades JPA, Redis ou integrações externas.
---

# Backend Spring — PHC Auto

Fonte da verdade: `backend/AGENTS.md`. Leia a seção relevante antes de mexer em um módulo (ex.: "FIPE híbrida", "Busca pública de anúncios"). Comunique-se em português brasileiro.

## Stack
Java 17, Spring Boot 4.1.x, Maven Wrapper (`./mvnw`), Spring MVC, Data JPA, Bean Validation, Lombok, Spring Modulith 2.1.x, PostgreSQL 17 (write + read), Redis 7.4, Flyway, springdoc. Pacote base `repasse.phcauto.backend`. Jackson 3 (`tools.jackson.*`).

## Estrutura de módulo
```text
<modulo>/
├── package-info.java            # @ApplicationModule
├── <Evento>.java / <Modulo>Facade.java   # só API intermodular real
└── internal/
    ├── core/{domain,gateway,usecase,validation,exception}/
    └── infrastructure/
        ├── adapter/in/web/{controller,request,response,validation,exception}/
        ├── adapter/out/{persistence,http,event}/
        ├── configuration/  security/  service/
```
- Crie só os subpacotes com responsabilidade concreta.
- A raiz do módulo não acumula DTOs HTTP.

## Regras de camada
- **Regra de negócio só no core** (domínio + casos de uso), sem Spring, HTTP, JPA ou Redis. O frontend não decide regra.
- Caso de uso = interface (`CriarAnuncio`) + implementação específica (`CriarAnuncioUseCase`), ligadas em `configuration/<Modulo>Configuration`.
- Gateway = porta no core com operações orientadas ao caso de uso (`existePorEmail`, `salvarCadastro`). Implementação na infraestrutura (`Jpa...Gateway`, `Jdbc...Gateway`), sem expor `JpaRepository` ou entidades.
- HTTP: `...Request`/`...Response` só no adapter web. No core: comandos/resultados (`...Command`, records).
- Controller por capacidade de negócio, `@Valid`, chama interface de caso de uso, devolve response próprio.
- **Proibido**: `BaseController`, `BaseService`, `BaseGateway`, CRUD genérico por antecedência, herança de controller.

## Persistência
- Write é a fonte de consistência e regra; read é projeção eventual via Modulith (`RowChanged` → `ReadModelListener`). Nunca dual-write.
- `*WriteRepository` no principal; `*ReadRepository` só SELECT.
- SQL direto, JDBC ou `@Modifying` bulk **não** publicam evento: se mutar dados projetados, trate a projeção.
- Associações JPA sempre `LAZY`; nunca `EAGER`, `CascadeType.ALL`/`PERSIST`. Preferir UUID escalar como FK. `JpaMappingArchitectureTests` vigia isso.
- `@Version`: `versao` em anúncios, `lock_version` nos demais. Para editar, carregue a entidade; não use `criar`.
- Transações curtas: chamadas HTTP externas, hashing e serialização fora da transação.
- Mudanças de esquema: ver skill `phc-flyway-migrations` (`hbm2ddl=validate`).

## Integrações e cache
- Redis é cache, nunca fonte única: se cair, faça fallback para PostgreSQL.
- Clientes HTTP externos em `adapter/out/http`, com timeout, cota, tratamento de 429/Retry-After e nada de segredo em log ou resposta.
- Segredos vêm de variáveis de ambiente (`application.yaml` com `${VAR:default}`); nunca versionar `.env`.

## Validação
```bash
cd backend
./mvnw -B -ntp -DskipTests package   # compila
./mvnw -B -ntp test                  # testes (precisa write/read acessíveis)
docker compose config --quiet        # se mexer no compose
```
- Build com `-DskipTests` não é teste passando: informe o que rodou e o que não rodou.
- Ao mudar arquitetura, execução ou configuração, atualize `backend/AGENTS.md`, `README.md` e `backend/docs/` correspondentes.
- Não criar commits/push a menos que o usuário peça.
