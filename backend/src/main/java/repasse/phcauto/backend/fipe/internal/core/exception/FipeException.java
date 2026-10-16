package repasse.phcauto.backend.fipe.internal.core.exception;
public class FipeException extends RuntimeException {
    public enum Motivo { INVALIDA, NAO_ENCONTRADA, INDISPONIVEL, LIMITE, CONFLITO }
    private final Motivo motivo;
    public FipeException(Motivo motivo, String mensagem) { super(mensagem); this.motivo = motivo; }
    public Motivo motivo() { return motivo; }
}
