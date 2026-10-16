package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository;
import java.util.*;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity.FipeReferenciaEntity;
public interface FipeReferenciaRepository extends JpaRepository<FipeReferenciaEntity,UUID> {
    String ATIVA = "(r.importacaoId is null or exists (select i.id from FipeImportacaoEntity i where i.id=r.importacaoId and i.status='CONCLUIDA'))";
    @Query("select r from FipeReferenciaEntity r where r.id=:id and " + ATIVA)
    Optional<FipeReferenciaEntity> buscarAtiva(@Param("id") UUID id);
    @Query("select r from FipeReferenciaEntity r where r.chaveConsulta=:chave and r.mesReferencia=:mes and " + ATIVA + " order by r.criadoEm desc, r.id")
    List<FipeReferenciaEntity> buscarConsulta(@Param("chave") String chave,@Param("mes") LocalDate mes,Pageable pagina);
    @Query("select r from FipeReferenciaEntity r where r.tipo=:tipo and r.codigoFipe=:codigo and r.anoModelo=:ano and r.combustivel=:combustivel and r.mesReferencia=:mes and r.importacaoId is null")
    Optional<FipeReferenciaEntity> buscarExterna(@Param("tipo")String tipo,@Param("codigo")String codigo,@Param("ano")int ano,@Param("combustivel")String combustivel,@Param("mes")LocalDate mes);
    @Query("select r from FipeReferenciaEntity r where " + ATIVA
        + " and (:tipo is null or r.tipo=:tipo) and (:marca is null or lower(r.marca)=:marca)"
        + " and (:termo is null or lower(r.modelo) like :termo escape '!')"
        + " and (:ano is null or r.anoModelo=:ano) and (:mes is null or r.mesReferencia=:mes)"
        + " order by r.mesReferencia desc,r.marca,r.modelo,r.anoModelo,r.id")
    List<FipeReferenciaEntity> pesquisar(@Param("tipo")String tipo,@Param("marca")String marca,
        @Param("termo")String termo,@Param("ano")Integer ano,@Param("mes")LocalDate mes,Pageable pagina);
}
