package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.response;
import java.time.LocalDate;
import java.util.List;
import repasse.phcauto.backend.fipe.internal.core.domain.OpcaoFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.PaginaOpcoesFipe;
public record OpcoesFipeResponse(List<OpcaoResponse> opcoes,int pagina,int tamanho,boolean temProximaPagina,boolean catalogoPendente) {
    /** anoId: ano/combustível FIPE do modelo, presente somente na lista de modelos por ano. */
    public record OpcaoResponse(String codigo,String nome,LocalDate mesReferencia,String anoId) {
        public OpcaoResponse(String codigo,String nome,LocalDate mesReferencia){this(codigo,nome,mesReferencia,null);}
        public static OpcaoResponse de(OpcaoFipe o){return new OpcaoResponse(o.codigo(),o.nome(),o.mesReferencia(),o.anoId());}
    }
    public static OpcoesFipeResponse de(PaginaOpcoesFipe p){
        return new OpcoesFipeResponse(p.opcoes().stream().map(OpcaoResponse::de).toList(),p.pagina(),p.tamanho(),p.temProximaPagina(),p.catalogoPendente());
    }
}
