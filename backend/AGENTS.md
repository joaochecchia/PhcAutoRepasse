# Contexto do projeto — PHC Auto

## Escopo e negócio

Este arquivo orienta o trabalho no diretório `backend/` e seus subdiretórios.
O repositório possui diretórios irmãos `backend/` e `frontend/`.

O responsável pelo projeto tem contrato com a PHC Auto e está construindo um site de repasse de carros, inspirado no Webmotors, com intenção de colocá-lo em produção. O modelo inicial fornecido pelo usuário está em `docs/modelo-inicial.dbml`. Ele descreve identidade, assinaturas, catálogo e vendas, incluindo perfis e pagamentos. Valores e regras dos planos e a conclusão da venda dentro do site ainda precisam de definição; não presumir essas decisões ou funcionalidades adicionais como aprovadas.

Comunique-se com o usuário em português brasileiro.

## Arquitetura

A arquitetura definida pelo usuário é Clean Architecture, com princípios SOLID e monólito modular. Preserve essa direção nas implementações.

Estado atual: há infraestrutura de execução, documentação OpenAPI e contratos de domínio em Java puro. Por solicitação do usuário, os modelos são classes abstratas com getters, os tipos/status são enums e os casos de uso são interfaces. Estão separados em `identidade`, `assinaturas`, `catalogo` e `vendas` dentro de `domain/model` e `domain/usecases`. Há cinco interfaces CRUD segregadas em `domain/usecases/crud` e uma base abstrata `Abstract<Model>CrudUseCase` por modelo, com métodos ainda abstratos. Os modelos não implementam CRUD. A chave composta `VeiculoCaracteristicaId` é um record de Java puro. As entidades JPA e os repositories de identidade, compliance, catálogo e planos ficam nos respectivos módulos. A entidade de assinatura e as entidades de vendas ainda permanecem na infraestrutura transitória. Há quatro controllers CRUD provisórios ocultos para essas áreas ainda não implementadas. A criação real de usuários está em `usuarios` e a criação real de anúncios está em `anuncios`, ambos com contratos e eventos públicos na raiz, core puro e controllers/adaptadores em `internal/infrastructure`. Os demais endpoints CRUD continuam provisórios. Consulte `docs/domain.md`; não apresentar os fluxos ou a modularização como concluídos.

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

O teste de contexto usa `@SpringBootTest` e requer write e read acessíveis. `PERSISTENCE_INTEGRATION_TEST=true` habilita os testes reais dos 26 mapeamentos, eventos, recuperação e concorrência; use bancos isolados. A integração automática Spring/Docker Compose está desativada por padrão; inicie as dependências explicitamente. Não confunda build com testes pulados com execução bem-sucedida dos testes.

