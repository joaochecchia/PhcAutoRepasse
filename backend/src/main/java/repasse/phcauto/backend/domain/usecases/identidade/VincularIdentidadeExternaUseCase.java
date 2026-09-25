package repasse.phcauto.backend.domain.usecases.identidade;

import repasse.phcauto.backend.domain.model.identidade.IdentidadeExterna;

/**
 * Vincula uma identidade externa verificada a um usuário existente e autorizado.
 * Provedor e identificador externo devem vir de autenticação validada pelo adaptador,
 * nunca apenas de valores enviados pelo cliente. Não vincula por coincidência de email.
 * Repetir o mesmo vínculo retorna o existente; uma identidade vinculada a outro
 * usuário deve ser rejeitada, sem transferir a conta. Não cria sessão ou tokens.
 * Dados, usuarioId, provedor e identificadorExterno são obrigatórios.
 */
public interface VincularIdentidadeExternaUseCase {

    IdentidadeExterna executar(IdentidadeExterna identidadeVerificada);
}
