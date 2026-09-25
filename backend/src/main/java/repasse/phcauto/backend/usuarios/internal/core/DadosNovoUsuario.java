package repasse.phcauto.backend.usuarios.internal.core;

import java.time.LocalDate;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record DadosNovoUsuario(TipoPessoa tipoPessoa, String nome, String email,
        String telefone, String senhaHash, String cpf, LocalDate dataNascimento,
        String cnpj, String razaoSocial, EnderecoCadastro endereco) {
    @Override public String toString() { return "DadosNovoUsuario[conteudo protegido]"; }
}
