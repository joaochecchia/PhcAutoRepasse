# Contexto do projeto — PHC Auto

## Escopo e negócio

Este arquivo orienta o trabalho no diretório `backend/` e seus subdiretórios.
O repositório possui diretórios irmãos `backend/` e `frontend/`.

O responsável pelo projeto tem contrato com a PHC Auto e está construindo um site de repasse de carros, inspirado no Webmotors, com intenção de colocá-lo em produção. O modelo inicial fornecido pelo usuário está em `docs/modelo-inicial.dbml`. Ele descreve identidade, assinaturas, catálogo e vendas, incluindo perfis e pagamentos. Valores e regras dos planos e a conclusão da venda dentro do site ainda precisam de definição; não presumir essas decisões ou funcionalidades adicionais como aprovadas.

Comunique-se com o usuário em português brasileiro.

## Arquitetura

A arquitetura definida pelo usuário é Clean Architecture, com princípios SOLID e monólito modular. Preserve essa direção nas implementações.

Estado atual: há infraestrutura de execução, documentação OpenAPI e contratos de domínio em Java puro. Por solicitação do usuário, os modelos são classes abstratas com getters, os tipos/status são enums e os casos de uso são interfaces. Estão separados em `identidade`, `assinaturas`, `catalogo` e `vendas` dentro de `domain/model` e `domain/usecases`. Há cinco interfaces CRUD segregadas em `domain/usecases/crud` e uma base abstrata `Abstract<Model>CrudUseCase` por modelo, com métodos ainda abstratos. Os modelos não implementam CRUD. A chave composta `VeiculoCaracteristicaId` é um record de Java puro. Há 24 entidades JPA na infraestrutura e repositories separados de leitura/escrita, mas ainda não há implementações concretas de casos de uso ou endpoints de negócio. Consulte `docs/domain.md`; não apresentar os fluxos ou a modularização como concluídos.

Pacote base: `repasse.phcauto.backend`.

```text
src/main/java/repasse/phcauto/backend/
├── BackendApplication.java
├── domain/
│   ├── model/
│   └── usecases/
└── infra/
    ├── controller/
    ├── service/
    ├── gateway/
    ├── config/
    └── database/
        ├── entity/
        └── repository/
```

Algumas dessas pastas estão vazias e podem não aparecer em um clone do Git.

- Mantenha regras de negócio nos modelos e casos de uso, independentes de Spring, HTTP, JPA e Redis.
- Defina contratos necessários aos casos de uso nas camadas internas; implemente os adaptadores na infraestrutura.
- Separe modelos de domínio, entidades JPA e contratos HTTP quando essas implementações forem criadas.
- Use controllers para entrada HTTP e serviços de infraestrutura para responsabilidades técnicas.
- Aplique SOLID com responsabilidades claras e injeção de dependências, evitando abstrações sem necessidade.
- Ao introduzir módulos, delimite-os por capacidade de negócio, mantendo uma única aplicação. As áreas iniciais seguem os schemas do DBML; evite reorganizações amplas sem necessidade da tarefa.

## CRUD provisório

O CRUD completo criado para todas as classes do modelo de banco é uma estrutura inicial e **não é uma decisão permanente**. Nem toda entidade precisará de todas as operações de criar, buscar, atualizar, excluir e listar.

Conforme os requisitos e casos de uso reais forem definidos, ajuste ou remova interfaces e bases abstratas desnecessárias. Mantenha apenas as operações justificadas pelas necessidades do negócio, respeitando a segregação de interfaces e a responsabilidade única. Não preserve ou implemente CRUD completo apenas por existir uma tabela ou uma base abstrata correspondente, nem exponha automaticamente essas operações em endpoints.

## Stack e arquivos principais

- Java 17, Spring Boot 4.1.1 e Maven Wrapper (`./mvnw`).
- Spring MVC, Spring Data JPA, Bean Validation e Lombok.
- PostgreSQL 17 e Redis 7.4 no Compose.
- Flyway: `spring-boot-starter-flyway` e `flyway-database-postgresql`.
- Swagger/OpenAPI: `springdoc-openapi-starter-webmvc-ui` 3.1.1.
- `pom.xml`: dependências e build; conferir as versões atuais antes de alterá-las.
- `src/main/resources/application.yaml`: conexões e configuração do Spring.
- `infra/config/OpenApiConfig.java`: título, descrição e versão da API.
- `compose.yaml`, `Dockerfile` e `.dockerignore`: execução em contêineres.
- `../README.md`: instruções de execução para desenvolvedores (movido para a raiz do repositório).
- `docs/persistence.md`: entidades, repositories, transações e sincronização por eventos Modulith.

