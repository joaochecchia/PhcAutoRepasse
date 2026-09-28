package repasse.phcauto.backend.anuncios.internal.infrastructure.controller;

import java.net.URI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import repasse.phcauto.backend.anuncios.internal.core.AnuncianteInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.AnuncioInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.AnuncioNaoEncontradoException;
import repasse.phcauto.backend.anuncios.internal.core.ExclusaoAnuncioBloqueadaException;

@RestControllerAdvice(basePackageClasses = AnunciosController.class)
class AnunciosExceptionHandler {
    @ExceptionHandler(AnuncioInvalidoException.class)
    ProblemDetail invalido(AnuncioInvalidoException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Anúncio inválido"); problem.setType(URI.create("urn:phcauto:anuncio-invalido")); return problem;
    }
    @ExceptionHandler(AnuncianteInvalidoException.class)
    ProblemDetail anunciante(AnuncianteInvalidoException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Anunciante inválido"); problem.setType(URI.create("urn:phcauto:anunciante-invalido")); return problem;
    }
    @ExceptionHandler(AnuncioNaoEncontradoException.class)
    ProblemDetail naoEncontrado(AnuncioNaoEncontradoException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Anúncio não encontrado"); problem.setType(URI.create("urn:phcauto:anuncio-nao-encontrado")); return problem;
    }
    @ExceptionHandler(ExclusaoAnuncioBloqueadaException.class)
    ProblemDetail exclusaoBloqueada(ExclusaoAnuncioBloqueadaException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Exclusão de anúncio bloqueada"); problem.setType(URI.create("urn:phcauto:exclusao-anuncio-bloqueada")); return problem;
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflito(DataIntegrityViolationException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Não foi possível alterar o anúncio com os dados informados");
        problem.setTitle("Conflito no anúncio"); problem.setType(URI.create("urn:phcauto:conflito-anuncio")); return problem;
    }
}
