package repasse.phcauto.backend.domain.usecases.crud;

/**
 * Solicita exclusão pela chave completa. Retorna true se removido e false se inexistente. A implementação deve respeitar autorização, integridade referencial e retenção de histórico; este contrato não autoriza remoção irrestrita.
 * Argumentos de referência não podem ser null.
 */
public interface ExcluirUseCase<ID> {

    boolean excluirPorId(ID id);
}
