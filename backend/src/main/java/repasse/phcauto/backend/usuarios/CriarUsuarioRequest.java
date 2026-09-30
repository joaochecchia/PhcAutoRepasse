package repasse.phcauto.backend.usuarios;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Instant;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;

@CadastroPorPapelValido
public record CriarUsuarioRequest(
        TipoPessoa tipoPessoa,
        PapelUsuario papel,
        @NotBlank @Size(max = 160) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @Pattern(regexp = "\\+?[1-9][0-9]{9,14}") String telefone,
        @NotBlank @Size(min = 8, max = 72)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha,
        String cpf, LocalDate dataNascimento, String cnpj, String razaoSocial,
        @Valid EnderecoRequest endereco,
        Boolean aceitouTermos,
        Instant aceiteTermosEm,
        @Size(max = 64) String versaoTermosUso,
        @Size(max = 64) String versaoPoliticaPrivacidade) {
    public CriarUsuarioRequest(TipoPessoa tipoPessoa, String nome, String email, String telefone,
            String senha, String cpf, LocalDate dataNascimento, String cnpj, String razaoSocial,
            EnderecoRequest endereco, Boolean aceitouTermos, Instant aceiteTermosEm,
            String versaoTermosUso, String versaoPoliticaPrivacidade) {
        this(tipoPessoa, null, nome, email, telefone, senha, cpf, dataNascimento, cnpj,
                razaoSocial, endereco, aceitouTermos, aceiteTermosEm,
                versaoTermosUso, versaoPoliticaPrivacidade);
    }
    @Override public String toString() { return "CriarUsuarioRequest[conteudo protegido]"; }
}
