package repasse.phcauto.backend.domain.usecases.identidade;

import java.util.Optional;
import repasse.phcauto.backend.domain.model.identidade.IdentidadeExterna;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;

/**
 * Consulta pela chave composta do provedor e seu identificador estável de conta.
 * Retorna vazio se não existe vínculo; esta consulta não autentica o usuário.
 * Ambos os argumentos são obrigatórios e o identificador não pode ser vazio.
 * A implementação deve restringir o acesso aos dados de autenticação.
 */
public interface BuscarIdentidadeExternaPorProvedorUseCase {

    Optional<IdentidadeExterna> executar(ProvedorAutenticacao provedor, String identificadorExterno);
}