## Banco e migrations

- Scripts em `src/main/resources/db/migration`.
- Convenção: `V1__descricao.sql`, `V2__descricao.sql`, com versões únicas.
- Os beans `primaryFlyway` e `projectionFlyway` executam migrations em write e read. A autoconfiguração Spring está desativada para evitar duplicação. V1 cria as 20 tabelas; V2 por banco adiciona o registro Modulith ou adapta a projeção.
- Ambas as unidades JPA usam `hibernate.hbm2ddl.auto=validate`, configurado explicitamente em `PersistenceConfig`. Evolua o esquema por migrations. Cada unidade aguarda as próprias migrations antes de validar.
- Não reescreva migrations já aplicadas em ambientes compartilhados; adicione uma nova migration.
- Write usa `postgres_data`, read usa `postgres_projection_data` e Redis usa `redis_data` com AOF. A atualização do read é assíncrona pelo Modulith, nunca dual-write na transação de negócio.
- `infra/config/RedisConfig.java` habilita cache Redis com JSON, prefixo `phcauto:cache:` e TTL padrão de 10 minutos, configuráveis por `REDIS_CACHE_PREFIX` e `REDIS_CACHE_TTL`. Não há casos de uso cacheados automaticamente. O domínio permanece sem Spring; futuros DTOs concretos de cache devem ficar na infraestrutura.
- O teste `RedisIntegrationTests` é habilitado por `REDIS_INTEGRATION_TEST=true` e verifica a integração real; requer conexões acessíveis com PostgreSQL e Redis.
- Preserve os dados existentes. `docker compose down` mantém os volumes; `docker compose down -v` os apaga e não deve ser usado como limpeza rotineira.

## Executar com Docker

Execute os comandos a partir de `backend/`:

```bash
docker compose up -d --build
docker compose logs -f backend
```

O Compose constrói a imagem `phcauto-backend:local` e inicia backend, PostgreSQL write, PostgreSQL read independente e Redis. O backend aguarda os healthchecks dos dois serviços. A imagem tem etapas separadas para build e execução e roda com usuário sem privilégios de root. O build da imagem pula a execução dos testes.

Portas padrão no host: backend `8080`, PostgreSQL principal `5432`, read `5433`, Redis `6379`. As portas do banco e Redis estão vinculadas a `127.0.0.1`.

Houve conflito com a porta `6379` durante a configuração inicial. Verifique a disponibilidade atual; não interrompa serviços alheios ao projeto. Alternativa já utilizada:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose up -d --build
```

Dentro da rede Docker, o backend usa `postgres:5432`, `postgres-read:5432` e `redis:6379`, independentemente das portas publicadas no host. `BACKEND_PORT` altera a porta HTTP publicada.

## Executar fora do Docker e validar

Para executar o backend pela IDE ou Maven, suba apenas as dependências:

```bash
docker compose up -d --wait postgres-read redis
docker compose run --rm postgres-bootstrap
./mvnw spring-boot:run
```

Pare o backend do Compose se ele estiver usando a mesma porta. Ao usar portas alternativas para as dependências, configure também `POSTGRES_PORT`, `POSTGRES_READ_PORT` e `REDIS_PORT` no processo Java.

Comandos de validação conforme a mudança:

```bash
docker compose config --quiet
./mvnw -B -ntp -DskipTests package
./mvnw -B -ntp test
```

O teste de contexto usa `@SpringBootTest` e requer write e read acessíveis. `PERSISTENCE_INTEGRATION_TEST=true` habilita os testes reais dos 24 mapeamentos, eventos, recuperação e concorrência; use bancos isolados. A integração automática Spring/Docker Compose está desativada por padrão; inicie as dependências explicitamente. Não confunda build com testes pulados com execução bem-sucedida dos testes.

Swagger UI: `http://localhost:8080/swagger-ui.html`.
OpenAPI JSON: `http://localhost:8080/v3/api-docs`.
Ajuste a porta nesses endereços ao usar `BACKEND_PORT`. A documentação atualmente não possui operações de negócio.

