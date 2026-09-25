package repasse.phcauto.backend.usuarios.internal.core;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record UsuarioCompleto(UUID id, TipoPessoa tipoPessoa, String nome, String email,
        String telefone, boolean ativo, String senhaHash, String cpf, LocalDate dataNascimento,
        String cnpj, String razaoSocial, EnderecoUsuarioDados endereco,
        Instant criadoEm, Instant atualizadoEm) { }
