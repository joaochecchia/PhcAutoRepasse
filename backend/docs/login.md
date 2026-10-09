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

Credenciais inválidas retornam 401; dados de entrada inválidos, 400; falta de permissão, 403. Novas senhas usam BCrypt (mínimo 8 caracteres, máximo 72 bytes UTF-8) e PBKDF2 legado continua aceito.

`JWT_SECRET` é obrigatório: Base64 de pelo menos 32 bytes aleatórios. Os defaults são `JWT_ISSUER=phcauto`, `JWT_TTL_SECONDS=900` e `REFRESH_TOKEN_TTL_SECONDS=2592000` (30 dias). O JWT de acesso já emitido permanece válido no máximo até completar seus 15 minutos; logout e detecção de reutilização impedem novas renovações. OAuth Google/Facebook permanece pendente.