## Separação de leitura e escrita via Spring Modulith

- Decisão atual do usuário: bancos independentes sincronizados por eventos Spring Modulith 2.1.1; não usar réplica física WAL nem triggers SQL.
- `WriteChangeIntegrator` captura callbacks de alterações JPA e publica `RowChanged` na mesma transação. O registro JDBC Modulith fica no write; rollback desfaz a publicação.
- `ReadModelListener` usa `@ApplicationModuleListener`. O projetor consulta o estado atual da origem sob lock por chave no read e faz upsert/delete idempotente. Eventos atrasados não reproduzem snapshots antigos.
- `*WriteRepository` usa o principal. `*ReadRepository` expõe apenas consultas com usuário SELECT. Um terceiro pool, `projectionDataSource`, permite ao sincronizador atualizar o read sem dar escrita aos repositories de consulta.
- A consistência é eventual por linha. Leituras que exigem consistência imediata e regras de negócio devem usar write. FKs e unicidades de negócio existem no write; no read foram removidas para permitir projeções assíncronas fora de ordem.
- `ProjectionReconciler` enfileira chaves existentes na inicialização (paginado); `PublicationRecovery` reenvia falhas. Pendências são retomadas no restart e publicações paradas são marcadas como falhas pelo monitor. Concluídas são removidas.
- SQL direto, JDBC e queries bulk `@Modifying` não publicam automaticamente. As operações `delete*InBatch` dos repositories foram adaptadas para preservar callbacks. Não introduzir mutações que contornem eventos sem tratar a projeção.
- `WRITE_DATABASE_URL`, `READ_DATABASE_URL`, `POSTGRES_READ_USER/PASSWORD` e `PROJECTION_USER/PASSWORD` configuram conexões. `PROJECTION_BOOTSTRAP` e `PROJECTION_RETRY_DELAY` controlam reconciliação e recuperação. Não usar `REPLICATION_PASSWORD` ou `REPLICA_STARTUP_TIMEOUT`.
- Migrations comuns: `db/migration`; exclusivas: `db/write` e `db/read`. Preserve a V1 aplicada; versões coincidentes exclusivas possuem históricos separados. Use versões livres nas duas bases para novas migrations comuns.
- `@Version` usa `versao` em anúncios e `lock_version` nos demais. Carregue entidades existentes para editar; não use `criar` para atualização.
- O volume do write é preservado. O novo read usa `postgres_projection_data`; o volume antigo da réplica física não é reutilizado nem apagado. Consulte `docs/persistence.md`.

## Configuração e deploy

