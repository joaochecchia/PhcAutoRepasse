package repasse.phcauto.backend.domain.usecases.identidade;

import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.DadosCompraPf;

/**
 * Consulta os dados pelo usuário, respeitando a autorização de acesso.
 * Retorna vazio se ainda não foram preenchidos. usuarioId não pode ser nulo.
 */
public interface BuscarDadosCompraPfPorUsuarioIdUseCase {

    Optional<DadosCompraPf> executar(UUID usuarioId);
}
