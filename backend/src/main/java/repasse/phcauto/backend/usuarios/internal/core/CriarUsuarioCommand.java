package repasse.phcauto.backend.usuarios.internal.core;

import java.time.LocalDate;
import java.util.Locale;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record CriarUsuarioCommand(TipoPessoa tipoPessoa, String nome, String email,
        String telefone, String senha, String cpf, LocalDate dataNascimento,
        String cnpj, String razaoSocial, EnderecoCadastro endereco) {
    public CriarUsuarioCommand {
        if (tipoPessoa == null || endereco == null) throw new CadastroInvalidoException("Tipo de pessoa e endereço obrigatórios");
        nome = ValidacaoCadastro.texto(nome, "Nome", 160);
        email = ValidacaoCadastro.texto(email, "Email", 254).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new CadastroInvalidoException("Email inválido");
        telefone = ValidacaoCadastro.texto(telefone, "Telefone", 32);
        if (!telefone.matches("\\+?[1-9][0-9]{9,14}")) throw new CadastroInvalidoException("Telefone inválido");
        if (senha == null || senha.isBlank() || senha.length() < 8 || senha.length() > 128) {
            throw new CadastroInvalidoException("Senha deve ter entre 8 e 128 caracteres");
        }
        if (tipoPessoa == TipoPessoa.PF) {
            if (!ValidacaoCadastro.documentoValido(cpf, true)) throw new CadastroInvalidoException("CPF inválido");
            if (dataNascimento == null) throw new CadastroInvalidoException("Nascimento obrigatório");
            if (cnpj != null || razaoSocial != null) throw new CadastroInvalidoException("PF não aceita dados de PJ");
        } else {
            if (!ValidacaoCadastro.documentoValido(cnpj, false)) throw new CadastroInvalidoException("CNPJ inválido");
            razaoSocial = ValidacaoCadastro.texto(razaoSocial, "Razão social", 200);
            if (cpf != null || dataNascimento != null) throw new CadastroInvalidoException("PJ não aceita dados de PF");
        }
    }
    @Override public String toString() { return "CriarUsuarioCommand[conteudo protegido]"; }
}
