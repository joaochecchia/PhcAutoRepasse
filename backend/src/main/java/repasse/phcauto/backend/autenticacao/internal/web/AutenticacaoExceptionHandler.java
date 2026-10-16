package repasse.phcauto.backend.autenticacao.internal.web;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.autenticacao.internal.service.TwoFactorChallengeService;
import repasse.phcauto.backend.usuarios.FalhaAutenticacaoException;

@RestControllerAdvice(assignableTypes=AutenticacaoController.class)
class AutenticacaoExceptionHandler {
    @ExceptionHandler(FalhaAutenticacaoException.class)
    ProblemDetail credenciais(){return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,"E-mail ou senha inválidos");}
    @ExceptionHandler(TwoFactorChallengeService.CodigoInvalidoException.class)
    ProblemDetail codigo(){return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,"Código inválido ou expirado");}
    @ExceptionHandler(org.springframework.data.redis.RedisConnectionFailureException.class)
    ProblemDetail indisponivel(){return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
            "A verificação de segurança está temporariamente indisponível");}
    @ExceptionHandler(TwoFactorChallengeService.ReenvioAguardeException.class)
    ResponseEntity<ProblemDetail> reenvio(TwoFactorChallengeService.ReenvioAguardeException error){
        var problem=ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,"Aguarde antes de solicitar outro código");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After",Long.toString(error.segundos())).body(problem);
    }
}
