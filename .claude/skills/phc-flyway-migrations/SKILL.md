---
name: phc-flyway-migrations
description: Como criar migrations Flyway no PHC Auto (PostgreSQL write + read, diretórios comuns e exclusivos, projeção Modulith). Use sempre que for alterar esquema, criar tabela/coluna/índice, carregar dados de referência ou ajustar entidades JPA que exigem mudança no banco.
---

# Migrations Flyway — PHC Auto

Referência: seção "Banco e migrations" e "Separação de leitura e escrita" em `backend/AGENTS.md`, e `backend/docs/persistence.md`.

## Onde fica cada migration
| Diretório | Aplicado em | Use para |
|---|---|---|
| `backend/src/main/resources/db/migration` | write **e** read | tabelas, colunas, índices de consulta comuns |
| `backend/src/main/resources/db/write` | só write | FKs, unicidades de negócio, triggers de integridade, filas |
| `backend/src/main/resources/db/read` | só read | adaptações da projeção |

Os beans `primaryFlyway` e `projectionFlyway` executam as migrations (a autoconfiguração do Spring está desligada). FKs e unicidades de negócio existem **só no write**; no read elas são removidas porque a projeção chega fora de ordem.

## Numeração
1. Descubra a maior versão em uso nos **três** diretórios:
   ```bash
   ls backend/src/main/resources/db/{migration,write,read} | grep -oE '^V[0-9]+' | sort -t V -k2 -n | tail -1
   ```
2. Use a próxima versão livre. Uma migration comum precisa de uma versão livre nas duas bases.
3. Nome: `V<n>__descricao_em_snake_case_pt.sql` (ex.: `V41__escopo_carregado_catalogo_fipe.sql`).
4. Depois, atualize em `backend/AGENTS.md` a nota "Próximas versões devem ser posteriores à V<n>".

## Regras
- **Nunca edite migration já aplicada.** Corrija com uma nova.
- O JPA usa `hbm2ddl.auto=validate`: toda coluna/tipo da entidade precisa existir na migration, com tipo compatível.
- Use schemas explícitos (`catalogo.`, `identidade.`, …), como nas migrations atuais.
- Seja idempotente quando fizer sentido (`IF NOT EXISTS`, `ON CONFLICT DO NOTHING`) e preserve dados existentes. Para tornar uma coluna obrigatória, faça backfill antes do `NOT NULL`; se houver legado, use `CHECK ... NOT VALID` ou a regra só para novos registros, como na V9.
- Em tabelas grandes, prefira `CREATE INDEX CONCURRENTLY`. Ele não roda dentro de transação: avalie se cabe numa migration separada.
- Se uma tabela nova precisa aparecer no read, registre-a na projeção (`infra/database/sync/ProjectionTable` e afins) e garanta permissões SELECT para o usuário read e escrita para o usuário de projeção.
- Carga de dados em SQL contorna eventos Modulith: aplique a mesma carga nas duas bases (migration comum) ou dispare a reconciliação.
- Não use `docker compose down -v` para "resetar": isso apaga os volumes do usuário.

## Validação
```bash
cd backend
docker compose up -d --wait postgres-read redis && docker compose run --rm postgres-bootstrap
./mvnw -B -ntp -DskipTests package && ./mvnw spring-boot:run   # migrations + validate sobem sem erro
```
Informe se as migrations foram de fato aplicadas num banco ou apenas escritas.
