package repasse.phcauto.backend.localizacao.internal.core.exception;

public class LocalizacaoNaoEncontradaException extends RuntimeException {
    public LocalizacaoNaoEncontradaException(String mensagem) { super(mensagem); }
    public LocalizacaoNaoEncontradaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
