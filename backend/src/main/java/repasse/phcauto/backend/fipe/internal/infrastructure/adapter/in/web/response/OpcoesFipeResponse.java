package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.response;
import java.time.LocalDate;
import java.util.List;
public record OpcoesFipeResponse(List<OpcaoResponse> opcoes,int pagina,int tamanho,boolean temProximaPagina,boolean catalogoPendente) {
    public record OpcaoResponse(String codigo,String nome,LocalDate mesReferencia) { }
}
