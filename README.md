git# PHC Auto — Backend

Backend do site de repasse de veículos da **PHC Auto**, inspirado no Webmotors. O projeto está em desenvolvimento, com arquitetura orientada a **Clean Architecture**, princípios **SOLID** e **monólito modular**.

## Estado do projeto

A infraestrutura de execução está configurada: aplicação Spring Boot, PostgreSQL write e read independente, Redis, Flyway, Swagger e Docker Compose. As 24 entidades JPA, 48 repositories (leitura/escrita) e a migration inicial estão implementados. O domínio contém modelos abstratos e contratos de casos de uso em Java puro, organizados por área de negócio.

Ainda não há endpoints de negócio nem implementações concretas dos casos de uso. A configuração de cache está disponível, mas nenhuma funcionalidade de negócio utiliza cache automaticamente.

As regras e valores dos planos, a exigência de assinatura para publicar e a conclusão da venda dentro do site ainda serão definidos.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 17 | Linguagem e execução |
| Spring Boot 4.1.1 | Inicialização e infraestrutura |
| Spring MVC e Bean Validation | API HTTP e validação na infraestrutura |
| Spring Data JPA | Integração com persistência relacional |
| Spring Modulith 2.1.1 | Eventos duráveis e sincronização da projeção read |
| PostgreSQL 17 | Banco de dados |
| Flyway | Versionamento do esquema |
| Redis 7.4, Lettuce e Spring Cache | Conexão Redis e cache |
| Springdoc 3.1.1 | OpenAPI e Swagger UI |
| Maven Wrapper | Build e testes |
| Docker e Docker Compose | Empacotamento e execução dos serviços |

As versões e dependências do backend estão no [pom.xml](backend/pom.xml).

## Início rápido com Docker

Requisito: Docker com o plugin Docker Compose. Não é necessário instalar Java ou Maven no host para esta opção.

Execute os comandos a partir do diretório `backend/`:

```bash
docker compose up -d --build
```

O Compose constrói a imagem `phcauto-backend:local` e inicia backend, PostgreSQL write, PostgreSQL read e Redis. O backend aguarda os healthchecks das dependências antes de iniciar. A imagem possui etapas separadas de build e execução, usa Java 17 e executa com usuário sem privilégios de root.

Acompanhe a inicialização:

```bash
docker compose logs -f backend
```

| Recurso | Endereço padrão |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| PostgreSQL principal no host | localhost:5432 |
| PostgreSQL read no host | localhost:5433 |
| Redis no host | localhost:6379 |

O Swagger ainda não lista operações de negócio porque os controllers não foram implementados.

### Portas ocupadas

Se PostgreSQL ou Redis já estiverem usando as portas padrão:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose up -d --build
```

O backend continuará usando `postgres:5432` e `redis:6379` na rede Docker. As variáveis acima alteram apenas as portas publicadas no host. `BACKEND_PORT` altera a porta HTTP publicada pelo Compose; ajuste também o endereço do Swagger quando necessário.

### Comandos úteis

```bash
# Estado dos serviços
docker compose ps

# Parar os serviços sem remover os contêineres
docker compose stop

# Remover os contêineres e a rede, preservando os volumes
docker compose down
```

Ao reconstruir a imagem, reutilize as mesmas variáveis de portas do comando de inicialização. Os comandos de estado, logs e parada não precisam delas.

## Desenvolvimento pela IDE ou Maven

Requisitos: Java 17 e Docker Compose para as dependências. O Maven Wrapper está incluído no projeto.

Se o backend do Docker estiver usando a porta 8080, pare-o antes de iniciar pela IDE:

```bash
docker compose stop backend
```

Suba apenas PostgreSQL e Redis e execute a aplicação:

```bash
docker compose up -d --wait postgres-read redis
docker compose run --rm postgres-bootstrap
./mvnw spring-boot:run
```

Com portas alternativas, use os mesmos valores no processo Java:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose up -d --wait postgres-read redis
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose run --rm postgres-bootstrap
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 ./mvnw spring-boot:run
```

Na IDE, execute `BackendApplication` e configure as variáveis no ambiente da execução. Os exemplos com variáveis antes do comando usam sintaxe Bash. No Windows, o wrapper também está disponível como `mvnw.cmd`.

A integração automática do Spring com Docker Compose está desativada por padrão; as dependências são iniciadas explicitamente.

## Arquitetura e estrutura

O repositório possui `backend/` e `frontend/`. Este README documenta o backend.

