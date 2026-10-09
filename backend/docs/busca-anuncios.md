# Busca pública de anúncios

`GET /api/v1/anuncios` consulta exclusivamente a projeção de leitura e retorna somente anúncios com status `PUBLICADO`. A projeção é atualizada de forma assíncrona pelo Spring Modulith, portanto um anúncio recém-publicado pode levar um pequeno intervalo para aparecer.

Todos os parâmetros são opcionais e combináveis:

- `tipoVeiculo`: `CARRO`, `MOTO`, `CAMINHAO`, `CAMINHONETE`, `BARCO` ou `LINHA_AMARELA`.
- `cidade`, `uf`, `latitude` e `longitude` para a busca por proximidade.
- `marca`, `modelo`, `termo` (marca/modelo livre), `tipoPessoa` (`PF`/`PJ`) e `perfil` (nome da pessoa ou nome fantasia da loja).
- `precoMinimoCentavos` e `precoMaximoCentavos`, aceitos de forma independente.
- `anoMinimo` e `anoMaximo`, aplicados ao ano do modelo e aceitos de forma independente.
- `cambio`, `combustivel`, `motorizacao`, `condicao` (`ZERO_KM`/`USADO`).
- `tipoDirecao`, `tracao`, `ipvaPago`, `blindado`, `numeroPortas`, `cilindradaLitros`, `tipoFreio` e `carroceria`.
- `pagina`, iniciando em zero, e `tamanho`, de 1 a 52. Os padrões são página 0 e tamanho 20.

A localização aceita `modoLocalizacao=DISPOSITIVO`, `ENDERECO_CADASTRADO`, `CIDADE`, `UF` ou `BRASIL`. Cidade exige apenas `cidade` e `uf`; o backend consulta o catálogo municipal local, sem API externa. Dispositivo exige somente latitude e longitude. Endereço cadastrado exige autenticação. UF aplica igualdade estadual, sem raio. Brasil não adiciona predicado geográfico. Quando o modo não é enviado, ele é inferido pelos parâmetros presentes.

Dispositivo, cidade e endereço cadastrado procuram primeiro anúncios dentro de 100 km e ampliam para 200 km somente quando nenhum anúncio satisfaz todos os filtros em 100 km. A resposta informa `raioKmAplicado`, `temProximaPagina` e `distanciaKm`. O frontend incrementa `pagina` enquanto `temProximaPagina=true`; cada consulta retorna no máximo 52 itens.

Textos são comparados sem diferença entre letras maiúsculas e minúsculas. `termo` aceita busca parcial por marca, modelo ou pela combinação dos dois; `perfil` aceita busca parcial por nome. O endereço público contém somente cidade e UF.

Exemplo:

```http
GET /api/v1/anuncios?tipoVeiculo=CARRO&cidade=Goiania&uf=GO&modoLocalizacao=CIDADE&marca=Chevrolet&modelo=Onix&pagina=0&tamanho=52
```

Resposta:

```json
{
  "mensagem": "Anúncios encontrados com sucesso",
  "carros": [],
  "total": 0,
  "pagina": 0,
  "tamanho": 52,
  "raioKmAplicado": 200,
  "temProximaPagina": false
}
```

Anúncios antigos sem coordenadas não aparecem na busca por raio até terem a localização atualizada. Eles continuam disponíveis nas buscas sem proximidade. A chave `carros` foi mantida conforme o contrato atual, embora possa conter qualquer um dos seis tipos de veículo.

## Página inicial

`GET /api/v1/anuncios/pagina-inicial` é exclusivo da vitrine inicial e retorna no máximo 20 anúncios publicados. Ele não altera a ordenação da busca paginada. A relevância usa, nesta ordem: existência de foto, quantidade de fotos, completude dos dados disponíveis, data de publicação e ID como desempate estável. Planos e assinaturas não influenciam o ranking enquanto seus benefícios comerciais não forem definidos.

A resposta é armazenada no Redis no cache `anunciosPaginaInicial`, chave `v1`, com o TTL geral configurado por `REDIS_CACHE_TTL`. Alterações projetadas em anúncio, veículo, especialização, endereço, foto ou perfil do anunciante limpam esse cache depois que a transação da projeção read termina. Assim, o cache não é reconstruído com uma versão anterior à alteração recém-projetada.
