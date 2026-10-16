> Atualização de arquitetura (15/10/2026): o fluxo interativo agora é exclusivamente local. Consulte [Catálogo FIPE local](fipe-catalogo-local.md), que substitui as instruções antigas de consulta externa durante o cadastro. Sincronização real permanece desativada por decisão do usuário.

# FIPE: catálogo mensal e consulta complementar

O módulo `fipe` guarda referências históricas no PostgreSQL de escrita. Uma importação administrativa alimenta o catálogo mensal; uma seleção ausente pode ser consultada na Parallelum e persistida para reutilização entre usuários. Não há scraping nem download automático de uma base integral sem fornecedor definido.

## Vínculo ao anúncio

POST e PATCH `/api/v1/anuncios` aceitam `fipeReferenciaId` (UUID retornado pelo catálogo). No PATCH use `/api/v1/anuncios/{id}`. Não envie preço FIPE no request: ele vem exclusivamente da referência persistida.

Carro, moto, caminhão e caminhonete aceitam FIPE; caminhonete corresponde ao tipo FIPE CARRO. Barco e linha amarela não aceitam vínculo neste fluxo. A referência é opcional para preservar anúncios existentes e veículos sem correspondência confirmada.

O core valida tipo, marca, ano/zero km, combustível e versão completa. Preencha `versao` com a descrição completa do modelo FIPE selecionado e `modelo` com seu nome compatível. Não existe associação por semelhança nem promessa de acerto automático: o anunciante seleciona a versão correta. A validação não comprova que o veículo físico é realmente daquela versão.

O backend grava em `catalogo.veiculos` o ID da referência, código, valor em centavos, mês, descrição do modelo e fonte. Respostas de criação, manutenção e busca retornam `fipe`, incluindo diferença em centavos e percentual frente ao preço pedido. Preço sob consulta não gera diferença. Sem vínculo, `fipe` é nulo.

PATCH com `removerFipe: true` limpa o vínculo. Trocar identificação/especificações relevantes sem selecionar outra referência limpa a FIPE anterior; mudar somente preço ou quilometragem a preserva. Selecionar nova referência e pedir remoção simultaneamente é inválido. Atualização mensal do catálogo não altera silenciosamente o valor histórico escolhido para um anúncio.

## Endpoints autenticados

- GET `/api/v1/fipe/referencias?tipo=CARRO&marca=Honda&termo=Civic&ano=2023&mes=2026-10-01&pagina=0&tamanho=50`: catálogo local, máximo 50 itens. Filtros são opcionais.
- GET `/api/v1/fipe/referencias/{id}`: referência local disponível.
- POST `/api/v1/fipe/sugestoes`: recebe os dados técnicos já preenchidos e retorna até 12 combinações reais compatíveis do mês vigente, sem estimar valores.
- GET `/api/v1/fipe/fornecedor/opcoes`: meses de referência; acrescente `tipo=cars`, `motorcycles` ou `trucks` para marcas, depois `marcaId` para modelos e `modeloId` para anos. Use `referencia` para o código do mês do provedor. Aceita `termo`, `pagina` e `tamanho`, limitado a 50.
- POST `/api/v1/fipe/consultas`: consulta local pela seleção exata e, se ausente, consulta externa e persistência.

Exemplo de estrutura da consulta (obtenha os IDs reais no endpoint de opções):

```json
{
  "tipo": "cars",
  "marcaId": "6",
  "modeloId": "1",
  "anoId": "2023-1",
  "referencia": 1,
  "mesReferencia": "2026-10-01"
}
```

Esses IDs são ilustrativos, não uma combinação garantida. O mês retornado pelo provedor deve coincidir com o solicitado. Lançamentos só podem ser consultados quando já existirem no fornecedor.

## Importação administrativa

Somente DONO pode usar:

- POST `/api/v1/fipe/importacoes?fonte=fornecedor-autorizado&mes=2026-10-01`, `Content-Type: application/x-ndjson`, corpo contendo o arquivo bruto (não multipart); retorna 202 e ID do processamento.
- GET `/api/v1/fipe/importacoes/{id}`: status e progresso.
- POST `/api/v1/fipe/importacoes/{id}/retomar`: retoma importação com falha no último lote confirmado.

Uma linha por referência, sem array externo. Exemplo **fictício**, apenas do formato:

```json
{"codigoFipe":"123456-7","tipo":"CARRO","marca":"Marca Exemplo","modelo":"Modelo Exemplo 1.0 FLEX","anoModelo":2023,"combustivel":"GASOLINA","mesReferencia":"2026-10-01","valorCentavos":7000000}
```

