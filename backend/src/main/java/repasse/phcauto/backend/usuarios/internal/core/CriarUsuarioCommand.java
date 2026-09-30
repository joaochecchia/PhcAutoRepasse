package repasse.phcauto.backend.usuarios.internal.core;
import java.time.LocalDate;
import java.util.Locale;
import repasse.phcauto.backend.domain.model.identidade.*;
public record CriarUsuarioCommand(TipoPessoa tipoPessoa, String nome, String email, String telefone,
        String senha, String cpf, LocalDate dataNascimento, String cnpj, String razaoSocial,
        EnderecoCadastro endereco, PapelUsuario papel) {
    public CriarUsuarioCommand(TipoPessoa tipoPessoa, String nome, String email, String telefone,
            String senha, String cpf, LocalDate nascimento, String cnpj, String razao, EnderecoCadastro endereco) {
        this(tipoPessoa,nome,email,telefone,senha,cpf,nascimento,cnpj,razao,endereco,PapelUsuario.CLIENTE);
    }
    public CriarUsuarioCommand {
        papel = papel == null ? PapelUsuario.CLIENTE : papel;
        nome = ValidacaoCadastro.texto(nome,"Nome",160);
        email = ValidacaoCadastro.texto(email,"Email",254).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new CadastroInvalidoException("Email inválido");
        if (senha == null || senha.isBlank() || senha.length()<8 || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new CadastroInvalidoException("Senha deve ter pelo menos 8 caracteres e no máximo 72 bytes UTF-8");
        if (papel == PapelUsuario.DONO) {
            tipoPessoa = TipoPessoa.PF;
            if (telefone != null || cpf != null || dataNascimento != null || cnpj != null || razaoSocial != null || endereco != null)
                throw new CadastroInvalidoException("DONO deve possuir somente nome, email e senha");
        } else {
            if (papel != PapelUsuario.CLIENTE && papel != PapelUsuario.ADMIN) throw new CadastroInvalidoException("Papel não permitido");
            if (tipoPessoa == null) throw new CadastroInvalidoException("Tipo de pessoa obrigatório");
            telefone = ValidacaoCadastro.texto(telefone,"Telefone",32);
            if (!telefone.matches("\\+?[1-9][0-9]{9,14}")) throw new CadastroInvalidoException("Telefone inválido");
            if (papel == PapelUsuario.CLIENTE && endereco == null) throw new CadastroInvalidoException("Endereço obrigatório");
            if (tipoPessoa == TipoPessoa.PF) {
                if (!ValidacaoCadastro.documentoValido(cpf,true)) throw new CadastroInvalidoException("CPF inválido");
                if (dataNascimento == null) throw new CadastroInvalidoException("Nascimento obrigatório");
                if (cnpj != null || razaoSocial != null) throw new CadastroInvalidoException("PF não aceita dados de PJ");
            } else {
                if (!ValidacaoCadastro.documentoValido(cnpj,false)) throw new CadastroInvalidoException("CNPJ inválido");
                razaoSocial = ValidacaoCadastro.texto(razaoSocial,"Razão social",200);
                if (cpf != null || dataNascimento != null) throw new CadastroInvalidoException("PJ não aceita dados de PF");
            }
        }
    }
    public String toString(){return "CriarUsuarioCommand[conteudo protegido]";}
}
