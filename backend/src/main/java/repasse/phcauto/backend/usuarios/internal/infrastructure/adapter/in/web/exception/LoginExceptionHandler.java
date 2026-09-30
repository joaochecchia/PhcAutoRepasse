package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.controller.LoginController;
import repasse.phcauto.backend.usuarios.internal.core.exception.CredenciaisInvalidasException;

@RestControllerAdvice(assignableTypes = LoginController.class)
public class LoginExceptionHandler {
    @ExceptionHandler(CredenciaisInvalidasException.class)
    ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    ProblemDetail requestInvalido(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados de login inválidos");
    }
}
