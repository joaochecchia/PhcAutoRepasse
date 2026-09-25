package repasse.phcauto.backend.usuarios.internal.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.controller.UsuariosController;
import repasse.phcauto.backend.usuarios.internal.core.*;

@RestControllerAdvice(assignableTypes = UsuariosController.class)
class UsuariosExceptionHandler {
    @ExceptionHandler({CadastroDuplicadoException.class, ExclusaoUsuarioBloqueadaException.class})
    ProblemDetail conflito(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    ProblemDetail naoEncontrado(UsuarioNaoEncontradoException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({CadastroInvalidoException.class, IllegalArgumentException.class})
    ProblemDetail invalido(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ProblemDetail requestInvalido(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados de usuário inválidos");
    }
}
