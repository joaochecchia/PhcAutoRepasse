# Busca pública de anúncios

`GET /api/v1/anuncios` consulta exclusivamente a projeção de leitura e retorna somente anúncios com status `PUBLICADO`. A projeção é atualizada de forma assíncrona pelo Spring Modulith, portanto um anúncio recém-publicado pode levar um pequeno intervalo para aparecer.

Todos os parâmetros são opcionais e combináveis:

- `tipoVeiculo`: `CARRO`, `MOTO`, `CAMINHAO`, `CAMINHONETE`, `BARCO` ou `LINHA_AMARELA`.
- `cidade`, `uf`, `marca`, `tipoPessoa` (`PF`/`PJ`) e `perfil` (nome da pessoa ou nome fantasia da loja).
- `precoMinimoCentavos` e `precoMaximoCentavos`, aceitos de forma independente.
- `anoMinimo` e `anoMaximo`, aplicados ao ano do modelo e aceitos de forma independente.
- `cambio`, `combustivel`, `motorizacao`, `condicao` (`ZERO_KM`/`USADO`).
- `tipoDirecao`, `tracao`, `ipvaPago`, `blindado`, `numeroPortas`, `cilindradaLitros`, `tipoFreio` e `carroceria`.
- `pagina`, iniciando em zero, e `tamanho`, de 1 a 100. Os padrões são página 0 e tamanho 20.

Textos de valores categóricos são comparados sem diferença entre letras maiúsculas e minúsculas. `perfil` aceita busca parcial. O endereço público contém somente cidade e UF.

Exemplo:

```http
GET /api/v1/anuncios?tipoVeiculo=CARRO&cidade=Goiania&uf=GO&marca=Chevrolet&precoMinimoCentavos=5000000&precoMaximoCentavos=9000000&anoMinimo=2020&cambio=AUTOMATICO&combustivel=FLEX&condicao=USADO&pagina=0&tamanho=20
```

Resposta:

```json
{
  "mensagem": "Anúncios encontrados com sucesso",
  "carros": [],
  "total": 0,
  "pagina": 0,
  "tamanho": 20
}
```

A chave `carros` foi mantida conforme o contrato solicitado, embora possa conter qualquer um dos seis tipos de veículo.
