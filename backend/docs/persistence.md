# Persistência e sincronização com Spring Modulith

## Fluxo

A aplicação usa dois bancos PostgreSQL 17 independentes. O banco `write` é a fonte dos dados; o banco `read` é uma projeção para consultas. Não há triggers SQL nem replicação física WAL.

```text
WriteRepository / dirty checking JPA
          ↓ mesma transação
Dados no write + evento no registro do Spring Modulith
          ↓ após commit
@ApplicationModuleListener
          ↓ transação própria no banco read
Inserção, atualização ou exclusão da projeção
          ↓
ReadRepository consulta
```

O domínio permanece em Java puro. Os eventos desta implementação são eventos técnicos de sincronização, não substituem futuros eventos de negócio dos módulos.

## Entidades e repositories

As 24 entidades estendem os modelos abstratos do domínio. Usuário, PF, PJ e endereço ficam em `usuarios/internal/infrastructure/entity`, com repositories read/write internos ao módulo; as demais continuam em `infra/database/entity/<area>`. As anotações JPA ficam na infraestrutura. O mesmo mapeamento de colunas é usado pelas duas unidades JPA.

- `*WriteRepository`: operações JPA na origem. Também deve ser usado para consultas que exigem leitura consistente logo após uma gravação.
- `*ReadRepository`: busca por ID, existência, contagem e listagem paginada no banco de leitura. Não expõe `save` ou `delete`.
- `EventAwareJpaRepository`: executa as exclusões em lote por entidade para preservar callbacks e eventos JPA. Essa escolha privilegia sincronização correta; não é um DELETE SQL em massa.

Use `criar(modelo)` para novos registros e carregue a entidade existente antes de chamar `atualizar(modelo)`. UUIDs podem ser gerados para raízes; detalhes exigem o identificador da entidade principal. Eventos de gateway usam sequência. `@Version` usa `versao` em anúncios e `lock_version` nas demais entidades.

O CRUD completo continua provisório. Repositories não são casos de uso nem endpoints. Não serialize entidades diretamente em HTTP, especialmente modelos com hashes de senha.

## Publicação, ordem e falhas

`WriteChangeIntegrator` captura insert, update e delete no Hibernate da unidade de escrita, incluindo dirty checking. Publica um `RowChanged` com identificador do evento, tabela de uma allowlist e chave do registro. O evento não carrega senha, dados pessoais ou o snapshot inteiro da linha.

O registro JDBC do Spring Modulith fica no write e participa da mesma transação JPA. Rollback desfaz tanto os dados quanto a publicação. O listener roda somente após commit.

`ReadModelProjector` abre uma transação com a credencial de projeção, obtém um advisory lock por tabela/chave e só então consulta o estado atual no write. Faz upsert quando a linha existe e delete quando ela não existe. Assim, eventos repetidos ou atrasados convergem para o estado atual sem restaurar snapshots antigos. O lock também serializa projetores de instâncias diferentes para a mesma chave.

O commit no read acontece antes do listener concluir. Se houver falha entre esse commit e a confirmação no registro de eventos, a entrega pode se repetir; o processamento é idempotente. Não existe transação distribuída entre os bancos nem garantia de exatamente uma entrega.

Falhas permanecem no registro. `PublicationRecovery` tenta novamente a cada 30 segundos. O monitor do Modulith marca como falhas publicações paradas por dois minutos. Publicações pendentes são reenviadas na inicialização; concluídas são removidas (`completion-mode=delete`). Há dois workers assíncronos e fila limitada, com pressão de retorno quando a fila enche. Dimensione pools e workers em conjunto; o padrão de escrita é 10 conexões.

Monitore as pendências no write:

```sql
SELECT status, count(*), min(publication_date) AS mais_antiga
FROM event_publication
GROUP BY status;
```

Uma alteração por SQL direto, JDBC, query nativa ou `@Modifying` não dispara callbacks Hibernate. Não use essas rotas para mutações de negócio sem publicar os eventos correspondentes. As migrations também não publicam eventos por si só.

## Reconciliação e consistência

Na inicialização, `ProjectionReconciler` percorre as chaves existentes dos dois bancos em páginas de 250 registros e publica eventos. Isso preenche um read novo com dados já existentes e permite remover linhas que deixaram de existir no write. A inicialização enfileira a carga; sua conclusão é assíncrona.

`PROJECTION_BOOTSTRAP=false` desativa essa varredura automática. O método `reconcile()` pode ser usado por uma rotina administrativa para reconstruir a projeção após intervenção manual. Ele não está exposto por HTTP. A varredura não equivale a um snapshot global atômico; alterações JPA concorrentes também geram eventos e convergem posteriormente.

A consistência é eventual, por linha. Uma transação que muda várias entidades pode aparecer parcialmente no read enquanto os eventos são processados. Regras de autorização, estoque, pagamento ou publicação que exigem consistência devem consultar o write.

