# Agregado de usuário

Usuário é a única raiz HTTP para os dados cadastrais. Perfil PF/PJ e endereço não possuem
controllers próprios e não podem ser criados, consultados, editados ou excluídos diretamente pela
API.

## Endpoints

| Método | Rota | Comportamento |
| --- | --- | --- |
| POST | `/api/v1/usuarios` | Cria usuário, PF ou PJ e endereço atomicamente |
| GET | `/api/v1/usuarios/{id}` | Retorna usuário, perfil correspondente e endereço |
| PATCH | `/api/v1/usuarios/{id}` | Altera apenas os campos/blocos enviados |
| DELETE | `/api/v1/usuarios/{id}` | Remove endereço, perfil e usuário atomicamente |

Não existem rotas HTTP para
`usuarios-pf`, `usuarios-pj` e `enderecos`.

## PATCH

O tipo da pessoa não pode ser alterado. Uma PF só aceita CPF e data de nascimento; uma PJ só aceita
CNPJ e razão social. Alterar o nome de uma PJ também mantém `nome_fantasia` consistente.

Campos ausentes ou `null` permanecem inalterados. String vazia limpa apenas `numero` e `complemento`
do endereço. Campos obrigatórios não podem ser apagados. Um corpo vazio é rejeitado.

Exemplo de alteração somente do endereço:

```json
{
  "endereco": {
    "cidade": "Campinas",
    "rua": "Rua Nova",
    "complemento": ""
  }
}
```

Exemplo de alteração somente de dados da PF:

```json
{
  "cpf": "52998224725",
  "dataNascimento": "1990-01-01"
}
```

## Transação e projeção

Criação, atualização e exclusão usam o banco write em uma única transação. Cada entidade JPA
realmente alterada gera `RowChanged`; o listener existente faz upsert/delete no banco read após o
commit. Uma alteração apenas de endereço não força update do usuário ou do perfil.

Na exclusão, endereço e perfil são removidos antes do usuário. Se dados de compra, anúncios,
assinaturas ou outros vínculos impedirem a remoção, a transação inteira é revertida e a API retorna
409. Não há exclusão parcial. O evento de negócio `UsuarioAlterado` só é publicado depois que o
gateway conclui a atualização ou exclusão, ainda dentro da transação.

GET usa o write para devolver o agregado consistente imediatamente após uma alteração. Senha e hash
nunca aparecem na resposta.

## Casos de uso e portas

- `BuscarUsuarioUseCase.execute(UUID)` depende de `ConsultarUsuarioGateway`.
- `AtualizarUsuarioUseCase.execute(UUID, AtualizarUsuarioRequest)` depende de portas segregadas de
  consulta, atualização, hash e publicação.
- `ExcluirUsuarioUseCase.execute(UUID)` depende de `ExcluirUsuarioGateway` e da porta de eventos.
- `JpaUsuarioAggregateGateway` é o adaptador JPA interno e coordena as quatro tabelas.

O core permanece sem Spring/JPA. O tipo PF/PJ é imutável e as consultas de unicidade usam o write.
As constraints PostgreSQL continuam protegendo contra concorrência.

## Validação

`UsuarioAggregateIntegrationTests` verifica PF e PJ, PATCH parcial, GET agregado, delete no write e
read, rollback quando há vínculo de compra e ausência das rotas diretas. `ManterUsuarioTests` cobre
ordem entre persistência e evento, hash de nova senha, PATCH vazio e campos incompatíveis.
