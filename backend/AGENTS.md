# Contexto do projeto — PHC Auto

## Escopo e negócio

Este arquivo orienta o trabalho no diretório `backend/` e seus subdiretórios.
O repositório possui diretórios irmãos `backend/` e `frontend/`.

O responsável pelo projeto tem contrato com a PHC Auto e está construindo um site de repasse de carros, inspirado no Webmotors, com intenção de colocá-lo em produção. O modelo inicial fornecido pelo usuário está em `docs/modelo-inicial.dbml`. Ele descreve identidade, assinaturas, catálogo e vendas, incluindo perfis e pagamentos. Valores e regras dos planos e a conclusão da venda dentro do site ainda precisam de definição; não presumir essas decisões ou funcionalidades adicionais como aprovadas.

Comunique-se com o usuário em português brasileiro.

## Arquitetura

A arquitetura definida pelo usuário é Clean Architecture, com princípios SOLID e monólito modular. Preserve essa direção nas implementações.

Estado atual: há infraestrutura de execução, documentação OpenAPI e contratos de domínio em Java puro. Por solicitação do usuário, os modelos são classes abstratas com getters, os tipos/status são enums e os casos de uso são interfaces. Estão separados em `identidade`, `assinaturas`, `catalogo` e `vendas` dentro de `domain/model` e `domain/usecases`. Há cinco interfaces CRUD segregadas em `domain/usecases/crud` e uma base abstrata `Abstract<Model>CrudUseCase` por modelo, com métodos ainda abstratos. Os modelos não implementam CRUD. A chave composta `VeiculoCaracteristicaId` é um record de Java puro. Não há implementações concretas de casos de uso ou endpoints de negócio. Consulte `docs/domain.md`; não apresentar os fluxos ou a modularização como concluídos.

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
- `README.md`: instruções de execução para desenvolvedores.

## Banco e migrations

- Scripts em `src/main/resources/db/migration`.
- Convenção: `V1__descricao.sql`, `V2__descricao.sql`, com versões únicas.
- O Flyway executa as migrations na inicialização.
- Hibernate está com `ddl-auto: validate`; evolua o esquema por migrations, sem substituir por `update` ou `create`.
- Não reescreva migrations já aplicadas em ambientes compartilhados; adicione uma nova migration.
- PostgreSQL usa volume `postgres_data`; Redis usa volume `redis_data` e persistência AOF.
- `infra/config/RedisConfig.java` habilita cache Redis com JSON, prefixo `phcauto:cache:` e TTL padrão de 10 minutos, configuráveis por `REDIS_CACHE_PREFIX` e `REDIS_CACHE_TTL`. Não há casos de uso cacheados automaticamente. O domínio permanece sem Spring; futuros DTOs concretos de cache devem ficar na infraestrutura.
- O teste `RedisIntegrationTests` é habilitado por `REDIS_INTEGRATION_TEST=true` e verifica a integração real; requer conexões acessíveis com PostgreSQL e Redis.
- Preserve os dados existentes. `docker compose down` mantém os volumes; `docker compose down -v` os apaga e não deve ser usado como limpeza rotineira.

## Executar com Docker

Execute os comandos a partir de `backend/`:

```bash
docker compose up -d --build
docker compose logs -f backend
```

O Compose constrói a imagem `phcauto-backend:local` e inicia backend, PostgreSQL e Redis. O backend aguarda os healthchecks dos dois serviços. A imagem tem etapas separadas para build e execução e roda com usuário sem privilégios de root. O build da imagem pula a execução dos testes.

Portas padrão no host: backend `8080`, PostgreSQL `5432`, Redis `6379`. As portas do banco e Redis estão vinculadas a `127.0.0.1`.

Houve conflito com a porta `6379` durante a configuração inicial. Verifique a disponibilidade atual; não interrompa serviços alheios ao projeto. Alternativa já utilizada:

```bash
POSTGRES_PORT=15432 REDIS_PORT=16379 docker compose up -d --build
```

Dentro da rede Docker, o backend usa `postgres:5432` e `redis:6379`, independentemente das portas publicadas no host. `BACKEND_PORT` altera a porta HTTP publicada.

## Executar fora do Docker e validar

Para executar o backend pela IDE ou Maven, suba apenas as dependências:

```bash
docker compose up -d --wait postgres redis
./mvnw spring-boot:run
```

Pare o backend do Compose se ele estiver usando a mesma porta. Ao usar portas alternativas para as dependências, configure também `POSTGRES_PORT` e `REDIS_PORT` no processo Java.

Comandos de validação conforme a mudança:

```bash
docker compose config --quiet
./mvnw -B -ntp -DskipTests package
./mvnw -B -ntp test
```

O teste atual usa `@SpringBootTest` e requer PostgreSQL acessível com a configuração correspondente. A integração automática Spring/Docker Compose está desativada por padrão; inicie as dependências explicitamente. Não confunda build com testes pulados com execução bem-sucedida dos testes.

Swagger UI: `http://localhost:8080/swagger-ui.html`.
OpenAPI JSON: `http://localhost:8080/v3/api-docs`.
Ajuste a porta nesses endereços ao usar `BACKEND_PORT`. A documentação atualmente não possui operações de negócio.

## Configuração e deploy

- Fora do Compose, `DATABASE_URL`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`, `REDIS_HOST`, `REDIS_PORT` e `REDIS_PASSWORD` configuram as conexões.
- No Compose, a URL do banco e o host/porta do Redis são definidos para os serviços internos; veja `compose.yaml` antes de configurar serviços externos.
- O Redis local não exige senha. `REDIS_PASSWORD` configura o cliente Spring para um servidor que já tenha autenticação; não habilita senha no servidor do Compose.
- As credenciais padrão do PostgreSQL são para desenvolvimento local. Não versionar credenciais reais, `.env`, chaves ou certificados privados.
- O Compose lê `.env`; o Spring não carrega esse arquivo automaticamente ao executar pela IDE/Maven. Configure as variáveis no ambiente do Java.
- `SWAGGER_ENABLED` controla a exposição da documentação.
- HTTP é usado localmente. Foi discutido terminar HTTPS no proxy reverso ou plataforma de hospedagem, mantendo HTTP na rede interna; domínio, certificado e deploy ainda não foram configurados.

## Orientações de manutenção

- Leia os arquivos atuais antes de editar e preserve alterações do usuário.
- Faça alterações focadas na solicitação, respeitando a arquitetura definida.
- Verifique as mudanças com os checks pertinentes e informe limitações de validação.
- Mantenha este contexto e o README coerentes quando mudar arquitetura, execução ou configuração.
