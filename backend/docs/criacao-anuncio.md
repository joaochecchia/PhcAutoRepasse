# Criação de anúncio

O fluxo real de criação usa `POST /api/v1/anuncios`. A mesma transação do banco write grava o veículo base, exatamente uma especialização, o endereço próprio do anúncio, os motores quando o veículo for um barco e o anúncio. Uma falha reverte toda a operação.

O request público é `CriarAnuncioRequest`. Campos comuns obrigatórios: `anuncianteId`, `tipoVeiculo`, `fabricante`, `modelo`, `titulo`, `tipoPreco` e `endereco`. O endereço exige CEP numérico com 8 dígitos, cidade, bairro, rua e UF. O frontend não envia coordenadas. O backend valida cidade e UF no catálogo de municípios e salva o código IBGE no endereço do anúncio. `numero` e `complemento` podem ser omitidos.

Exemplo para carro:

```json
{
  "anuncianteId": "00000000-0000-0000-0000-000000000000",
  "tipoVeiculo": "CARRO",
  "fabricante": "Toyota",
  "modelo": "Corolla",
  "versao": "2.0 XEi",
  "anoFabricacao": 2024,
  "anoModelo": 2025,
  "cor": "Preto",
  "titulo": "Corolla 2.0 XEi",
  "descricao": "Veículo revisado",
  "tipoPreco": "FIXO",
  "precoCentavos": 14590000,
  "aceitaTroca": true,
  "publicarAgora": true,
  "endereco": {
    "cep": "74000000",
    "cidade": "Goiânia",
    "bairro": "Centro",
    "rua": "Rua 1",
    "numero": "10",
    "complemento": null,
    "uf": "GO"
  },
  "carro": {
    "quilometragem": 12000,
    "cambio": "Automático",
    "combustivel": "Flex",
    "numeroPortas": 4,
    "numeroLugares": 5
  }
}
```

Deve ser enviado exatamente um bloco compatível com `tipoVeiculo`: `carro`, `moto`, `caminhao`, `caminhonete`, `barco` ou `linhaAmarela`. Para barco, o bloco aceita `motores`; suas posições devem ser positivas e únicas.

`FIXO` exige `precoCentavos` positivo. `SOB_CONSULTA` exige que `precoCentavos` seja omitido. `publicarAgora=true` cria o anúncio como `PUBLICADO`; ausente ou falso cria `RASCUNHO`.

A resposta `AnuncioResponse` retorna os identificadores do anúncio e do veículo, dados comuns, detalhes específicos, status e datas. Por privacidade, a localização pública contém somente `cidade`; CEP, bairro, rua, número, complemento e UF não fazem parte da resposta.

O core fica em `anuncios/internal/core`, sem Spring ou JPA. A fachada transacional e os adaptadores ficam em `anuncios/internal/infrastructure`. `AnuncioCriado` é o evento público do módulo. As alterações JPA também geram `RowChanged`, consumido pelo projetor Modulith para sincronizar o banco read.

Enquanto a autenticação HTTP não estiver pronta, `anuncianteId` vem no request e precisa identificar um usuário ativo. Futuramente ele deverá ser obtido da identidade autenticada, e não aceito diretamente do cliente.

## Alteração parcial e exclusão

- `PATCH /api/v1/anuncios/{anuncioId}` altera somente os campos enviados do anúncio, veículo, endereço ou especialização. `null` significa manter o valor atual.
- O tipo do veículo, o anunciante e os identificadores do agregado não podem ser trocados pelo PATCH.
- Somente o bloco específico compatível com o tipo existente é aceito. Para barcos, uma lista não vazia de motores substitui a lista atual; lista ausente ou vazia mantém os motores.
- `tipoPreco=SOB_CONSULTA` limpa o preço. Para `FIXO`, o preço final deve ser positivo.
- `publicarAgora=true` publica e `false` volta para rascunho. Se omitido, o status existente é preservado.
- `DELETE /api/v1/anuncios/{anuncioId}` remove fotos, características, especialização, veículo e endereço dentro da mesma transação. Compras vinculadas bloqueiam a exclusão com HTTP 409.
- Atualização e exclusão publicam eventos de negócio e as mudanças de todas as linhas continuam sendo projetadas no banco read por `RowChanged`.

## Placa completa e visibilidade

Carros, motos, caminhões e caminhonetes exigem `placa` completa no cadastro, com 7 caracteres e sem pontuação, aceitando os formatos brasileiro antigo e Mercosul. O backend normaliza a placa para letras maiúsculas antes de persistir. O request de criação exige também `exibirPlacaCompleta`; no PATCH ambos os campos são opcionais e os demais dados do veículo podem ser alterados isoladamente.

A placa completa sempre fica armazenada. `exibirPlacaCompleta=true` devolve a placa completa em `detalhes.placa`; quando falso, a resposta devolve somente o último caractere. `detalhes.placaCompletaVisivel` informa qual regra foi aplicada. A API nunca depende do frontend para ocultar a placa. Registros anteriores à V13 permanecem com `placa` nula até que sejam corrigidos; a coluna legada `final_placa` foi preservada nesta etapa para uma implantação compatível.

A migration comum V13 adiciona `placa` e `exibir_placa_completa` às quatro tabelas e a projeção Modulith replica os dois campos. V16 adiciona as coordenadas do endereço do anúncio e V17 exige o par em novas gravações no banco write, preservando registros legados. V18 adiciona coordenadas aos endereços de usuário. Próximas migrations devem usar V19 ou superior.
