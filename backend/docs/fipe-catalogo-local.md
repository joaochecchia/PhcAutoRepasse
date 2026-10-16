# Catálogo FIPE local

## Decisão e limites

As requisições do formulário passam somente pelo backend. A fonte persistente é PostgreSQL; Redis é um cache compartilhado e descartável. Quando a persistência estiver autorizada e um escopo ainda não existir localmente, o backend pode preenchê-lo sob demanda pela Parallelum. Sem autorização, não faz essa consulta. As consultas por ID usadas para validar anúncios continuam lendo o write para consistência transacional. O snapshot FIPE já existente nos veículos foi preservado.

Em 16/10/2026, o usuário solicitou `FIPE_PERSISTENCE_AUTHORIZED=true` como padrão, com contratação do plano premium planejada para depois. Não foi feita carga de dados reais nem varredura da API nesta alteração. Um catálogo vazio permite cadastro manual sem FIPE. A flag não concede licença; antes de operar com dados reais, conferir se os termos do plano contratado permitem a persistência e o uso comercial pretendidos.

Referências verificadas: [documentação oficial](https://fipe.api.br/docs/api/fipe) e [termos](https://fipe.api.br/termos-de-uso). A documentação especifica `X-Subscription-Token`, base fixa `https://fipe.parallelum.com.br/api/v2`, tipos `cars`, `motorcycles`, `trucks`, `reference` como código da edição, e hierarquia brands/models/years. Caminhonetes usam cars. Barcos e linha amarela continuam sem vínculo FIPE.

## Organização

- `fipe/internal/core`: comandos, validação, portas e casos de uso de consultas locais.
- `adapter/out/persistence`: catálogo JPA, consultas paginadas, cache de preços e fila operacional JDBC.
- `service/CatalogoLocalFipe`: cache de opções com fallback PostgreSQL.
- `service/SincronizacaoFipeService`: agendamento técnico e execução externa fora de transações.
- `adapter/out/http/ParallelumClient`: credencial exclusivamente no backend, URL fixa, sem redirects, cota e exclusão distribuídas, cooldown/circuito aberto compartilhado, timeout e download limitado a 2 MB.
- `frontend/src/components/FipeVehicleSelector.tsx`: sugestões dependentes nos campos normais de marca, modelo/versão e ano do modelo, com cancelamento e fallback manual. O bloco FIPE separado foi removido.

Não se deduz motorização, câmbio ou potência pela descrição. A FIPE fornece uma descrição conjunta de modelo/versão; ela é preservada, não artificialmente separada. Ano `32000` significa zero km, não um ano civil: o ano do veículo continua sendo informado. Ao vincular uma referência, o combustível é preenchido com o valor dela e o backend valida a compatibilidade do vínculo. Nunca se copia preço FIPE para preço anunciado.

## Migrations

- V38 comum: `fipe.catalogo`, árvore normalizada de edição → marca → modelo/versão → ano/combustível, com IDs internos UUID e códigos externos separados; índices por escopo, nome e pai.
- V39 write: unicidade `(escopo,codigo_externo)`, FK de hierarquia; `fipe.sincronizacoes`, fila persistente exclusiva do write com índice de pendências.
- V40 comum: modelo/versão do veículo aceitam até 250 caracteres para preservar descrições completas.

Catálogo é gravado por JPA e projetado via `RowChanged`. A fila operacional não é projetada, pois não é dado de consulta de negócio. Preços/mês/código/combustível permanecem em `fipe.referencias`, valores inteiros em centavos; não duplicar esses preços nos nós do catálogo. A consulta de preço usa a chave externa completa e mês, impedindo combinação de hierarquias diferentes.

## HTTP

Todos os endpoints FIPE exigem autenticação conforme a política existente; mutações por cookie exigem CSRF.

| Método e rota | Finalidade |
|---|---|
| GET `/api/v1/fipe/tipos` | Tipos suportados |
| GET `/api/v1/fipe/catalogo/opcoes` | Edições/marcas/modelos/anos do catálogo; escopo ausente pode ser carregado sob demanda quando autorizado |
| GET `/api/v1/fipe/catalogo/edicao-ativa?tipo=cars` | Última edição que já tem marcas carregadas para o tipo |
| POST `/api/v1/fipe/consultas` | Resolve referência local; quando autorizado e ausente, consulta e salva o preço sob demanda |
| GET `/api/v1/fipe/referencias` | Pesquisa referências já persistidas |
| GET `/api/v1/fipe/referencias/{id}` | Referência por UUID |
| POST `/api/v1/fipe/sincronizacoes` | Agenda um escopo, apenas DONO |
| GET `/api/v1/fipe/sincronizacoes/{id}` | Progresso, tentativas e erro seguro, apenas DONO |

`/fornecedor/opcoes` permanece como alias compatível. `/sugestoes` permanece local. A importação NDJSON existente foi preservada para arquivos obtidos legalmente; seu formato de preços não inclui códigos externos de marcas/modelos e, sozinho, não preenche os selects.

Opções: sem filtros lista edições; com `tipo` e `referencia`, marcas; acrescentar `marcaId` para modelos, e `modeloId` para anos. `termo` filtra por nome. `pagina` começa em zero; `tamanho` máximo 50; `temProximaPagina` controla carregar mais. A paginação ocorre em SQL e não exige count de todas as linhas.

## Sincronização autorizada

O administrador agenda escopos progressivamente, esperando a conclusão do pai antes do filho. Não há descoberta recursiva/varredura automática. Exemplos de bodies, com códigos meramente ilustrativos:

```json
{}
```

Atualiza a lista de edições. Use os códigos retornados, sem presumir o número do mês:

```json
{"tipo":"cars","referencia":338}
```

```json
{"tipo":"cars","referencia":338,"marcaId":"25"}
```

```json
{"tipo":"cars","referencia":338,"marcaId":"25","modeloId":"123"}
```

```json
{"tipo":"cars","referencia":338,"marcaId":"25","modeloId":"123","anoId":"2022-1","mesReferencia":"2026-10-01"}
```

Cada tarefa tem chave única e lease com token. `FOR UPDATE SKIP LOCKED` impede duas instâncias de assumirem a mesma tarefa ao mesmo tempo; leases expirados são retomados, upserts usam lock transacional e unicidade. A gravação é feita em lotes de até 100 linhas. Uma interrupção reexecuta o escopo idempotentemente; progresso já persistido permanece utilizável. HTTP não mantém conexão/transação de banco aberta.

Falhas transitórias/429 são reagendadas com backoff limitado, até cinco tentativas. 404/validação não geram retries automáticos. O cooldown compartilhado respeita Retry-After. A credencial compartilha limite diário e trava HTTP entre instâncias. Redis indisponível impede chamadas externas, mas não impede leituras do PostgreSQL.

No dia 1º de cada mês, quando autorizada, a rotina agenda a lista de edições e carrega as marcas da edição mais recente para os três tipos. A edição anterior continua ativa até que as marcas do novo tipo estejam carregadas; modelos, anos e preços são obtidos sob demanda. Às 03:15, escopos conhecidos do mês corrente são revalidados. Não percorre histórico nem sincroniza todos os modelos/anos indiscriminadamente. A ativação não garante que todos os modelos da nova edição já estejam no PostgreSQL.

## Cache e configuração

Prefixos: `phcauto:fipe:v1:pagina`, `phcauto:fipe:v1:preco`, geração por escopo e travas curtas. Invalidação usa troca de geração após commit; preços não encontrados e páginas vazias não entram no cache. Falhas de Redis caem para consultas indexadas no PostgreSQL. Single-flight por processo e trava compartilhada com espera curta reduzem stampedes; após a espera limitada, PostgreSQL atende o leitor.

| Variável | Padrão |
|---|---|
| `FIPE_PERSISTENCE_AUTHORIZED` | `true` |
| `FIPE_PARALLELUM_TOKEN` | vazio |
| `FIPE_DAILY_LIMIT` | `900`, intervalo permitido 1–1000 |
| `FIPE_SYNC_DELAY` | `5000` ms |
| `FIPE_CONNECT_TIMEOUT` | `PT2S` |
| `FIPE_REQUEST_TIMEOUT` | `PT10S`, máximo 30s |
| `FIPE_FAILURE_COOLDOWN` | `PT10S`; pausa compartilhada após timeout/falha de rede (HTTP 5xx usa 60s e 429 respeita Retry-After) |
| `FIPE_CATALOG_CACHE_TTL` | `PT10M` |
| `REDIS_MAXMEMORY` | `256mb` no Compose |

TTLs adicionais configuráveis via propriedades Spring: `app.fipe.brands-cache-ttl` (6h), `models-cache-ttl` e `years-cache-ttl` (1h), `prices-cache-ttl` (10min). Cron em `app.fipe.sync-refresh-cron`; `-` desativa. Cache frontend: até 100 páginas por um minuto, sem tokens nem preços inventados.

Redis é compartilhado com segurança/rate limiting: usa `noeviction`, nunca `allkeys-lru`, para não remover controles de segurança por pressão de cache. Monitorar memória e ajustar capacidade; ao atingir o limite, escritas de cache falham e a consulta usa PostgreSQL. As chaves de cache expiram. Os limites de API já existentes permanecem: 120 consultas/minuto por usuário e 30 operações administrativas/hora.

Métricas: `phcauto.fipe.cache`, `phcauto.fipe.price.cache`, `phcauto.fipe.catalog.duration`, `phcauto.fipe.http.duration`, `phcauto.fipe.external`, `phcauto.fipe.sync`, `phcauto.fipe.sync.duration`. Não registrar token, JSON do usuário ou corpo externo nos logs.

## Validação e desempenho

Testes reais usam bancos isolados com `FIPE_INTEGRATION_TEST=true`; não usar o banco de anúncios reais. Cobrem hierarquia, duplicidade, concorrência, atualização e execução da fila. Testes unitários cobrem cache hit/miss/Redis offline, HTTP 429/timeout/cota, limites e regras do vínculo. Testes de arquitetura verificam core puro e limites Modulith.

`FipeCatalogIntegrationTests.medirLatenciaCatalogoLocalComCache` mede 200 amostras por categoria após dez aquecimentos, quatro threads, PostgreSQL/Redis locais. **Não inclui HTTP, autenticação, navegador ou rede externa**, nem demonstra escala de produção.

Carga HTTP reproduzível: `k6 run scripts/load/fipe.js`. Configure `BASE_URL`, `TOKEN`, `REFERENCIA`, `MARCA`, `MODELO`, `ANO`, `MES`; opcionais `TIPO`, `VUS`, `DURATION`, `PAUSE`. Use homologação, dados já carregados e usuários de carga autorizados. Limites normais podem produzir 429 com um único token; isso é um resultado de proteção, não uma falha do catálogo. Não elevar limites em produção para executar carga.

Antes de produção: autorização e política de retenção, carga inicial licenciada, testes HTTP sustentados em múltiplas instâncias, métricas de pool/CPU/memória/cache, falhas de Redis real e definição de SLOs. As metas de p95 são hipóteses de teste, não garantias.

Medição local em 16/10/2026 (200 amostras/categoria, quatro threads, cache aquecido; chamada de serviço, sem HTTP):

| Categoria | p50 ms | p95 ms | p99 ms |
|---|---:|---:|---:|
| Marcas | 1,441 | 2,318 | 2,441 |
| Modelos | 0,776 | 1,008 | 1,099 |
| Anos | 1,420 | 2,615 | 3,233 |
| Preço | 0,667 | 1,217 | 1,428 |

Suíte nessa execução: 178 testes, zero falhas/erros, 24 condicionais ignorados. O roteiro k6 foi adicionado, mas não executado contra uma implantação de homologação; ainda não há p95 HTTP validado.

Validação visual com Playwright, usando o formulário real e respostas simuladas: seleção dependente, preenchimento, limpeza ao trocar marca, ausência de overflow em 390 px e fallback manual com HTTP 503. Evidências locais em `output/playwright/fipe-desktop.png`, `fipe-mobile.png` e `fipe-fallback.png`. Isso não substitui um teste ponta a ponta com catálogo licenciado. Backend reconstruído e iniciado com sincronização real desativada.
