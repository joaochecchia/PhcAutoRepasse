# Módulo de planos

O módulo `planos` administra os planos comerciais sem depender de usuários, anúncios, pagamentos ou permissões. Essas integrações poderão depender da API pública de planos quando suas regras forem definidas.

## Endpoints

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/api/v1/assinaturas/planos` | Cria e retorna o plano com HTTP 201 |
| GET | `/api/v1/assinaturas/planos/{planoId}` | Consulta um plano |
| GET | `/api/v1/assinaturas/planos?offset=0&limite=20` | Lista até 100 planos |
| PATCH | `/api/v1/assinaturas/planos/{planoId}` | Altera somente campos enviados |
| DELETE | `/api/v1/assinaturas/planos/{planoId}` | Exclui com HTTP 204 |

Exemplo de criação:

```json
{
  "nome": "Premium",
  "valorCentavos": 19990,
  "periodoMeses": 1,
  "limiteAnuncios": 25,
  "limiteVistoriasCautelares": 1,
  "ativo": true
}
```

Exemplo de alteração parcial:

```json
{
  "valorCentavos": 24990,
  "ativo": false
}
```

Nome é obrigatório, possui até 100 caracteres e é único. Valor e limite aceitam zero, mas não valores negativos. Período aceita de 1 a 32767 meses. Valor, período, limite de anúncios e limite de vistorias cautelares são opcionais porque as regras comerciais definitivas ainda não foram aprovadas. Campos ausentes ou nulos no PATCH permanecem inalterados.

A fachada abre transações curtas no banco write. Consultas também usam o write para consistência imediata neste fluxo administrativo. A sincronização técnica `RowChanged` atualiza o banco read depois do commit. A FK existente impede excluir planos vinculados a assinaturas e resulta em HTTP 409.


A futura assinatura preservará `limiteAnunciosContratado` e `limiteVistoriasCautelaresContratado` como snapshots. Nenhum desses campos aplica limitação atualmente, e não existe fluxo de vistoria ou contador de consumo.
