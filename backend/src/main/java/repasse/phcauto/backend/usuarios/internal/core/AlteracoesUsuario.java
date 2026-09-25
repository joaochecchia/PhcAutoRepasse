package repasse.phcauto.backend.usuarios.internal.core;

import java.time.LocalDate;

public record AlteracoesUsuario(String nome, String email, String telefone, String senhaHash,
        String cpf, LocalDate dataNascimento, String cnpj, String razaoSocial,
        EnderecoUsuarioDados endereco) { }
