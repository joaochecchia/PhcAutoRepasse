package repasse.phcauto.backend.fipe.internal.core.domain;
import java.util.List;
/** catalogoPendente: o escopo ainda não foi carregado do fornecedor e a carga foi agendada em segundo plano. */
public record PaginaOpcoesFipe(List<OpcaoFipe> opcoes,int pagina,int tamanho,boolean temProximaPagina,boolean catalogoPendente) {
    public PaginaOpcoesFipe(List<OpcaoFipe> opcoes,int pagina,int tamanho,boolean temProximaPagina) {
        this(opcoes,pagina,tamanho,temProximaPagina,false);
    }
}