Swagger UI: `http://localhost:8080/swagger-ui.html`.
OpenAPI JSON: `http://localhost:8080/v3/api-docs`.
Ajuste a porta nesses endereços ao usar `BACKEND_PORT`. A documentação expõe os fluxos reais de usuários, anúncios e planos; os quatro CRUDs provisórios de assinatura e vendas permanecem ocultos.

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
- Os controllers legados provisórios restantes retornam apenas um envelope `HashMap` com as chaves `menssage` e `Body`; não injetam casos de uso, gateways ou repositories. O controller legado de usuário e os endpoints diretos de PF, PJ e endereço foram removidos; essas operações passam pelo agregado exposto por `UsuariosController` em `usuarios/internal/infrastructure/controller`. Não interpretar as mensagens dos controllers legados como persistência concluída.
- `CrudHttpResponseFactory` centraliza apenas a montagem técnica do envelope e remove senha, hashes, tokens e segredos do corpo devolvido. Ele não é um `BaseController` e não contém regras de negócio.
- A aplicação usa @Modulithic e detecção explicitly-annotated. Assinatura e vendas ainda são módulos transitórios; usuarios, anuncios, compliance e planos são módulos por capacidade, com entidades, repositories e adaptadores em internal/infrastructure. Identidade e catálogo não mantêm módulos HTTP legados paralelos.

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
- Campos adicionados aceitam ausência para preservar usuários existentes e permitir preenchimento gradual. Isso não define todos como opcionais no formulário: o cadastro local completo já é validado no core de `usuarios`; a etapa de compra continua pendente. Complemento, filiação desconhecida e inscrição isenta precisam de tratamento adequado, sem dados fictícios.
- `identidades_externas` permite Google e Facebook no mesmo usuário, com unicidade de `(provedor, identificador_externo)` no write. A senha local pode ser nula. Nenhum token OAuth é persistido. O identificador deve vir de uma resposta autenticada do provedor; não vincular contas automaticamente pela coincidência de email.
- O cadastro local PF/PJ está implementado no módulo `usuarios`; login OAuth, callbacks, credenciais dos provedores e fluxos de compra permanecem pendentes. O futuro fluxo deve exigir senha local válida ou identidade externa verificada, coletar email se o provedor não o fornecer e completar CPF/CNPJ/endereço antes da etapa que os exige. A criação dos vínculos deve ocorrer na mesma transação do usuário.
- V3 comum adiciona os campos e as quatro tabelas, preservando V1/V2. V4 exclusiva do write aplica FKs e unicidade. V5 comum garante documentos numéricos. V6 comum cria `catalogo.enderecos_anuncio`, migra cidade/UF e torna `anuncios.endereco_id` obrigatório; V7 exclusiva adiciona FK no write e remove a unicidade da projeção read. V8 comum adiciona os campos e índices necessários aos filtros do catálogo. V9 exclusiva do write reforça os dados técnicos obrigatórios de novos anúncios sem invalidar registros legados. V10 comum adiciona índices funcionais da busca pública. Próximas migrations devem usar V11 ou superior.
- `catalogo.enderecos_anuncio` usa o mesmo conjunto de dados postais de `identidade.enderecos_usuario`: CEP, cidade, bairro, rua, número, complemento e UF. A chave identifica o endereço do anúncio, e `anuncios.endereco_id` mantém o relacionamento com a oferta.
- No futuro frontend de criação do anúncio, perguntar se o veículo está no mesmo endereço do usuário. Se estiver, copiar os dados para o endereço próprio do anúncio; se não estiver, solicitar outro endereço. Não vincular o anúncio diretamente ao endereço do usuário, pois o anunciante pode publicar um veículo localizado longe dele e o endereço do anúncio precisa preservar seu próprio estado.
- Por privacidade e segurança, respostas públicas e a visualização inicial do anúncio devem expor somente a cidade do veículo. CEP, bairro, rua, número, complemento e o endereço completo não devem ser enviados no DTO público inicial; qualquer liberação posterior exige um caso de uso e regra de autorização explícitos.
- Os quatro novos modelos têm contratos de domínio, entidades JPA, repositories read/write e sincronização Modulith. Os casos de uso desses modelos são interfaces específicas em `domain/usecases/identidade`: salvar e consultar endereço/complementos por usuário, vincular identidade externa verificada e consultar por provedor/identificador. O endereço participa da criação concreta de usuário; não há CRUD completo nem fluxos concretos de compra/vínculo externo para esses modelos. `domain` corresponde ao core da arquitetura, sem frameworks.

## Preparação de eventos do CRUD de usuário

- Consulte `docs/eventos-usuario.md`. `UsuarioAlterado` e `PublicarEventoUsuarioGateway` são contratos Java puros; o adaptador Spring exige transação write existente e gravável.
- Cadastro completo deve salvar usuário + PF **ou** PJ + endereço na mesma transação, com identidade externa quando aplicável. Não criar esses registros essenciais em listeners assíncronos.
- `UsuarioAlterado` sinaliza CADASTRADO, ATUALIZADO ou EXCLUIDO após a operação lógica; publicar antes do commit via porta interna. RowChanged continua responsável pela projeção das linhas.
- O cadastro local já está conectado no módulo `usuarios`; não existem consumidores de negócio em produção para UsuarioCriado. O registro Modulith só persiste entregas para listeners transacionais existentes; não usar eventos como armazenamento para consumidores futuros.
- A recuperação inclui RowChanged e UsuarioAlterado. Listeners futuros precisam de identificadores estáveis e idempotência. Workers e capacidade da fila são configuráveis por MODULITH_EVENT_WORKERS e MODULITH_EVENT_QUEUE_CAPACITY.

## Criação concreta no módulo usuarios