```text
backend/
├── AGENTS.md
├── Dockerfile
├── compose.yaml
├── pom.xml
├── docs/
│   ├── domain.md
│   └── modelo-inicial.dbml
└── src/
    ├── main/
    │   ├── java/repasse/phcauto/backend/
    │   │   ├── BackendApplication.java
    │   │   ├── domain/
    │   │   │   ├── model/{identidade,assinaturas,catalogo,vendas}/
    │   │   │   └── usecases/{crud,identidade,assinaturas,catalogo,vendas}/
    │   │   └── infra/
    │   │       ├── config/
    │   │       ├── controller/
    │   │       ├── service/
    │   │       ├── gateway/
    │   │       └── database/{entity,repository}/
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    └── test/java/repasse/phcauto/backend/
```

Algumas pastas de infraestrutura ainda estão vazias e podem não aparecer em um clone do Git.

| Área | Responsabilidades previstas no modelo |
| --- | --- |
| Identidade | Usuários, perfis PF/PJ e papéis |
| Assinaturas | Planos e histórico de assinaturas |
| Catálogo | Veículos, detalhes por tipo, características, anúncios e fotos |
| Vendas | Compras, pagamentos e eventos de gateway |

O domínio não depende de Spring, JPA, Redis ou Lombok. Seus modelos são classes abstratas com getters, os tipos e status são enums e os casos de uso são contratos. A infraestrutura concentra integrações e configurações técnicas.

As interfaces CRUD são segregadas por operação: criar, buscar, atualizar, excluir e listar. Existem bases abstratas por modelo para futuras implementações. **Esse CRUD completo é provisório:** cada entidade deverá manter somente as operações necessárias ao negócio. A existência de uma tabela não exige todas as operações nem sua exposição por API.

Consulte os [contratos de domínio](backend/docs/domain.md) e o [modelo DBML](backend/docs/modelo-inicial.dbml). As regras descritas nesses documentos ainda precisam ser implementadas; os contratos não as executam por si só.

## Configuração

A configuração do Spring está em [application.yaml](backend/src/main/resources/application.yaml).

### Aplicação e PostgreSQL

| Variável | Padrão | Uso |
| --- | --- | --- |
| BACKEND_PORT | 8080 | Porta HTTP publicada pelo Compose |
| POSTGRES_DB | phcauto | Nome do banco |
| POSTGRES_USER | phcauto | Usuário do banco |
| POSTGRES_PASSWORD | phcauto_local | Senha local |
| POSTGRES_PORT | 5432 | Porta do principal publicada no host e usada pelo Java local |
| POSTGRES_READ_PORT | 5433 | Porta da projeção publicada no host e usada pelo Java local |
| POSTGRES_READ_USER | phcauto_read | Usuário de leitura |
| POSTGRES_READ_PASSWORD | phcauto_read_local | Senha local de leitura |
| PROJECTION_USER | phcauto_projection | Usuário que atualiza a projeção |
| PROJECTION_PASSWORD | phcauto_projection_local | Senha local do projetor |
| PROJECTION_BOOTSTRAP | true | Reconciliação inicial dos dados existentes |
| PROJECTION_RETRY_DELAY | 30s | Intervalo de reprocessamento de falhas |
| WRITE_DATABASE_URL | URL do principal | Sobrescreve a conexão de escrita |
| READ_DATABASE_URL | jdbc:postgresql://localhost:5433/phcauto | Sobrescreve a conexão de leitura |
| DATABASE_URL | jdbc:postgresql://localhost:5432/phcauto | Sobrescreve a URL ao executar fora do Compose |
| SWAGGER_ENABLED | true | Habilita Swagger e OpenAPI |
| DOCKER_COMPOSE_ENABLED | false | Integração automática Spring/Compose |

Fora do Docker, a URL padrão é construída com `POSTGRES_PORT` e `POSTGRES_DB`. No Compose, a URL usa o host `postgres` e a porta interna `5432`.

### Redis

| Variável | Padrão | Uso |
| --- | --- | --- |
| REDIS_HOST | localhost | Host ao executar fora do Compose |
| REDIS_PORT | 6379 | Porta publicada no host e usada pelo Java local |
| REDIS_DATABASE | 0 | Banco lógico |
| REDIS_USERNAME | vazio | Usuário ACL de servidor externo |
| REDIS_PASSWORD | vazio | Senha de servidor externo |
| REDIS_SSL_ENABLED | false | TLS para servidor externo |
| REDIS_CONNECT_TIMEOUT | 2s | Tempo limite para conectar |
| REDIS_TIMEOUT | 2s | Tempo limite dos comandos |
| REDIS_CACHE_TTL | 10m | Expiração padrão positiva do cache |
| REDIS_CACHE_PREFIX | phcauto:cache: | Prefixo não vazio das chaves de cache |

No Compose, o backend usa `redis:6379`, e banco lógico, TTL e prefixo são repassados por variáveis. O servidor Redis local não exige senha; configurar `REDIS_PASSWORD` no cliente não habilita autenticação no servidor. Para serviços externos, ajuste as conexões e o Compose conforme o ambiente.

