package repasse.phcauto.backend.credenciais.internal.service;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.notificacoes.EmailService;
@Component
class EmailCredenciaisDispatcher {
    private static final Logger log=LoggerFactory.getLogger(EmailCredenciaisDispatcher.class);
    private final EmailService emails;
    EmailCredenciaisDispatcher(EmailService emails){this.emails=emails;}
    @Async public void redefinicao(String email,String link,long minutos){try{emails.enviarRedefinicaoSenha(email,link,minutos);}
        catch(RuntimeException e){log.error("Falha ao enviar e-mail de redefinição de senha",e);}}
    @Async public void senhaAlterada(String email){try{emails.enviarAvisoSenhaAlterada(email);}
        catch(RuntimeException e){log.error("Falha ao enviar aviso de senha alterada",e);}}
}
