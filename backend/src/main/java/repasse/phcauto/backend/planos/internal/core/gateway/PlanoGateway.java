package repasse.phcauto.backend.planos.internal.core.gateway;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import repasse.phcauto.backend.planos.internal.core.domain.NovoPlano;
import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;
public interface PlanoGateway {
    PlanoDados criar(UUID id, NovoPlano plano, Instant criadoEm);
    Optional<PlanoDados> buscar(UUID id);
    List<PlanoDados> listar(int offset, int limite);
    long contar();
    PlanoDados atualizar(UUID id, PlanoDados plano);
    boolean excluir(UUID id);
    boolean existeNome(String nome, UUID ignorarId);
}
