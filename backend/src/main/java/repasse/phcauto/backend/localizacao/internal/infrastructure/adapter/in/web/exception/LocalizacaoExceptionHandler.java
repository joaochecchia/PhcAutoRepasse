package repasse.phcauto.backend.localizacao.internal.infrastructure.adapter.in.web.exception;

import java.net.URI;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.localizacao.internal.core.exception.LocalizacaoNaoEncontradaException;

@RestControllerAdvice
class LocalizacaoExceptionHandler {
    @ExceptionHandler(LocalizacaoNaoEncontradaException.class)
    ResponseEntity<ProblemDetail> tratar(LocalizacaoNaoEncontradaException error) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, error.getMessage());
        problema.setTitle("Localização não encontrada");
        problema.setType(URI.create("urn:phcauto:localizacao-nao-encontrada"));
        return ResponseEntity.unprocessableEntity().body(problema);
    }
}
