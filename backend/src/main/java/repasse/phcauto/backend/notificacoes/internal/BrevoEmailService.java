package repasse.phcauto.backend.notificacoes.internal;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import repasse.phcauto.backend.notificacoes.EmailService;

@Component
class BrevoEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);
    private final RestClient client;
    private final boolean enabled;
    private final String remetenteEmail;
    private final String remetenteNome;
    private final long template2fa;
    private final long templateReset;
    private final long templateAlterada;

    BrevoEmailService(@Value("${app.email.brevo.enabled:false}") boolean enabled,
            @Value("${app.email.brevo.api-key:}") String apiKey,
            @Value("${app.email.brevo.sender-email:}") String remetenteEmail,
            @Value("${app.email.brevo.sender-name:PHC Auto}") String remetenteNome,
            @Value("${app.email.brevo.template-2fa-id:0}") long template2fa,
            @Value("${app.email.brevo.template-password-reset-id:0}") long templateReset,
            @Value("${app.email.brevo.template-password-changed-id:0}") long templateAlterada) {
        if (enabled && (apiKey.isBlank() || remetenteEmail.isBlank()
                || template2fa <= 0 || templateReset <= 0 || templateAlterada <= 0)) {
            throw new IllegalStateException("Brevo habilitada exige API key, remetente e IDs dos três templates");
        }
        this.enabled = enabled;
        this.remetenteEmail = remetenteEmail;
        this.remetenteNome = remetenteNome;
        this.template2fa = template2fa;
        this.templateReset = templateReset;
        this.templateAlterada = templateAlterada;
        this.client = RestClient.builder().baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey).build();
    }

    @Override public void enviarCodigoSegundoFator(String destinatario, String codigo, long minutosValidade) {
        enviar(destinatario, template2fa, Map.of("codigo", codigo, "minutosValidade", minutosValidade));
    }

    @Override public void enviarRedefinicaoSenha(String destinatario, String link, long minutosValidade) {
        enviar(destinatario, templateReset, Map.of("link", link, "minutosValidade", minutosValidade));
    }

    @Override public void enviarAvisoSenhaAlterada(String destinatario) {
        enviar(destinatario, templateAlterada, Map.of());
    }

    private void enviar(String destinatario, long templateId, Map<String, ?> parametros) {
        if (!enabled) {
            log.info("E-mail transacional suprimido porque a Brevo está desabilitada");
            return;
        }
        client.post().uri("/smtp/email").body(Map.of(
                "sender", Map.of("email", remetenteEmail, "name", remetenteNome),
                "to", java.util.List.of(Map.of("email", destinatario)),
                "templateId", templateId,
                "params", parametros)).retrieve().toBodilessEntity();
    }
}
