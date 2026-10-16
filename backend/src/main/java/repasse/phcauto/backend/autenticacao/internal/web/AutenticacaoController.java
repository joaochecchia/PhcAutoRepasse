package repasse.phcauto.backend.autenticacao.internal.web;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.security.MessageDigest;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repasse.phcauto.backend.autenticacao.DispositivosConfiaveis;
import repasse.phcauto.backend.autenticacao.internal.security.*;
import repasse.phcauto.backend.autenticacao.internal.service.*;
import repasse.phcauto.backend.usuarios.*;

@RestController
@RequestMapping("/api/v1/autenticacao")
public class AutenticacaoController {
    private final SegurancaUsuarios usuarios;
    private final SessoesUsuarios sessoes;
    private final TwoFactorChallengeService desafios;
    private final TrustedDeviceService dispositivos;
    private final AuthenticationCookieService authCookies;
    private final DeviceCookieService deviceCookies;
    public AutenticacaoController(SegurancaUsuarios usuarios,SessoesUsuarios sessoes,
            TwoFactorChallengeService desafios,TrustedDeviceService dispositivos,
            AuthenticationCookieService authCookies,DeviceCookieService deviceCookies){
        this.usuarios=usuarios;this.sessoes=sessoes;this.desafios=desafios;this.dispositivos=dispositivos;
        this.authCookies=authCookies;this.deviceCookies=deviceCookies;
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,HttpServletRequest http){
        var usuario=usuarios.autenticar(request.email(),request.senha());
        String device=cookie(http,deviceCookies.name());
        if(!desafios.enabled()||dispositivos.confiavel(device,usuario.id(),usuario.versaoSeguranca(),http.getHeader("User-Agent"))){
            return sessao(usuario.id(),usuario.papel(),null);
        }
        var desafio=desafios.criar(usuario,http.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).header("Cache-Control","no-store")
                .body(new LoginResponse("SEGUNDO_FATOR_NECESSARIO",null,null,0,
                        desafio.challengeId(),desafio.expiresIn(),desafio.emailMascarado()));
    }
    @PostMapping("/2fa/verificar")
    public ResponseEntity<LoginResponse> verificar(@Valid @RequestBody VerifyRequest request,HttpServletRequest http){
        var dados=desafios.validar(request.challengeId(),request.codigo());
        String agenteHash=SecurityHashes.sha256(http.getHeader("User-Agent")==null?"desconhecido":http.getHeader("User-Agent"));
        if(!MessageDigest.isEqual(agenteHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                dados.agenteHash().getBytes(java.nio.charset.StandardCharsets.UTF_8))) throw new TwoFactorChallengeService.CodigoInvalidoException();
        var atual=usuarios.buscarAtivo(dados.usuarioId());
        if(atual.versaoSeguranca()!=dados.versao()) throw new TwoFactorChallengeService.CodigoInvalidoException();
        String tokenDispositivo=request.confiarNesteDispositivo()
                ?dispositivos.criar(atual.id(),atual.versaoSeguranca(),http.getHeader("User-Agent")).token():null;
        return sessao(atual.id(),atual.papel(),tokenDispositivo);
    }
    @PostMapping("/2fa/reenviar")
    public ResponseEntity<LoginResponse> reenviar(@Valid @RequestBody ResendRequest request,HttpServletRequest http){
        var desafio=desafios.reenviar(request.challengeId(),http.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).header("Cache-Control","no-store")
                .body(new LoginResponse("SEGUNDO_FATOR_NECESSARIO",null,null,0,
                        desafio.challengeId(),desafio.expiresIn(),desafio.emailMascarado()));
    }
    @GetMapping("/dispositivos")
    public List<DispositivosConfiaveis.Dispositivo> listar(Authentication auth,HttpServletRequest request){
        return dispositivos.listar(UUID.fromString(auth.getName()),cookie(request,deviceCookies.name()));
    }
    @DeleteMapping("/dispositivos/{id}")
    public ResponseEntity<Void> revogar(Authentication auth,@PathVariable UUID id){
        dispositivos.revogar(UUID.fromString(auth.getName()),id);return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/dispositivos")
    public ResponseEntity<Void> revogarTodos(Authentication auth){
        dispositivos.revogarTodos(UUID.fromString(auth.getName()));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,deviceCookies.remover()).build();
    }
    private ResponseEntity<LoginResponse> sessao(UUID id,String papel,String deviceToken){
        var emitida=sessoes.iniciar(id,papel); var resposta=ResponseEntity.ok().header("Cache-Control","no-store")
                .header(HttpHeaders.SET_COOKIE,authCookies.access(emitida.accessToken(),emitida.accessExpiresIn()))
                .header(HttpHeaders.SET_COOKIE,authCookies.refresh(emitida.refreshToken(),emitida.refreshExpiresIn()));
        if(deviceToken!=null) resposta.header(HttpHeaders.SET_COOKIE,deviceCookies.criar(deviceToken));
        return resposta.body(new LoginResponse("AUTENTICADO",id,papel,emitida.accessExpiresIn(),null,0,null));
    }
    private static String cookie(HttpServletRequest request,String name){if(request.getCookies()!=null)
        for(var c:request.getCookies())if(name.equals(c.getName()))return c.getValue();return null;}
    public record LoginRequest(@Email @NotBlank @Size(max=254) String email,
            @NotBlank @Size(max=128) String senha){@Override public String toString(){return "LoginRequest[protegido]";}}
    public record VerifyRequest(@NotBlank String challengeId,@Pattern(regexp="\\d{6}") String codigo,
            boolean confiarNesteDispositivo){@Override public String toString(){return "VerifyRequest[protegido]";}}
    public record ResendRequest(@NotBlank @Size(max=100) String challengeId){}
    public record LoginResponse(String status,UUID usuarioId,String papel,long expiresIn,
            String challengeId,long challengeExpiresIn,String emailMascarado){}
}
