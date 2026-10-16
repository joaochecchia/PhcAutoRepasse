# Login e sessão

POST `/api/v1/usuarios/registrar` mantém o contrato de cadastro PF/PJ e compliance. As versões vigentes dos documentos legais podem ser consultadas em `GET /api/v1/compliance/documentos-vigentes`.

POST `/api/v1/usuarios/login` recebe:

```json
{"email":"usuario@exemplo.com","senha":"sua-senha"}
```

No sucesso, a resposta 200 contém somente os metadados da sessão:

```json
{"autenticado":true,"usuarioId":"<uuid>","papel":"CLIENTE","expiresIn":900}
```

O JWT de acesso é enviado exclusivamente no cookie `PHC_AUTH` e dura 15 minutos. Um refresh token opaco é enviado em `PHC_REFRESH`; ambos usam `HttpOnly`, `SameSite=Lax` e caminho `/`. Defina `AUTH_COOKIE_SECURE=true` em HTTPS para ativar `Secure` (obrigatório em produção). O frontend usa Axios com `withCredentials: true` e não lê nem armazena tokens.

POST `/api/v1/usuarios/refresh` rotaciona o refresh token e emite novo JWT. Só o hash SHA-256 do refresh fica no banco. Reutilizar um token já consumido revoga toda a família da sessão. GET `/api/v1/usuarios/sessao` informa a sessão atual; POST `/api/v1/usuarios/logout` revoga a família e expira os dois cookies. O resolver também aceita `Authorization: Bearer <jwt>` para Swagger e clientes de API.

A troca de senha incrementa a versão de sessão do usuário e revoga todas as famílias de refresh token da conta. Como cada JWT carrega essa versão e ela é validada no banco em toda autenticação, tokens emitidos anteriormente deixam de ser aceitos imediatamente em todos os dispositivos.

Credenciais inválidas retornam 401; dados de entrada inválidos, 400; falta de permissão, 403. Novas senhas usam BCrypt e exigem no mínimo oito caracteres, maiúscula, minúscula, número e caractere especial; PBKDF2 legado continua aceito.

`JWT_SECRET` é obrigatório: Base64 de pelo menos 32 bytes aleatórios. Os defaults são `JWT_ISSUER=phcauto`, `JWT_TTL_SECONDS=900` e `REFRESH_TOKEN_TTL_SECONDS=2592000` (30 dias). O JWT inclui a versão de segurança da conta e deixa de ser aceito após troca/redefinição de senha; os refresh tokens também são revogados. Com `MFA_ENABLED=true`, `POST /api/v1/autenticacao/login` só emite a sessão diretamente para dispositivo confiável, caso contrário retorna um desafio para `POST /api/v1/autenticacao/2fa/verificar`.
# Login com Google

O OAuth2 social usa Authorization Code no backend. O Google comprova a identidade e a
aplicação emite seus próprios access e refresh tokens nos mesmos cookies HttpOnly do login local.
Ative somente após cadastrar os callbacks nos provedores:

```text
http://localhost:8080/login/oauth2/code/google
```

Em produção, substitua a origem pela URL pública do backend e configure:

```env
OAUTH2_ENABLED=true
OAUTH2_FRONTEND_URL=https://www.seudominio.com.br
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

Cadastros novos recebem um ticket aleatório de uso curto armazenado no Redis por hash. Nome e
e-mail vêm do provedor, e os demais dados obrigatórios e o aceite são concluídos no formulário.
Uma conta local preexistente nunca é vinculada automaticamente apenas pela coincidência do e-mail.

