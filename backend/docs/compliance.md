# Compliance, aceite e restrição etária

O módulo `repasse.phcauto.backend.compliance` registra o comprovante de aceite dos Termos de Uso e da Política de Privacidade durante `POST /api/v1/usuarios`.

## Contrato de cadastro

Além dos dados do usuário, o request exige:

```json
{
  "aceitouTermos": true,
  "aceiteTermosEm": "2026-09-29T14:00:00Z",
  "versaoTermosUso": "1.1",
  "versaoPoliticaPrivacidade": "1.1"
}
```

`aceiteTermosEm` representa o instante do clique informado pelo frontend. O servidor também grava `registrado_em` usando seu próprio relógio. O identificador de rede é obtido pelo controller com `HttpServletRequest.getRemoteAddr()` e não pode ser escolhido no JSON.

As versões precisam coincidir com:

- `TERMOS_USO_VERSAO_ATUAL`
- `POLITICA_PRIVACIDADE_VERSAO_ATUAL`

Os defaults locais são `1.1`. O documento publicado no frontend fica em `public/documentos/termos-uso-politica-privacidade-v1.1.pdf`. Uma publicação de novos documentos deve atualizar as variáveis, arquivar o novo PDF e ajustar o frontend no mesmo release. Manter apenas a versão sem conservar o documento correspondente não prova o conteúdo histórico.

## Persistência e transação

A migration `V11__cria_registro_aceites_compliance.sql` cria `compliance.aceites_termos` com:

- ID do comprovante;
- ID do usuário;
- instante declarado do clique;
- versão dos Termos de Uso;
- versão da Política de Privacidade;
- endereço IP ou identificador de rede observado;
- instante de registro no servidor;
- versão de lock.

O módulo de usuários chama `ComplianceFacade` após criar o agregado, dentro da mesma transação write. `DefaultComplianceFacade` e o gateway exigem transação existente. Qualquer falha reverte usuário, perfil, endereço, aceite e publicações Modulith.

A tabela não possui endpoint público. Ela é sincronizada para o read pelos eventos técnicos `RowChanged`. O identificador do usuário é preservado mesmo após exclusão da conta para que a política de retenção possa ser definida antes de eliminar evidências; acesso, retenção, anonimização e descarte ainda precisam de regras operacionais.

## Restrição etária

Cadastros PF exigem 18 anos completos. A mesma regra é aplicada no PATCH da data de nascimento. Quem completa 18 anos na data atual pode se cadastrar.

PJ não possui idade. Se futuramente for necessário verificar a maioridade do representante legal, crie um contrato e finalidade próprios; não reutilize silenciosamente a data de nascimento da PF.

## Limites

Esta implementação cobre os requisitos técnicos definidos para o cadastro. Adequação integral à LGPD também depende de base legal por finalidade, transparência, canal de direitos do titular, retenção e descarte, segurança, governança, contratos com operadores e resposta a incidentes.
