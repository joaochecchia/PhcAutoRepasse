package repasse.phcauto.backend.notificacoes;

public interface EmailService {
    void enviarCodigoSegundoFator(String destinatario, String codigo, long minutosValidade);
    void enviarRedefinicaoSenha(String destinatario, String link, long minutosValidade);
    void enviarAvisoSenhaAlterada(String destinatario);
}
