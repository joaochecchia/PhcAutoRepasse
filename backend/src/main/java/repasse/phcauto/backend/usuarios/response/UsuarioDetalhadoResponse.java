package repasse.phcauto.backend.usuarios.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record UsuarioDetalhadoResponse(
        UUID id,
        TipoPessoa tipoPessoa,
        String nome,
        String email,
        String telefone,
        boolean ativo,
        String cpf,
        LocalDate dataNascimento,
        String cnpj,
        String razaoSocial,
        EnderecoUsuarioResponse endereco,
        Instant criadoEm,
        Instant atualizadoEm) { }