- Fora do Compose, `DATABASE_URL`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`, `REDIS_HOST`, `REDIS_PORT` e `REDIS_PASSWORD` configuram as conexões.
- No Compose, a URL do banco e o host/porta do Redis são definidos para os serviços internos; veja `compose.yaml` antes de configurar serviços externos.
- O Redis local não exige senha. `REDIS_PASSWORD` configura o cliente Spring para um servidor que já tenha autenticação; não habilita senha no servidor do Compose.
- As credenciais padrão do PostgreSQL são para desenvolvimento local. Não versionar credenciais reais, `.env`, chaves ou certificados privados.
- O Compose lê `.env`; o Spring não carrega esse arquivo automaticamente ao executar pela IDE/Maven. Configure as variáveis no ambiente do Java.
- `SWAGGER_ENABLED` controla a exposição da documentação.
- HTTP é usado localmente. Foi discutido terminar HTTPS no proxy reverso ou plataforma de hospedagem, mantendo HTTP na rede interna; domínio, certificado e deploy ainda não foram configurados.

## Controllers, casos de uso e gateways

- Não criar `BaseController`, `BaseService` ou `BaseGateway` genéricos apenas para compartilhar CRUD. Essa abstração não representa uma decisão arquitetural deste projeto.
- Organizar controllers por capacidade e fluxo de negócio, por exemplo: cadastro, autenticação, anúncios, compras, pagamentos e assinaturas. Cada controller deve receber apenas os requests necessários, aplicar `@Valid`, chamar interfaces de casos de uso e devolver responses próprios.
- Reutilizar responsabilidades HTTP técnicas por composição: tratamento global de erros, paginação, autenticação, mapeamento e padronização de respostas. Não usar herança de controller para impor endpoints iguais a recursos diferentes.
- Casos de uso devem continuar como interfaces da camada interna. Suas implementações devem ser específicas por operação de negócio; não criar uma `BaseService` que imponha CRUD completo.
- Interfaces de gateway são portas da camada interna e devem declarar operações orientadas à necessidade do caso de uso, como `existePorEmail`, `buscarPorCpf`, `salvarCadastro` ou `buscarDadosParaCompra`.
- Implementações de gateway ficam na infraestrutura e podem usar repositories Spring Data. Não expor `JpaRepository`, entidades JPA ou detalhes de read/write aos casos de uso.
- Não criar `BaseGateway` CRUD por antecedência. Extrações compartilhadas só devem ocorrer quando existir repetição técnica real, estável e sem regras de negócio.
- O CRUD abstrato existente é provisório. Ao implementar fluxos reais, preferir contratos menores e remover operações que não sejam justificadas pelo negócio.

## Orientações de manutenção

- Leia os arquivos atuais antes de editar e preserve alterações do usuário.
- Faça alterações focadas na solicitação, respeitando a arquitetura definida.
- Verifique as mudanças com os checks pertinentes e informe limitações de validação.
- Mantenha este contexto e o README coerentes quando mudar arquitetura, execução ou configuração.
- O Codex não deve criar commits em nenhuma circunstância. Não executar `git commit`, `git commit --amend`, commits de merge, rebase que reescreva commits, criação de tags ou `git push`. As alterações devem permanecer no working tree para revisão e versionamento manual pelo usuário.

## Cadastro PF/PJ e complementos da compra

- PF: nome e email em `usuarios`; CPF e nascimento em `usuarios_pf`.
- PJ: nome da empresa em `usuarios_pj.nome_fantasia`, razão social e CNPJ no mesmo perfil; email de contato em `usuarios.email`.
- `usuarios.telefone` representa celular da PF ou número de contato da PJ. Senhas locais são hashes em `senha_hash`, nunca texto puro.
- `enderecos_usuario` armazena um endereço por usuário (PF ou PJ): CEP, cidade, bairro, rua, número, complemento e UF.
- `dados_compra_pf` contém RG, nome do pai, nome da mãe, naturalidade e gênero. `dados_compra_pj` contém inscrição estadual e regime tributário. São complementos opcionais do perfil, preenchidos na etapa de compra; não são snapshots de transações.
- Campos adicionados aceitam ausência para preservar usuários existentes e permitir preenchimento gradual. Isso não define todos como opcionais no formulário: a validação de cadastro completo e da etapa de compra deverá ficar nos futuros casos de uso. Complemento, filiação desconhecida e inscrição isenta precisam de tratamento adequado, sem dados fictícios.
- `identidades_externas` permite Google e Facebook no mesmo usuário, com unicidade de `(provedor, identificador_externo)` no write. A senha local pode ser nula. Nenhum token OAuth é persistido. O identificador deve vir de uma resposta autenticada do provedor; não vincular contas automaticamente pela coincidência de email.
- Esta entrega prepara a persistência, mas não implementa login OAuth, callbacks, credenciais dos provedores, endpoints ou casos de uso de cadastro/compra. O futuro fluxo deve exigir senha local válida ou identidade externa verificada, coletar email se o provedor não o fornecer e completar CPF/CNPJ/endereço antes da etapa que os exige. A criação dos vínculos deve ocorrer na mesma transação do usuário.
- V3 comum adiciona os campos e as quatro tabelas, preservando V1/V2. V4 exclusiva do write aplica FKs e unicidade; o read mantém a projeção assíncrona sem essas restrições. Próximas migrations comuns devem usar V5 ou superior.
- Os quatro novos modelos têm contratos de domínio, entidades JPA, repositories read/write e sincronização Modulith. Os casos de uso desses modelos são interfaces específicas em `domain/usecases/identidade`: salvar e consultar endereço/complementos por usuário, vincular identidade externa verificada e consultar por provedor/identificador. Não há implementações concretas nem CRUD completo para esses modelos. `domain` corresponde ao core da arquitetura, sem frameworks.
