package repasse.phcauto.backend.autenticacao.internal.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.autenticacao.DispositivosConfiaveis;
import repasse.phcauto.backend.autenticacao.internal.persistence.*;
import repasse.phcauto.backend.autenticacao.internal.security.*;

@Service
public class TrustedDeviceService implements DispositivosConfiaveis {
    private final DispositivoConfiavelRepository dispositivos;
    private final DeviceCookieService cookies;
    private final Clock clock;
    private final SecureRandom random=new SecureRandom();
    public TrustedDeviceService(DispositivoConfiavelRepository dispositivos, DeviceCookieService cookies, Clock clock) {
        this.dispositivos=dispositivos; this.cookies=cookies; this.clock=clock;
    }
    @Transactional(transactionManager="writeTransactionManager")
    public boolean confiavel(String token, UUID usuario, long versao, String userAgent) {
        if(token==null||token.isBlank()) return false;
        var agora=clock.instant();
        return dispositivos.findByTokenHash(SecurityHashes.sha256(token)).filter(d -> d.valida(usuario,versao,
                SecurityHashes.sha256(normalizarAgente(userAgent)),agora)).map(d->{d.usado(agora);return true;}).orElse(false);
    }
    @Transactional(transactionManager="writeTransactionManager")
    public TokenDispositivo criar(UUID usuario,long versao,String userAgent) {
        byte[] bytes=new byte[32]; random.nextBytes(bytes);
        String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String agente=normalizarAgente(userAgent);
        var agora=clock.instant();
        var salvo=dispositivos.save(DispositivoConfiavelEntity.criar(usuario,SecurityHashes.sha256(token),
                SecurityHashes.sha256(agente),nome(agente),versao,agora,agora.plus(cookies.ttl())));
        return new TokenDispositivo(salvo.getId(),token);
    }
    @Override @Transactional(transactionManager="writeTransactionManager",readOnly=true)
    public List<Dispositivo> listar(UUID usuarioId,String tokenAtual){
        String hash=tokenAtual==null?null:SecurityHashes.sha256(tokenAtual);
        return dispositivos.findByUsuarioIdOrderByUltimoUsoEmDesc(usuarioId).stream()
                .filter(d->d.getRevogadoEm()==null && d.getExpiraEm().isAfter(clock.instant()))
                .map(d->new Dispositivo(d.getId(),d.getNomeDispositivo(),d.getCriadoEm(),d.getUltimoUsoEm(),
                        d.getExpiraEm(),Objects.equals(hash,d.getTokenHash()))).toList();
    }
    @Override @Transactional(transactionManager="writeTransactionManager")
    public void revogar(UUID usuarioId,UUID dispositivoId){dispositivos.findById(dispositivoId)
            .filter(d->d.getUsuarioId().equals(usuarioId)).ifPresent(d->d.revogar(clock.instant()));}
    @Override @Transactional(transactionManager="writeTransactionManager")
    public void revogarTodos(UUID usuarioId){dispositivos.findByUsuarioIdOrderByUltimoUsoEmDesc(usuarioId)
            .forEach(d->d.revogar(clock.instant()));}
    private static String normalizarAgente(String value){
        if(value==null||value.isBlank()) return "desconhecido";
        return value.strip().substring(0,Math.min(200,value.strip().length()));
    }
    private static String nome(String agente){return agente.substring(0,Math.min(120,agente.length()));}
    public record TokenDispositivo(UUID id,String token) { }
}