`anoModelo: 32000` representa a categoria zero km do fornecedor. Todos os registros devem pertencer ao mês informado, no primeiro dia do mês, sem mês futuro. Limites: 50 MB por arquivo, 8 KB por linha, 100 referências por lote. O processamento é incremental e retoma pelo deslocamento em bytes, sem carregar o arquivo inteiro em memória. Falha reverte o lote atual; lotes anteriores permanecem ocultos até a conclusão integral. Reenvio do mesmo arquivo/fonte/mês reutiliza a importação pelo checksum. Arquivo inválido precisa ser corrigido e reenviado; não editar o arquivo interno de um job já iniciado.

Também é possível depositar `YYYY-MM.ndjson` em `data/fipe/entrada/`. Diariamente às 03h de São Paulo, o backend verifica o arquivo do mês atual e o enfileira; o checksum evita reimportação. A aquisição e autorização desse arquivo são responsabilidade operacional ainda pendente.

## Arquitetura e desempenho

Use cases e gateways permanecem no core Java puro. O adaptador HTTP consulta o fornecedor fora de transações de banco. O módulo anúncios acessa somente a fachada pública `FipeFacade` e persiste seu snapshot na mesma transação do agregado. Não há chamada externa na criação, PATCH ou listagem de anúncios.

Referências guardam a descrição do modelo junto de cada valor mensal, preservando o histórico sem depender de um cadastro de modelos mutável. As migrations V35 comum e V36/V37 de escrita criam catálogo, jobs, snapshots, índices, unicidades por tipo/código/ano/combustível/mês/importação e FKs. JPA publica RowChanged para projeção assíncrona; a leitura pública usa o snapshot já projetado no veículo, sem consultas adicionais por card. O limite existente de 52 anúncios por página permanece.

Os índices suportam filtros de catálogo, busca textual por modelo, seleção exata e fila de importação. Isso reduz o trabalho de busca, mas não substitui testes de carga com a base real. Consultas administrativas usam paginação limitada, e o catálogo incompleto não fica disponível para vinculação.

## Configuração e operação

- `FIPE_PARALLELUM_TOKEN`: token privado, somente backend. Sem token, o catálogo local continua funcionando; consulta externa retorna indisponibilidade.
- `FIPE_DAILY_LIMIT`: padrão 900 chamadas numa janela móvel de 24h, compartilhada no Redis por token. Ajuste à cota contratada; chamadas feitas fora deste backend não entram nesse contador.
- `FIPE_IMPORT_DIRECTORY`: padrão `./data/fipe`; Compose monta em `/app/data/fipe`.
- `FIPE_MONTHLY_SOURCE`: identificação da fonte do arquivo mensal.
- Confira `app.fipe` em `application.yaml` para intervalo do worker e agendamento.

Redis armazena cache positivo por seis horas, ausência 404 por 30 minutos, orçamento e lock global para limitar concorrência externa. Timeout, indisponibilidade e 429 geram espera/cooldown, nunca um valor FIPE inventado. Redis indisponível bloqueia consultas ao provedor para não perder o controle da cota. Há limites HTTP separados para catálogo, consulta externa e importação.

Métricas `phcauto.fipe.external` e `phcauto.fipe.import.errors` permitem acompanhar uso e erros. Os arquivos são mantidos para retomada; dimensione disco, backup e retenção operacional. Várias instâncias precisam compartilhar o diretório de importação. Não remover arquivos de jobs pendentes/em processamento/com falha que devam ser retomados.

Ainda é necessário fornecer um catálogo autorizado para a carga integral. O token externo é configurado somente no backend e nunca chega ao navegador. Nenhuma FIPE fictícia foi carregada no banco da aplicação.

Na etapa 01 o formulário coleta normalmente tipo, marca, modelo, versão, ano, condição, combustível e ficha técnica. Versão é obrigatória para carro, moto, caminhão e caminhonete. Depois da localização, ao abrir a etapa 03, o frontend envia esses dados para `POST /api/v1/fipe/sugestoes`. O backend pesquisa o mês vigente e seleciona somente candidatos reais com marca, modelo, versão, ano/zero km e combustível compatíveis.

Uma correspondência única é resolvida automaticamente por `/api/v1/fipe/consultas`. Em caso de ambiguidade, o usuário escolhe entre versões reais retornadas. Se a identificação falhar ou o usuário considerar a versão incorreta, a etapa 03 oferece a busca manual pelo catálogo oficial. Ela permite escolher outra referência, mas nunca editar o valor e chamá-lo de FIPE. O frontend recebe o UUID persistido e envia somente `fipeReferenciaId` ao criar o anúncio; o core repete todas as validações e ignora qualquer tentativa do cliente de fornecer o valor FIPE.

## Validação

Testes cobrem regras de vínculo, remoção e preservação no PATCH, parsing, cache negativo, timeout, 429, Redis indisponível, paginação, importação incremental, rollback, concorrência, autorização e projeção real para cards. Integração: `FIPE_INTEGRATION_TEST=true`, sempre com bancos isolados write/read e Redis isolado. Chamadas reais ao fornecedor não fazem parte dos testes automatizados.
