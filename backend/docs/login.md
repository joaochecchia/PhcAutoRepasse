# Login e registro

POST `/api/v1/usuarios/registrar` mantém o contrato de cadastro PF/PJ e compliance. POST `/api/v1/usuarios/login` recebe:

```json
{"email":"usuario@exemplo.com","senha":"sua-senha"}
```

Resposta 200 com Cache-Control: no-store:

```json
{"mensagem":"Login realizado com sucesso","accessToken":"<jwt>","tokenType":"Bearer","expiresIn":900}
```

Use Authorization: Bearer <jwt> nas rotas protegidas ou Authorize no Swagger. Credenciais inválidas retornam 401; dados de entrada inválidos, 400; falta de permissão, 403. Não há sessão HTTP.

LoginUseCase/Login dependem apenas de AutenticacaoGateway no core. DatabaseAutenticacaoGateway usa AuthenticationManager, DaoAuthenticationProvider e UsuarioDetailsService; a leitura acontece no write em transação curta, encerrada antes da verificação da senha. JwtLoginService emite o token na infraestrutura. A entidade JPA não implementa UserDetails.

Novas senhas usam BCrypt (mínimo 8 caracteres, máximo 72 bytes UTF-8). PBKDF2 legado continua aceito. O token contém UUID do usuário e papel, sem dados pessoais ou senha.

JWT_SECRET é obrigatório: Base64 de pelo menos 32 bytes aleatórios. Gere com `openssl rand -base64 48`, coloque em `.env` para Compose ou exporte pela IDE/Maven. Não versionar segredos. JWT_ISSUER=phcauto e JWT_TTL_SECONDS=900 são os defaults; TTL aceito de 1 a 86400 segundos.

Swagger, registro, login e busca de anúncios são públicos. Usuários operam o próprio cadastro; anúncios exigem anunciante autenticado/proprietário; ADMIN pode administrar cadastros/anúncios e o CRUD de planos. Endpoints provisórios são bloqueados. Não há refresh/revogação: tokens já emitidos valem até a expiração, inclusive após exclusão ou alteração de credenciais. OAuth Google/Facebook permanece pendente.

Spring Boot 4.1.1 gerencia Spring Security 7.1.1; springdoc 3.1.1 é mantido para Boot 4. A documentação tem esquema bearerAuth. Não houve alteração de schema ou necessidade de migration.

Testes SecurityHttpTests e LoginHttpTests verificam autorização, assinatura/emissor/expiração, credenciais, compatibilidade dos hashes e ausência de sessão.