FKs e unicidades de negócio são impostas no write. Uma migration exclusiva do read remove essas restrições da projeção para permitir chegadas fora de ordem e trocas de valores únicos sem bloqueio permanente. PKs, tipos, NOT NULL, CHECKs e índices independentes são mantidos; índices vinculados a constraints UNIQUE são removidos com elas. As consultas podem observar relações incompletas ou valores temporariamente duplicados durante a convergência. O read não recebe operações de negócio.

## Conexões e permissões

Existem dois servidores e três pools:

| Pool | Banco | Acesso |
| --- | --- | --- |
| `writeDataSource` | write | JPA de escrita e registro de eventos Modulith |
| `readDataSource` | read | Usuário de consulta com SELECT, conexão read-only |
| `projectionDataSource` | read | Migrations e atualização técnica da projeção |

`writeTransactionManager` é o padrão. `readTransactionManager` atende consultas JPA. `projectionTransactionManager` é JDBC e usado apenas pelo sincronizador. Consultas do read e atualizações da projeção usam credenciais distintas; desativar `readOnly` no JDBC não concede DML ao usuário de consulta.

| Variável | Padrão local |
| --- | --- |
| WRITE_DATABASE_URL | JDBC localhost, POSTGRES_PORT (5432), POSTGRES_DB (phcauto) |
| READ_DATABASE_URL | JDBC localhost, POSTGRES_READ_PORT (5433), POSTGRES_DB (phcauto) |
| POSTGRES_USER / POSTGRES_PASSWORD | phcauto / phcauto_local |
| POSTGRES_READ_USER / POSTGRES_READ_PASSWORD | phcauto_read / phcauto_read_local |
| PROJECTION_USER / PROJECTION_PASSWORD | phcauto_projection / phcauto_projection_local |
| WRITE_POOL_SIZE / READ_POOL_SIZE / PROJECTION_POOL_SIZE | 10 / 10 / 5 |
| PROJECTION_BOOTSTRAP | true |
| PROJECTION_RETRY_DELAY | 30s |

`DATABASE_URL` permanece como fallback da URL de escrita. No Compose, os hosts são `postgres` e `postgres-read`, ambos na porta interna 5432. As credenciais de projeção do Compose também administram o read para migrations; separe usuário de migration e privilégios mínimos de DML no deploy de produção. A aplicação exige ambos os bancos disponíveis na inicialização para migrations e validação. Uma indisponibilidade posterior do read mantém eventos pendentes até recuperação.

## Migrations

A V1 existente foi preservada. O Flyway é executado explicitamente por datasource:

- Write: `db/migration` + `db/write`. V2 cria o schema oficial do registro de eventos Modulith 2.1.1.
- Read: `db/migration` + `db/read`. V2 adapta as restrições da projeção.

Cada banco tem seu histórico Flyway. Versões podem coincidir entre pastas exclusivas de bancos diferentes; não podem se repetir dentro das localizações de um mesmo banco. Migrations comuns futuras devem usar uma versão livre em ambos os históricos. `spring.flyway.enabled=false` desliga apenas a autoconfiguração duplicada, não os beans explícitos. Hibernate valida o esquema nas duas unidades.

Regras de composição PF/PJ, detalhes por tipo de veículo e transições comerciais ainda dependem dos casos de uso. As FKs, unicidades e CHECK de preço da V1 continuam no write.

## Executar e migrar da topologia anterior

A partir de `backend/`:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose up -d --build
```

O Compose preserva `postgres_data` e cria `postgres_projection_data` para o read independente. **O antigo volume `postgres_read_data` não é reutilizado nem apagado.** Não tente iniciar seu conteúdo de standby como este novo read. Os dados existentes do write são carregados pelos eventos da reconciliação inicial. Os scripts antigos de WAL foram removidos; nenhum dado do principal é apagado.

Para IDE/Maven:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose up -d --wait postgres-read redis
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 docker compose run --rm postgres-bootstrap
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 ./mvnw spring-boot:run
```

`postgres-bootstrap` cria o usuário de consulta no read, inclusive em volumes já inicializados. Os bancos não usam mais `pg_basebackup`, streaming WAL ou `REPLICATION_PASSWORD`. Ambos retornam false para `pg_is_in_recovery()`.

## Validação

Com as dependências e o usuário de consulta preparados:

```bash
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 \
  PERSISTENCE_INTEGRATION_TEST=true REDIS_INTEGRATION_TEST=true ./mvnw -B -ntp test
```

A suíte cobre os 24 modelos e suas chaves, atualização/exclusão no read, concorrência otimista, permissões, rollback de dados/eventos, falha e reprocessamento, dirty checking, entrega atrasada sem regressão e reconstrução da projeção. O teste de falha cria e remove uma constraint temporária no read: execute em bancos isolados.

Referência: [Eventos e registro persistente do Spring Modulith](https://docs.spring.io/spring-modulith/reference/events.html).

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

## Cadastro local

A criação atômica e o evento de negócio UsuarioCriado estão descritos em [criacao-usuario.md](criacao-usuario.md). PATCH, consulta e exclusão coordenada estão em [agregado-usuario.md](agregado-usuario.md). As duas unidades JPA também escaneiam as entidades internas de usuarios; ProjectionTable preserva as mesmas tabelas e chaves. O registro de falhas inclui UsuarioCriado.