- API pública na raiz `usuarios`; core Java puro em `usuarios/internal/core`; entidades, repositories read/write e adaptadores em `usuarios/internal/infrastructure`.
- `CriarUsuarioUseCase.execute` é implementado por `CriarUsuario`. `UsuariosFacade` é uma interface pública sem Spring; `DefaultUsuariosFacade`, em `internal/infrastructure/service`, delimita a transação write; `JpaUsuarioGateway` grava usuário, PF ou PJ e endereço. Consultas de unicidade usam write. Concorrência também é protegida por constraints.
- O caso de uso publica por `PublicarUsuarioCriadoGateway`; o adaptador usa ApplicationEventPublisher. Não importar Spring no core.
- POST `/api/v1/usuarios` retorna UsuarioResponse, 201, sem senha/hash. Novo contrato descrito em `docs/criacao-usuario.md`. Senha só no request/comando e persistida como PBKDF2; papel fixo CLIENTE.
- Evento público `usuarios.UsuarioCriado` e RowChanged são publicados na transação. A recuperação aceita ambos e UsuarioAlterado. Não publicar UsuarioAlterado(CADASTRADO) adicionalmente no novo fluxo.
- Login local de verificação de credenciais está implementado; sessão/token, OAuth, atualização, exclusão e efeitos posteriores continuam pendentes. Não criar listeners vazios nem registros essenciais de cadastro de forma assíncrona.
- `USUARIOS_INTEGRATION_TEST=true` habilita integração real de cadastro; usar bancos isolados. Nenhuma migration foi alterada para mover as entidades.

## Agregado de usuário

- Consulte `docs/agregado-usuario.md`. Usuário é a única raiz HTTP: POST, GET, PATCH e DELETE somente em `/api/v1/usuarios`; não criar aliases duplicados.
- PF, PJ e endereço não possuem controllers próprios. Toda mutação ocorre pelos casos de uso do agregado e na mesma transação write.
- PATCH altera apenas campos enviados, não permite trocar PF/PJ e mantém nome/nome_fantasia de PJ coerentes. Campos nulos ficam inalterados; string vazia limpa somente número/complemento.
- DELETE remove endereço e perfil antes de usuário. Qualquer FK externa bloqueia e reverte tudo; não há exclusão parcial.
- JPA publica `RowChanged` somente para linhas efetivamente alteradas; a projeção read converge após commit. GET agregado usa write para consistência imediata.
- Casos de uso e gateways de buscar, atualizar e excluir são segregados no core. `JpaUsuarioAggregateGateway` é o adaptador interno.
- Os controllers provisórios de identidade e catálogo foram removidos após os fluxos reais migrarem para usuarios e anuncios. Restam quatro CRUDs provisórios de assinatura e vendas, marcados com @Hidden; remova @Hidden somente quando o respectivo fluxo de negócio estiver implementado e aprovado.

## Login local

- POST `/api/v1/usuarios/login` recebe `usuarios.request.LoginRequest` e retorna `usuarios.response.LoginResponse` somente com mensagem, HTTP 200. Request/response são NamedInterfaces públicas.
- `LoginUseCase.execute(LoginRequest)` é implementado por `Login`, com dependência injetada exclusivamente em `AutenticacaoGateway`; o core não importa Spring/JPA.
- `DatabaseAutenticacaoGateway` consulta write em transação read-only e valida hash/status; não consultar a projeção para autenticação. Senha nunca é comparada como texto puro.
- PasswordEncoder compartilhado mantém PBKDF2 e aceita BCrypt identificado. Conta inexistente/inativa/sem senha local, hash inválido e email ambíguo retornam a mesma CredenciaisInvalidasException (401).
- O sucesso não estabelece sessão ou token. OAuth2 e Spring Security HTTP continuam pendentes; ver `docs/login.md`.

## Criação concreta no módulo anuncios

- Consulte `docs/criacao-anuncio.md`. POST `/api/v1/anuncios` cria veículo, exatamente uma especialização, endereço próprio, motores de barco quando houver e anúncio na mesma transação write.
- Tipos atendidos: CARRO, MOTO, CAMINHAO, CAMINHONETE, BARCO e LINHA_AMARELA. O core rejeita bloco específico ausente, duplicado ou incompatível com o tipo.
- `CriarAnuncioUseCase.execute` é implementado por `CriarAnuncio` e depende somente de `CriarAnuncioGateway` e `PublicarAnuncioCriadoGateway`. Spring, JPA e HTTP ficam em `anuncios/internal/infrastructure`.
- `publicarAgora=true` cria PUBLICADO e define `publicadoEm`; falso ou ausente cria RASCUNHO. FIXO exige preço positivo e SOB_CONSULTA não aceita preço.
- O anunciante precisa existir e estar ativo. Até a autenticação HTTP ser implementada, o ID vem no request; futuramente deve vir da identidade autenticada.
- `AnuncioResponse` expõe somente a cidade da localização. O endereço completo é persistido, mas não integra a resposta pública.
- `AnuncioCriado` é o evento público de negócio. `RowChanged` continua sendo produzido pelos callbacks JPA e consumido pelo projetor Modulith para sincronizar todas as linhas no read. Registros essenciais não são criados assincronamente.
- `ANUNCIOS_INTEGRATION_TEST=true` habilita o teste real dos seis tipos e da projeção; use bancos isolados.

