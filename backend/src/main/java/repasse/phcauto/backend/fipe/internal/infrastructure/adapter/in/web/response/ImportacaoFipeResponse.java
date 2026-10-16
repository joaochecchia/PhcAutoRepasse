package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.in.web.response;
import java.util.UUID;
public record ImportacaoFipeResponse(UUID id,String status,long registros,String erro) { }
