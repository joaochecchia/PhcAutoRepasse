package repasse.phcauto.backend.fipe.internal.core.domain;
import java.util.List;
/** Todas as opções de um nível do catálogo; pendente quando o fornecedor falhou e a carga foi agendada em segundo plano. */
public record CargaOpcoesFipe(List<OpcaoFipe> opcoes,boolean pendente) {
    public CargaOpcoesFipe { opcoes=List.copyOf(opcoes); }
}