## Manutenção do agregado de anúncio

- `PATCH /api/v1/anuncios/{anuncioId}` e `DELETE /api/v1/anuncios/{anuncioId}` estão implementados no módulo `anuncios`; PF/PJ/endereço de usuário continuam fora desse fluxo.
- O PATCH é parcial também para veículo, endereço e especialização, não permite trocar tipo/anunciante/IDs e preserva o status quando `publicarAgora` não é enviado.
- O DELETE remove dependências próprias do catálogo na mesma transação e é bloqueado quando há compra vinculada. Use remoções individuais pelos repositories para preservar callbacks `RowChanged`; não introduzir bulk delete sem projeção equivalente.
- Eventos públicos `AnuncioAtualizado` e `AnuncioExcluido` são publicados na transação write. A projeção read continua sendo sincronizada pelos eventos técnicos de linha.

## Rollback dos agregados

- As fachadas concretas de `usuarios` e `anuncios` abrem transações no `writeTransactionManager`; gateways JPA e adaptadores de publicação exigem `Propagation.MANDATORY`.
- Usuário/perfil/endereço e anúncio/veículo/especialização/endereço/dependências são atômicos. Qualquer falha deve reverter todas as linhas e os registros Modulith criados na transação.
- A projeção read recebe somente eventos após commit. Nunca capturar uma exceção de persistência e continuar a transação como se a operação tivesse sucesso.
- Há testes reais, habilitados pelas variáveis de integração, que forçam rollback de criação, PATCH e DELETE e verificam ausência de persistência parcial.

## Desempenho JPA e transações

- Associações JPA futuras devem declarar `FetchType.LAZY`; buscar grafos necessários com `JOIN FETCH`, projeção ou consulta específica. Nunca introduzir `EAGER`.
- Não usar `CascadeType.ALL` nem `CascadeType.PERSIST`. Persistir dependências manualmente, em ordem, dentro da transação do agregado.
- Os modelos atuais usam UUIDs escalares para FKs e não possuem associações JPA, eliminando carregamento implícito e N+1 por navegação.
- Manter transações curtas. Validação HTTP, serialização, chamadas externas e cálculos caros devem ocorrer fora da transação quando não dependem de consistência do banco. No login, somente a leitura das credenciais é transacional; PBKDF2/BCrypt ocorre depois.
- `JpaMappingArchitectureTests` deve continuar falhando se surgir relação EAGER ou cascade ALL/PERSIST.

## Campos de filtro do catálogo

- V8 comum adiciona `condicao` (`ZERO_KM`/`USADO`) e `tipo_freio` aos veículos; direção e cilindrada em litros para carros/caminhonetes; direção para caminhões; e blindagem para caminhonetes. Registros legados podem permanecer nulos, mas novos anúncios exigem condição.
- Marca usa `veiculos.fabricante`; cidade/UF usam `enderecos_anuncio`; preço usa `anuncios.preco_centavos`; ano usa `veiculos.ano_modelo`/`ano_fabricacao`; câmbio, combustível, tração, carroceria, IPVA e portas já ficam nas tabelas específicas. Motos usam `cilindradas` em cc.
- Perfil do anunciante vem do relacionamento `anuncios.anunciante_id`: PF usa `usuarios.nome`; PJ pode usar `usuarios_pj.nome_fantasia`, com `usuarios.tipo_pessoa` para distinguir o perfil.
- A migration prepara persistência e índices. O caso de uso/endpoint de pesquisa ainda deve ser criado com filtros opcionais e paginação; não expor entidades JPA diretamente.

## Obrigatoriedade técnica dos anúncios

- Todo novo anúncio exige ano de fabricação, ano do modelo e condição. Para carro, moto, caminhão e caminhonete, o core também exige os dados técnicos definidos por tipo; a regra é aplicada após a mesclagem do PATCH.
- Carro: quilometragem, carroceria, câmbio, combustível, motorização, cilindrada em litros e portas. Moto: quilometragem, cilindradas, categoria, câmbio e combustível. Caminhão: quilometragem, configuração, carroceria, câmbio, combustível e eixos. Caminhonete: quilometragem, cabine, carroceria, câmbio, combustível, motorização, cilindrada em litros e portas.
- Veículo `ZERO_KM` deve ter quilometragem zero. Os demais campos técnicos continuam opcionais.
- V9 do write cria constraints `NOT VALID`: novas gravações inválidas são bloqueadas, enquanto anúncios legados incompletos são preservados até serem corrigidos. Não preencher legado com dados fictícios.

