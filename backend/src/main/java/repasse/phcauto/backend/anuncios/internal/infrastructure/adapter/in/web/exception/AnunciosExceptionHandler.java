package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.exception;

import java.net.URI;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncianteInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioNaoEncontradoException;
import repasse.phcauto.backend.anuncios.internal.core.exception.ExclusaoAnuncioBloqueadaException;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.controller.AnunciosController;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail beanValidation(MethodArgumentNotValidException ex) {
        var erros = new java.util.LinkedHashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.putIfAbsent(erro.getField(), mensagemValidacao(erro.getCode(), erro.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(erro ->
                erros.putIfAbsent(erro.getObjectName(), mensagemValidacao(erro.getCode(), erro.getDefaultMessage())));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Existem campos inválidos no anúncio");
        problem.setTitle("Falha de validação");
        problem.setType(URI.create("urn:phcauto:validacao-anuncio"));
        problem.setProperty("mensagem", "Corrija os campos informados e tente novamente");
        problem.setProperty("erros", erros);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail constraintViolation(ConstraintViolationException ex) {
        var erros = new java.util.LinkedHashMap<String, String>();
        ex.getConstraintViolations().forEach(violacao ->
                erros.putIfAbsent(violacao.getPropertyPath().toString(), violacao.getMessage()));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Existem campos inválidos no anúncio");
        problem.setTitle("Falha de validação");
        problem.setType(URI.create("urn:phcauto:validacao-anuncio"));
        problem.setProperty("mensagem", "Corrija os campos informados e tente novamente");
        problem.setProperty("erros", erros);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail corpoInvalido(HttpMessageNotReadableException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente, malformado ou contém um valor incompatível");
        problem.setTitle("Requisição inválida");
        problem.setType(URI.create("urn:phcauto:requisicao-anuncio-invalida"));
        problem.setProperty("mensagem", "Verifique o JSON e os valores dos campos");
        return problem;
    }

    private static String mensagemValidacao(String codigo, String mensagemPadrao) {
        if (codigo == null) return mensagemPadrao;
        return switch (codigo) {
            case "NotNull", "NotBlank", "NotEmpty" -> "Campo obrigatório";
            case "Positive" -> "Deve ser maior que zero";
            case "PositiveOrZero" -> "Não pode ser negativo";
            case "Size" -> "Tamanho inválido";
            case "Pattern" -> "Formato inválido";
            case "AssertTrue" -> mensagemPadrao;
            default -> mensagemPadrao;
        };
    }

}
