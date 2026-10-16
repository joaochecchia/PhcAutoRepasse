package repasse.phcauto.backend.credenciais.internal.web;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.credenciais.internal.service.CredenciaisService;
import repasse.phcauto.backend.usuarios.CredencialInvalidaException;
import repasse.phcauto.backend.usuarios.FalhaAutenticacaoException;
import repasse.phcauto.backend.autenticacao.SegundoFator;
@RestControllerAdvice(assignableTypes=CredenciaisController.class)
class CredenciaisExceptionHandler {
    @ExceptionHandler(CredenciaisService.TokenInvalidoException.class)
    ProblemDetail token(){return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Token inválido ou expirado");}
    @ExceptionHandler(FalhaAutenticacaoException.class)
    ProblemDetail senha(){return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,"Senha atual inválida");}
    @ExceptionHandler({CredencialInvalidaException.class,IllegalArgumentException.class})
    ProblemDetail invalido(RuntimeException e){return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.getMessage());}
    @ExceptionHandler(SegundoFator.CodigoInvalidoException.class)
    ProblemDetail codigo(){return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,"Código inválido ou expirado");}
    @ExceptionHandler(CredenciaisController.SegundoFatorIndisponivelException.class)
    ProblemDetail segundoFatorIndisponivel(){return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
            "A verificação em duas etapas está temporariamente indisponível");}
}
