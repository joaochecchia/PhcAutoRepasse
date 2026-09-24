# Backend PHC Auto

## Executar tudo com Docker

Requisito: Docker com Docker Compose. Java e Maven são fornecidos pela imagem de build.

```bash
docker compose up -d --build
```

Isso compila a imagem `phcauto-backend:local` e inicia backend, PostgreSQL e Redis. O backend aguarda os healthchecks do banco e do Redis antes de iniciar. A imagem usa Java 17 e executa a aplicação com usuário sem privilégios de root.

Se as portas do banco ou do Redis estiverem ocupadas:

```bash
POSTGRES_PORT=15432 REDIS_PORT=16379 docker compose up -d --build
```

Essas portas são as publicadas no host; o backend usa `postgres:5432` e `redis:6379` dentro da rede Docker. Configure `BACKEND_PORT` para alterar a porta HTTP publicada (padrão: 8080).

Para acompanhar a inicialização:

```bash
docker compose logs -f backend
```

## Desenvolver pela IDE ou Maven

Requisito adicional: Java 17. Suba apenas as dependências e execute o backend localmente:

```bash
docker compose up -d --wait postgres redis
./mvnw spring-boot:run
```

Se o backend do Compose já estiver rodando, pare-o com `docker compose stop backend` antes de usar a mesma porta na IDE. Para portas alternativas, exporte `POSTGRES_PORT` e `REDIS_PORT` também no ambiente do Java.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- PostgreSQL: localhost:5432, banco `phcauto`, usuário `phcauto`, senha local `phcauto_local`.
- Redis: localhost:6379, sem senha no ambiente local, acessível apenas pela interface local do host.

O Compose é iniciado explicitamente. A integração automática do Spring está desativada por padrão para permitir conexões externas sem depender de Docker.

## Conexões

Configure as variáveis no ambiente do processo Java e do Compose quando necessário:

| Variável | Padrão |
| --- | --- |
| POSTGRES_DB | phcauto |
| POSTGRES_USER | phcauto |
| POSTGRES_PASSWORD | phcauto_local |
| POSTGRES_PORT | 5432 |
| DATABASE_URL | jdbc:postgresql://localhost:5432/phcauto |
| REDIS_HOST | localhost |
| REDIS_PORT | 6379 |
| REDIS_PASSWORD | vazio |
| SWAGGER_ENABLED | true |

`DATABASE_URL` permite usar PostgreSQL externo. `REDIS_PASSWORD` configura o cliente para um Redis externo com autenticação; o Redis deste Compose não exige senha. As credenciais padrão são para desenvolvimento local; configure credenciais próprias no ambiente de produção.

O Compose lê `.env`, mas o Spring não carrega esse arquivo automaticamente: exporte as mesmas variáveis no terminal ou configure-as na IDE. Caso o backend rode em um contêiner na mesma rede, utilize os hosts `postgres:5432` e `redis:6379` nas conexões.

## Persistência e migrations

Os volumes nomeados `postgres_data` e `redis_data` preservam os dados ao recriar os contêineres. O Redis usa AOF. `docker compose down` mantém os volumes; `docker compose down -v` apaga os dados.

Crie migrations em `src/main/resources/db/migration`, no formato `V1__descricao.sql`. O Flyway as executa na inicialização; o Hibernate apenas valida o esquema (`ddl-auto: validate`).

## Redis e cache

A conexão usa Lettuce, configurado pelo Spring Boot. `infra/config/RedisConfig.java` habilita o Spring Cache com Redis, valores JSON (Jackson 3), chaves em texto, prefixo por aplicação/cache e sem armazenar valores nulos.

| Variável | Padrão | Uso |
| --- | --- | --- |
| REDIS_DATABASE | 0 | Banco lógico Redis |
| REDIS_CONNECT_TIMEOUT | 2s | Tempo limite para conectar |
| REDIS_TIMEOUT | 2s | Tempo limite dos comandos |
| REDIS_USERNAME | vazio | Usuário ACL de servidor externo |
| REDIS_SSL_ENABLED | false | TLS para servidor externo |
| REDIS_CACHE_TTL | 10m | Expiração padrão positiva das entradas de cache |
| REDIS_CACHE_PREFIX | phcauto:cache: | Prefixo não vazio das chaves de cache |

As chaves do Spring Cache seguem `phcauto:cache:<nome-do-cache>::<chave>`. O Compose repassa banco lógico, TTL e prefixo ao backend; autenticação/TLS externos são configuráveis ao executar fora desse Compose local.

A infraestrutura pode injetar `CacheManager` para cache ou `StringRedisTemplate` para operações com strings. O TTL e o prefixo acima se aplicam ao CacheManager; operações diretas com StringRedisTemplate devem definir seus próprios prefixos e expiração.

Ainda não há funcionalidade de negócio cacheada. Aplique cache somente onde houver necessidade, definindo também invalidação na atualização/exclusão. Mantenha anotações de cache na infraestrutura, sem acoplar o domínio ao Spring. Use DTOs concretos de cache na infraestrutura; os modelos abstratos não são objetos prontos para serialização. O serializador aceita tipos da infraestrutura e tipos Java usuais (java.lang, java.util, java.time, java.math). Não armazene hashes de senha ou modelos completos de usuário em caches públicos.

A validação de integração é opt-in e utiliza PostgreSQL e Redis reais nas conexões configuradas, cria uma chave única e a remove ao final:

```bash
POSTGRES_PORT=15432 REDIS_PORT=16379 REDIS_INTEGRATION_TEST=true ./mvnw test
```

O teste verifica leitura/escrita JSON de um DTO com data, TTL, prefixo e exclusão. Sem `REDIS_INTEGRATION_TEST=true`, esse teste específico é pulado.
