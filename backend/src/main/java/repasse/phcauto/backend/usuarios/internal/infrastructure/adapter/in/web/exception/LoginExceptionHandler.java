package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller.LoginController;
import repasse.phcauto.backend.usuarios.internal.core.exception.CredenciaisInvalidasException;
import repasse.phcauto.backend.infra.security.ratelimit.RateLimitExceededException;

@RestControllerAdvice(assignableTypes = LoginController.class)
public class LoginExceptionHandler {
    @ExceptionHandler(RateLimitExceededException.class)
    org.springframework.http.ResponseEntity<ProblemDetail> muitasTentativas(RateLimitExceededException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Muitas tentativas de autenticação. Aguarde antes de tentar novamente");
        return org.springframework.http.ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(exception.retryAfterSeconds())).body(problem);
    }
    @ExceptionHandler(CredenciaisInvalidasException.class)
    ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    ProblemDetail requestInvalido(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados de login inválidos");
    }
}