O Compose lê `.env`, mas o Spring não carrega esse arquivo automaticamente quando executado pela IDE ou pelo Maven. Nesse caso, exporte as variáveis ou configure-as na IDE. As credenciais padrão são de desenvolvimento; arquivos `.env`, segredos e certificados privados estão excluídos do Git.

## Persistência e migrations

O principal PostgreSQL utiliza `postgres_data`, a projeção utiliza `postgres_projection_data`, e Redis utiliza `redis_data` com persistência AOF. Os volumes preservam os dados ao recriar os contêineres. **`docker compose down -v` remove os volumes e apaga esses dados.**

Os beans explícitos Flyway executam migrations comuns de `src/main/resources/db/migration` em ambos os bancos e migrations específicas de `db/write` e `db/read` na base correspondente. Use nomes como:

```text
V1__criar_tabelas.sql
V2__adicionar_indice_anuncios.sql
```

Esses nomes ilustram a convenção. A migration `V1__criar_modelo_inicial.sql` já cria as tabelas; os demais exemplos não são scripts existentes. As duas unidades Hibernate usam `hibernate.hbm2ddl.auto=validate` em `PersistenceConfig`, deixando a evolução do esquema para o Flyway. Não altere migrations já aplicadas em ambientes compartilhados: crie uma nova versão.

## Cache

[RedisConfig.java](backend/src/main/java/repasse/phcauto/backend/infra/config/RedisConfig.java) configura Spring Cache com serialização JSON via Jackson 3, chaves em texto, expiração e sem armazenamento de valores nulos.

As chaves seguem o formato:

```text
phcauto:cache:<nome-do-cache>::<chave>
```

A infraestrutura pode usar `CacheManager` ou `StringRedisTemplate`. Prefixo e TTL padrão se aplicam ao CacheManager; operações diretas com StringRedisTemplate precisam definir suas próprias chaves e expiração.

Use DTOs concretos de cache na infraestrutura. O serializador aceita tipos desse pacote e tipos Java de `java.lang`, `java.util`, `java.time` e `java.math`. Os modelos abstratos não são objetos prontos para serialização. Ao aplicar cache a uma funcionalidade, defina também sua invalidação e evite armazenar dados sensíveis de usuários.

## Build e testes

Build sem executar testes:

```bash
./mvnw -B -ntp -DskipTests package
```

O Dockerfile também pula os testes durante o build da imagem.

O teste de contexto requer PostgreSQL write e read independente acessíveis. Para executar os testes com as portas padrão:

```bash
docker compose up -d --wait postgres-read redis
docker compose run --rm postgres-bootstrap
./mvnw -B -ntp test
```

O teste específico de integração Redis é habilitado com `REDIS_INTEGRATION_TEST=true`. Exemplo completo com portas alternativas:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose up -d --wait postgres-read redis
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose run --rm postgres-bootstrap
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 REDIS_INTEGRATION_TEST=true ./mvnw -B -ntp test
```

Esse teste verifica escrita e leitura JSON de um DTO com data, TTL configurado, prefixo e exclusão no Redis real. Usa uma chave única e a remove ao final. Sem a variável, apenas esse teste específico é pulado.

Para validar a estrutura do Compose:

```bash
docker compose config --quiet
```

## Deploy e manutenção

O Compose atual serve como ambiente de desenvolvimento. Domínio público, certificado e deploy de produção ainda não estão configurados. A abordagem discutida para produção é terminar HTTPS no proxy reverso ou na plataforma de hospedagem, mantendo HTTP na rede interna.

As orientações de arquitetura e manutenção estão em [AGENTS.md](backend/AGENTS.md). Atualize a documentação ao alterar a execução, os contratos ou as configurações do projeto.

## Sincronização via Spring Modulith

O banco de leitura é independente e atualizado por eventos persistidos do Spring Modulith 2.1.1. Alterações JPA no write registram eventos na mesma transação; após commit, um listener insere, atualiza ou remove os registros do read. Repositories de leitura continuam sem operações de escrita.

O sincronizador trata repetição e eventos atrasados, reprocessa falhas e faz uma reconciliação inicial dos dados existentes. SQL direto e `@Modifying` não disparam essa sincronização automaticamente. A consistência é eventual: use o write para decisões que exigem dados imediatamente consistentes.

O Compose preserva o volume do principal e usa o novo volume `postgres_projection_data`. O antigo volume da réplica física não é reutilizado nem apagado. A V1 existente permanece inalterada, e cada banco recebe sua migration V2 específica.

As entidades, conexões, permissões, transações, limitações e testes estão em [Persistência](backend/docs/persistence.md). Para executar a suíte completa com as dependências preparadas:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 \
  PERSISTENCE_INTEGRATION_TEST=true REDIS_INTEGRATION_TEST=true ./mvnw -B -ntp test
```

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
