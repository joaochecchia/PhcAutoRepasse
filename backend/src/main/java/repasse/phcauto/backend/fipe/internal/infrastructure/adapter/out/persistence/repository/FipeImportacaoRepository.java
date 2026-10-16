package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity.FipeImportacaoEntity;
public interface FipeImportacaoRepository extends JpaRepository<FipeImportacaoEntity,UUID> {
    Optional<FipeImportacaoEntity> findByChecksumAndFonteAndMesReferencia(String checksum,String fonte,java.time.LocalDate mes);
    @Query(value="select * from fipe.importacoes where status in ('PENDENTE','PROCESSANDO') order by criado_em limit 1 for update skip locked",nativeQuery=true)
    Optional<FipeImportacaoEntity> proximoLote();
}
