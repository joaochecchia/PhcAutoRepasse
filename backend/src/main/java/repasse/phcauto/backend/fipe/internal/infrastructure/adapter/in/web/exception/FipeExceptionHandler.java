package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.exception;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
@RestControllerAdvice(basePackages="repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.controller")
public class FipeExceptionHandler {
    @ExceptionHandler(FipeException.class) public ResponseEntity<ProblemDetail> tratar(FipeException e){
        var status=switch(e.motivo()){case INVALIDA->HttpStatus.BAD_REQUEST;case NAO_ENCONTRADA->HttpStatus.NOT_FOUND;
            case INDISPONIVEL->HttpStatus.SERVICE_UNAVAILABLE;case LIMITE->HttpStatus.TOO_MANY_REQUESTS;case CONFLITO->HttpStatus.CONFLICT;};
        var p=ProblemDetail.forStatusAndDetail(status,e.getMessage());p.setTitle("Consulta FIPE");
        var resposta=ResponseEntity.status(status);
        if(status==HttpStatus.TOO_MANY_REQUESTS || status==HttpStatus.SERVICE_UNAVAILABLE)resposta.header("Retry-After","60");
        return resposta.body(p);
    }
    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ProblemDetail invalida(Exception e){return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Verifique os campos da requisição FIPE");}
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail conflito(Exception e){return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"Importação ou referência FIPE duplicada ou incompatível");}
}
