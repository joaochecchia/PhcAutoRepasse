package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.security.SocialRegistrationTicketService;

@RestControllerAdvice
class SocialAuthenticationExceptionHandler {
    @ExceptionHandler(SocialRegistrationTicketService.TicketInvalidoException.class)
    ResponseEntity<ProblemDetail> ticketInvalido() {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.GONE,
                "O cadastro social expirou. Entre novamente com Google.");
        problem.setTitle("Cadastro social expirado");
        return ResponseEntity.status(HttpStatus.GONE).body(problem);
    }
}
