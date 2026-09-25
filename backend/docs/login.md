# Login local

`POST /api/v1/usuarios/login`

```json
{"email":"cliente@example.com","senha":"Senha-de-exemplo-123"}
```

Sucesso: HTTP 200, exclusivamente:

```json
{"mensagem":"Login realizado com sucesso"}
```

Credenciais incorretas, usuário inexistente, inativo, sem senha local ou email legado ambíguo retornam HTTP 401 com a mesma mensagem `Email ou senha inválidos`. Corpo inválido retorna 400. Falhas de banco não são convertidas em credenciais incorretas.

## Organização

- `usuarios.request.LoginRequest`: record de email e senha, normaliza somente o email, valida presença/formato e limita tamanho. Senha WRITE_ONLY e toString protegido.
- `usuarios.response.LoginResponse`: record com apenas mensagem.
- Os pacotes request e response são interfaces nomeadas públicas no Modulith.
- `usuarios.internal.infrastructure.controller.LoginController`: injeta LoginUseCase e retorna HTTP 200 após sucesso.
- `internal.core.LoginUseCase.execute(LoginRequest)` e implementação Login: sem Spring/JPA; única dependência injetada é AutenticacaoGateway.
- `internal.core.AutenticacaoGateway.autenticar(email, senha)`: retorna normalmente no sucesso ou lança CredenciaisInvalidasException.
- `internal.infrastructure.DatabaseAutenticacaoGateway`: consulta o repository JPA do write em transação somente leitura e valida status e hash. A consulta limitada a duas linhas rejeita ambiguidades legadas de caixa no email.
- `UsuariosConfiguration`: monta o caso de uso e compartilha PasswordEncoder entre cadastro e login.

A comparação usa hashes PBKDF2 já gravados pelo cadastro, não texto puro. O encoder também reconhece `{bcrypt}`; o TODO registra onde trocar o algoritmo de novos hashes preservando a leitura dos antigos. Hashes desconhecidos ou sem identificação não são aceitos. Para contas inexistentes/sem senha utiliza-se um hash simulado para executar a verificação sem retornar imediatamente.

## Escopo e evolução

Este fluxo apenas verifica credenciais. Não cria sessão, JWT, cookie de autenticação, SecurityContext nem autorização de endpoints. Não publica eventos de cadastro ou altera os bancos. Não são necessárias migrations.

Uma futura implementação da porta pode delegar autenticação local ao Spring Security. Google/Facebook exigem fluxo próprio com redirecionamento, callback e verificação do provedor; a assinatura email/senha não substitui OAuth2. Não tratar email fornecido pelo cliente como prova de identidade externa e não vincular contas automaticamente por email coincidente.

## Testes

```bash
./mvnw -B -ntp -Dtest=LoginTests,LoginHttpTests,UsuariosArchitectureTests,ModulithStructureTests,CriarUsuarioTests test
```

Os testes cobrem normalização, delegação, falha, hash de cadastro, BCrypt, conta inativa/social/inexistente, email ambíguo e contratos HTTP. `UsuariosIntegrationTests`, habilitado por `USUARIOS_INTEGRATION_TEST=true` em bancos isolados, também realiza login de PF/PJ imediatamente após cadastro e rejeita senha incorreta.
