package repasse.phcauto.backend.domain.usecases.identidade;

import repasse.phcauto.backend.domain.model.identidade.EnderecoUsuario;

/**
 * Cria ou substitui os dados complementares do usuário identificado no modelo.
 * A implementação deve verificar autorização e existência do usuário e, quando
 * aplicável, a compatibilidade do perfil PF/PJ antes de salvar.
 * Não conclui cadastro nem compra; a validação de cada etapa pertence ao seu fluxo.
 * Dados e identificador do usuário não podem ser nulos.
 */
public interface SalvarEnderecoUsuarioUseCase {

    EnderecoUsuario executar(EnderecoUsuario dados);
}
