package repasse.phcauto.backend.usuarios.internal.core.domain;

import java.time.LocalDate;

public record AtualizarUsuarioCommand(
        String nome,
        String email,
        String telefone,
        String senha,
        String cpf,
        LocalDate dataNascimento,
        String cnpj,
        String razaoSocial,
        EnderecoUsuarioPatch endereco) {

    public record EnderecoUsuarioPatch(
            String cep,
            String cidade,
            String bairro,
            String rua,
            String numero,
            String complemento,
            String uf,
            Integer municipioCodigoIbge) {
        public EnderecoUsuarioPatch(String cep, String cidade, String bairro, String rua,
                String numero, String complemento, String uf) {
            this(cep, cidade, bairro, rua, numero, complemento, uf, null);
        }
    }

    @Override public String toString() { return "AtualizarUsuarioCommand[conteudo protegido]"; }
}
