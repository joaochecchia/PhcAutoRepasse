package repasse.phcauto.backend.credenciais;
import java.time.Instant;
import java.util.UUID;
public record SenhaAlterada(UUID eventId,UUID usuarioId,Motivo motivo,Instant ocorridoEm){
    public enum Motivo { TROCA_AUTENTICADA, REDEFINICAO_EMAIL }
}
