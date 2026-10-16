package repasse.phcauto.backend.credenciais.internal.service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import repasse.phcauto.backend.autenticacao.DispositivosConfiaveis;
import repasse.phcauto.backend.credenciais.SenhaAlterada;
import repasse.phcauto.backend.credenciais.internal.persistence.*;
import repasse.phcauto.backend.usuarios.SegurancaUsuarios;
import repasse.phcauto.backend.usuarios.UsuarioSeguranca;
import repasse.phcauto.backend.infra.security.ratelimit.RateLimiter;
@Service
public class CredenciaisService {
    private final TokenRedefinicaoSenhaRepository tokens; private final SegurancaUsuarios usuarios;
    private final DispositivosConfiaveis dispositivos; private final EmailCredenciaisDispatcher emails;
    private final ApplicationEventPublisher eventos; private final Clock clock; private final Duration ttl;
    private final RateLimiter rateLimiter;
    private final String frontendUrl; private final SecureRandom random=new SecureRandom();
    public CredenciaisService(TokenRedefinicaoSenhaRepository tokens,SegurancaUsuarios usuarios,
            DispositivosConfiaveis dispositivos,EmailCredenciaisDispatcher emails,ApplicationEventPublisher eventos,
            RateLimiter rateLimiter,
            Clock clock,@Value("${app.security.password-reset.ttl:20m}") Duration ttl,
            @Value("${app.security.password-reset.frontend-url:http://localhost:5173/#redefinir-senha}") String frontendUrl,
            @Value("${app.security.production:false}") boolean production){
        if(production&&!frontendUrl.startsWith("https://"))throw new IllegalStateException("URL de redefinição deve usar HTTPS");
        this.tokens=tokens;this.usuarios=usuarios;this.dispositivos=dispositivos;this.emails=emails;
        this.eventos=eventos;this.rateLimiter=rateLimiter;this.clock=clock;this.ttl=ttl;this.frontendUrl=frontendUrl;
    }
    @Transactional(transactionManager="writeTransactionManager")
    public Optional<UsuarioSeguranca> contaParaRedefinicao(String email){
        rateLimiter.consumir("redefinicao-senha-conta", email.strip().toLowerCase(Locale.ROOT), 3, Duration.ofHours(1));
        return usuarios.buscarAtivoPorEmail(email).filter(UsuarioSeguranca::possuiSenhaLocal);
    }
    @Transactional(transactionManager="writeTransactionManager")
    public String autorizar(UUID usuarioId,String ip){
        validarContaParaTroca(usuarioId);var agora=clock.instant();tokens.revogarDoUsuario(usuarioId,agora);
        String token=token();tokens.save(TokenRedefinicaoSenhaEntity.criar(usuarioId,hash(token),hash(ip==null?"":ip),agora,agora.plus(ttl)));
        return token;
    }
    @Transactional(transactionManager="writeTransactionManager")
    public void confirmar(String token,String novaSenha){var agora=clock.instant();var entidade=tokens.buscarParaAtualizar(hash(token))
            .filter(t->t.valido(agora)).orElseThrow(TokenInvalidoException::new);entidade.usar(agora);
        var usuario=usuarios.buscarAtivo(entidade.getUsuarioId());usuarios.redefinirSenha(usuario.id(),novaSenha);
        dispositivos.revogarTodos(usuario.id());tokens.revogarDoUsuario(usuario.id(),agora);
        eventos.publishEvent(new SenhaAlterada(UUID.randomUUID(),usuario.id(),SenhaAlterada.Motivo.REDEFINICAO_EMAIL,agora));
        depoisDoCommit(()->emails.senhaAlterada(usuario.email()));}
    public UsuarioSeguranca validarContaParaTroca(UUID usuarioId){
        var usuario=usuarios.buscarAtivo(usuarioId);
        if(!usuario.possuiSenhaLocal()) throw new repasse.phcauto.backend.usuarios.CredencialInvalidaException(
                "A conta autenticada pelo Google não possui senha local");
        return usuario;
    }
    @Transactional(transactionManager="writeTransactionManager")
    public void trocarAposValidacao(UUID usuarioId,String novaSenha){var usuario=usuarios.buscarAtivo(usuarioId);
        usuarios.redefinirSenha(usuarioId,novaSenha);dispositivos.revogarTodos(usuarioId);
        var agora=clock.instant();tokens.revogarDoUsuario(usuarioId,agora);
        eventos.publishEvent(new SenhaAlterada(UUID.randomUUID(),usuarioId,SenhaAlterada.Motivo.TROCA_AUTENTICADA,agora));
        depoisDoCommit(()->emails.senhaAlterada(usuario.email()));}
    private String token(){byte[] b=new byte[32];random.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
    private static String hash(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));}
        catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private static void depoisDoCommit(Runnable acao){
        if(!TransactionSynchronizationManager.isSynchronizationActive()){acao.run();return;}
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override public void afterCommit(){acao.run();}
        });
    }
    public static class TokenInvalidoException extends RuntimeException{}
}
