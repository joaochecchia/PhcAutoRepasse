package repasse.phcauto.backend.planos.internal.infrastructure.controller;

import java.net.URI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import repasse.phcauto.backend.planos.internal.core.PlanoInvalidoException;
import repasse.phcauto.backend.planos.internal.core.PlanoNaoEncontradoException;

@RestControllerAdvice(assignableTypes = PlanosController.class)
class PlanosExceptionHandler {
    @ExceptionHandler(PlanoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(PlanoNaoEncontradoException error) {
        return problema(HttpStatus.NOT_FOUND, "Plano não encontrado", error.getMessage());
    }
    @ExceptionHandler(PlanoInvalidoException.class)
    ProblemDetail invalido(PlanoInvalidoException error) {
        return problema(HttpStatus.BAD_REQUEST, "Plano inválido", error.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail bodyInvalido(MethodArgumentNotValidException error) {
        var detalhe = error.getBindingResult().getFieldErrors().stream().findFirst()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .orElse("Dados do plano são inválidos");
        return problema(HttpStatus.BAD_REQUEST, "Plano inválido", detalhe);
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail parametroInvalido(HandlerMethodValidationException error) {
        return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido", "Revise offset, limite e identificadores informados");
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflito(DataIntegrityViolationException error) {
        return problema(HttpStatus.CONFLICT, "Operação não permitida",
                "O nome já está em uso ou o plano possui registros dependentes");
    }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail concorrencia(ObjectOptimisticLockingFailureException error) {
        return problema(HttpStatus.CONFLICT, "Plano alterado concorrentemente",
                "Recarregue o plano e tente novamente");
    }
    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        var problem = ProblemDetail.forStatusAndDetail(status, detalhe);
        problem.setTitle(titulo);
        problem.setType(URI.create("urn:phcauto:planos:" + status.value()));
        return problem;
    }
}
