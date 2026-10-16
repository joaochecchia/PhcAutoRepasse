package repasse.phcauto.backend.credenciais.internal.web;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.credenciais.internal.security.CredentialCookieService;
import repasse.phcauto.backend.credenciais.internal.service.CredenciaisService;
import repasse.phcauto.backend.autenticacao.SegundoFator;
@RestController @RequestMapping("/api/v1/credenciais")
public class CredenciaisController {
    private final CredenciaisService credenciais;private final CredentialCookieService cookies;private final SegundoFator segundoFator;
    public CredenciaisController(CredenciaisService credenciais,CredentialCookieService cookies,SegundoFator segundoFator){
        this.credenciais=credenciais;this.cookies=cookies;this.segundoFator=segundoFator;}
    @PostMapping("/redefinicoes")
    public ResponseEntity<TrocaSenhaResponse> solicitar(@Valid @RequestBody SolicitarRequest request,HttpServletRequest http){
        if(!segundoFator.habilitado())throw new SegundoFatorIndisponivelException();
        var conta=credenciais.contaParaRedefinicao(request.email());
        if(conta.isEmpty()) return ResponseEntity.accepted().header("Cache-Control","no-store").body(
                new TrocaSenhaResponse("SEGUNDO_FATOR_NECESSARIO",UUID.randomUUID().toString(),300,"seu e-mail",null));
        var desafio=segundoFator.criar(conta.get(),http.getHeader("User-Agent"));
        return desafio(desafio);
    }
    @PostMapping("/redefinicoes/autorizar")
    public ResponseEntity<TrocaSenhaResponse> autorizarRedefinicao(@Valid @RequestBody AutorizarRequest request,HttpServletRequest http){
        var usuario=segundoFator.validar(request.challengeId(),request.codigo(),http.getHeader("User-Agent"));
        return autorizado(credenciais.autorizar(usuario.id(),http.getRemoteAddr()));
    }
    @PostMapping("/redefinicoes/confirmar") public ResponseEntity<Void> confirmar(@Valid @RequestBody ConfirmarRequest request){
        credenciais.confirmar(request.token(),request.novaSenha());return limparCookies();}
    @PostMapping("/senha/desafio")
    public ResponseEntity<TrocaSenhaResponse> trocar(Authentication auth,
            HttpServletRequest http){
        var usuario=credenciais.validarContaParaTroca(UUID.fromString(auth.getName()));
        if(!segundoFator.habilitado())throw new SegundoFatorIndisponivelException();
        var desafio=segundoFator.criar(usuario,http.getHeader("User-Agent"));
        return desafio(desafio);
    }
    @PostMapping("/senha/autorizar")
    public ResponseEntity<TrocaSenhaResponse> confirmarTroca(@Valid @RequestBody AutorizarRequest request,
            Authentication auth,HttpServletRequest http){
        UUID usuarioId=UUID.fromString(auth.getName());var usuario=credenciais.validarContaParaTroca(usuarioId);
        segundoFator.validar(request.challengeId(),request.codigo(),usuarioId,usuario.versaoSeguranca(),http.getHeader("User-Agent"));
        return autorizado(credenciais.autorizar(usuarioId,http.getRemoteAddr()));}
    private ResponseEntity<TrocaSenhaResponse> desafio(SegundoFator.Desafio d){return ResponseEntity.accepted()
        .header("Cache-Control","no-store").body(new TrocaSenhaResponse("SEGUNDO_FATOR_NECESSARIO",d.id(),d.expiresIn(),d.emailMascarado(),null));}
    private ResponseEntity<TrocaSenhaResponse> autorizado(String token){return ResponseEntity.ok().header("Cache-Control","no-store")
        .body(new TrocaSenhaResponse("AUTORIZADA",null,0,null,token));}
    private ResponseEntity<Void> limparCookies(){var resposta=ResponseEntity.noContent().header("Cache-Control","no-store");
        cookies.removerTodos().forEach(cookie->resposta.header(HttpHeaders.SET_COOKIE,cookie));return resposta.build();}
    public record SolicitarRequest(@Email @NotBlank @Size(max=254) String email){}
    public record ConfirmarRequest(@NotBlank @Size(max=200) String token,@NotBlank @Size(max=128) String novaSenha,
            @NotBlank @Size(max=128) String confirmacaoSenha){
        public ConfirmarRequest { validarConfirmacao(novaSenha,confirmacaoSenha); }
        @Override public String toString(){return "ConfirmarRequest[protegido]";}}
    public record AutorizarRequest(@NotBlank String challengeId,@Pattern(regexp="\\d{6}") String codigo){}
    public record TrocaSenhaResponse(String status,String challengeId,long challengeExpiresIn,String emailMascarado,String token){}
    public record Mensagem(String detail){}
    public static class SegundoFatorIndisponivelException extends RuntimeException { }
    private static void validarConfirmacao(String senha,String confirmacao){
        if(!java.util.Objects.equals(senha,confirmacao))throw new IllegalArgumentException("As senhas não coincidem");}
}
