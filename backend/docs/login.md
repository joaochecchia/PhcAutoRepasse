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

O JWT é enviado exclusivamente no cookie `PHC_AUTH`, com `HttpOnly`, `SameSite=Lax`, caminho `/` e tempo de vida igual ao token. Defina `AUTH_COOKIE_SECURE=true` quando a aplicação estiver publicada em HTTPS. O frontend usa Axios com `withCredentials: true` e não lê nem armazena o token.

GET `/api/v1/usuarios/sessao` informa a sessão atual; quando não há autenticação válida, retorna `{"autenticado":false}`. POST `/api/v1/usuarios/logout` expira o cookie. O resolver também aceita `Authorization: Bearer <jwt>` para Swagger e clientes de API.

Credenciais inválidas retornam 401; dados de entrada inválidos, 400; falta de permissão, 403. Novas senhas usam BCrypt (mínimo 8 caracteres, máximo 72 bytes UTF-8) e PBKDF2 legado continua aceito.

`JWT_SECRET` é obrigatório: Base64 de pelo menos 32 bytes aleatórios. `JWT_ISSUER=phcauto` e `JWT_TTL_SECONDS=900` são os defaults. Não há refresh nem lista de revogação; tokens emitidos expiram conforme o TTL. OAuth Google/Facebook permanece pendente.