## Busca pública de anúncios

- Consulte `docs/busca-anuncios.md`. GET `/api/v1/anuncios` usa o banco read e retorna somente anúncios `PUBLICADO`, com paginação limitada a 100 itens.
- Os filtros opcionais cobrem tipo, localização pública, marca, perfil PF/PJ, nome de pessoa/loja, faixas de preço/ano e dados técnicos. Comparações categóricas textuais ignoram maiúsculas/minúsculas; perfil usa correspondência parcial.
- O caso de uso e gateway estão em `anuncios/internal/core`; o adaptador JDBC parametrizado fica na infraestrutura. A resposta HTTP é `ResponseEntity<HashMap<String,Object>>` com `mensagem`, `carros`, `total`, `pagina` e `tamanho`.
- Por privacidade, a lista retorna cidade e UF, nunca CEP, bairro, rua, número ou complemento. A chave `carros` pode conter qualquer tipo de veículo por decisão explícita do contrato atual.


## Compliance e restrição etária

- O módulo `compliance` registra o comprovante do aceite no mesmo fluxo transacional de `POST /api/v1/usuarios`.
- O request de cadastro exige `aceitouTermos=true`, `aceiteTermosEm`, `versaoTermosUso` e `versaoPoliticaPrivacidade`. O IP/identificador de rede vem de `HttpServletRequest.getRemoteAddr()`, nunca do corpo enviado pelo cliente.
- As versões aceitas precisam coincidir com `TERMOS_USO_VERSAO_ATUAL` e `POLITICA_PRIVACIDADE_VERSAO_ATUAL`; os defaults locais são `1.0`. Atualize as variáveis junto com a publicação dos documentos.
- `compliance.aceites_termos` preserva usuário, instante declarado do clique, versões, endereço de rede e instante de registro no servidor. Não existe endpoint público de leitura desses dados.
- Usuários PF precisam ter pelo menos 18 anos completos tanto no cadastro quanto ao alterar a data de nascimento. PJ não possui idade; eventual idade do representante exige requisito e campo próprios.
- A gravação do aceite usa a transação write aberta pela fachada de usuários com propagação obrigatória. Falha em compliance reverte usuário, perfil, endereço e eventos.
- V11 comum cria o schema e a tabela de compliance. A tabela participa da projeção read por `RowChanged`. Próximas migrations devem usar V12 ou superior.


## Módulo de planos

- `planos` é um módulo independente do Spring Modulith. A raiz contém requests, responses e `PlanosFacade`; regras puras ficam em `planos/internal/core`; controller, fachada transacional, entidade e repositories ficam em `planos/internal/infrastructure`.
- O CRUD real usa `/api/v1/assinaturas/planos`: POST cria, GET por ID consulta, GET lista por `offset`/`limite`, PATCH altera parcialmente e DELETE remove. O antigo controller provisório com PUT foi removido.
- Nome é obrigatório, normalizado e único. Valor e limite não podem ser negativos; período, quando informado, deve estar entre 1 e 32767 meses. Campos opcionais ausentes no PATCH são preservados; o contrato atual não usa `null` para limpar valores.
- Todas as operações usam o banco write para consistência imediata. `RowChanged` continua projetando a tabela no read. Exclusão de plano referenciado por assinatura retorna conflito e faz rollback.
- Não adicionar dependências de outros módulos em `planos` antecipadamente. Futuras permissões e integrações devem consumir somente a API pública do módulo.

## Benefícios futuros de planos e assinaturas

- V12 comum adiciona `planos.limite_vistorias_cautelares` e os snapshots `assinaturas.limite_anuncios_contratado` e `assinaturas.limite_vistorias_cautelares_contratado`. Próximas migrations devem usar V13 ou superior.
- `planos.limite_anuncios` e `planos.limite_vistorias_cautelares` descrevem benefícios comerciais. Nulo significa ainda não definido, zero significa que o benefício não está incluído e valor positivo representa o limite oferecido.
- Ao implementar o futuro módulo de assinaturas, copie preço e limites do plano para a assinatura no momento da contratação. Mudanças posteriores no plano não devem alterar benefícios já contratados.
- Esses campos não aplicam limites atualmente. Não bloquear criação/publicação de anúncios, não criar consumo de vistoria e não presumir renovação, saldo ou permissão até os respectivos casos de uso serem definidos.
- Contadores de uso não ficam na linha da assinatura nesta preparação. O futuro fluxo deve definir eventos/registros próprios para consumo e estorno, especialmente para vistorias cautelares.
