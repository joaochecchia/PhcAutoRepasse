package repasse.phcauto.backend.infra.controller.mapper.identidade;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.domain.model.identidade.DadosCompraPf;
import repasse.phcauto.backend.domain.model.identidade.DadosCompraPj;
import repasse.phcauto.backend.domain.model.identidade.EnderecoUsuario;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.domain.model.identidade.Usuario;
import repasse.phcauto.backend.domain.model.identidade.UsuarioPf;
import repasse.phcauto.backend.domain.model.identidade.UsuarioPj;
import repasse.phcauto.backend.infra.controller.dto.identidade.CadastroPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.CadastroPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.CompletarCadastroSocialPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.CompletarCadastroSocialPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCadastroPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCadastroPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCompraPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCompraPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.EnderecoCadastroRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.EnderecoResponse;
import repasse.phcauto.backend.infra.controller.dto.identidade.PessoaFisicaResponse;
import repasse.phcauto.backend.infra.controller.dto.identidade.PessoaJuridicaResponse;

/** Converte contratos HTTP em modelos de domínio e modelos de domínio em respostas HTTP. */
@Component
public class CadastroDtoMapper {

    public Usuario paraUsuario(CadastroPessoaFisicaRequest request, UUID usuarioId,
            String senhaHash, Instant agora) {
        Objects.requireNonNull(request, "request");
        return paraUsuario(request.getDados(), usuarioId, senhaHash, agora);
    }

    public Usuario paraUsuario(CadastroPessoaJuridicaRequest request, UUID usuarioId,
            String senhaHash, Instant agora) {
        Objects.requireNonNull(request, "request");
        return paraUsuario(request.getDados(), usuarioId, senhaHash, agora);
    }

    public Usuario paraUsuario(CompletarCadastroSocialPessoaFisicaRequest request,
            UUID usuarioId, Instant agora) {
        Objects.requireNonNull(request, "request");
        return paraUsuario(request.getDados(), usuarioId, null, agora);
    }

    public Usuario paraUsuario(CompletarCadastroSocialPessoaJuridicaRequest request,
            UUID usuarioId, Instant agora) {
        Objects.requireNonNull(request, "request");
        return paraUsuario(request.getDados(), usuarioId, null, agora);
    }

    public Usuario paraUsuario(DadosCadastroPessoaFisicaRequest request, UUID usuarioId,
            String senhaHash, Instant agora) {
        Objects.requireNonNull(request, "request");
        return new UsuarioModel(obrigatorio(usuarioId, "usuarioId"), request.getNome(), request.getEmail(),
                senhaHash, request.getNumeroCelular(), TipoPessoa.PF, PapelUsuario.CLIENTE, true,
                obrigatorio(agora, "agora"), agora);
    }

    public Usuario paraUsuario(DadosCadastroPessoaJuridicaRequest request, UUID usuarioId,
            String senhaHash, Instant agora) {
        Objects.requireNonNull(request, "request");
        return new UsuarioModel(obrigatorio(usuarioId, "usuarioId"), request.getNomeEmpresa(),
                request.getEmailContato(), senhaHash, request.getNumeroContato(), TipoPessoa.PJ,
                PapelUsuario.CLIENTE, true, obrigatorio(agora, "agora"), agora);
    }

    public UsuarioPf paraPerfilPf(DadosCadastroPessoaFisicaRequest request, UUID usuarioId) {
        Objects.requireNonNull(request, "request");
        return new UsuarioPfModel(obrigatorio(usuarioId, "usuarioId"), request.getCpf(),
                request.getDataNascimento());
    }

    public UsuarioPj paraPerfilPj(DadosCadastroPessoaJuridicaRequest request, UUID usuarioId) {
        Objects.requireNonNull(request, "request");
        return new UsuarioPjModel(obrigatorio(usuarioId, "usuarioId"), request.getCnpj(),
                request.getRazaoSocial(), request.getNomeEmpresa());
    }

    public EnderecoUsuario paraEndereco(EnderecoCadastroRequest request, UUID usuarioId) {
        Objects.requireNonNull(request, "request");
        return new EnderecoUsuarioModel(obrigatorio(usuarioId, "usuarioId"), request.getCep(),
                request.getCidade(), request.getBairro(), request.getRua(), request.getNumero(),
                request.getComplemento(), request.getUf());
    }

    public DadosCompraPf paraDadosCompraPf(DadosCompraPessoaFisicaRequest request, UUID usuarioId) {
        Objects.requireNonNull(request, "request");
        return new DadosCompraPfModel(obrigatorio(usuarioId, "usuarioId"), request.getRg(),
                request.getNomePai(), request.getNomeMae(), request.getNaturalidade(), request.getGenero());
    }

    public DadosCompraPj paraDadosCompraPj(DadosCompraPessoaJuridicaRequest request, UUID usuarioId) {
        Objects.requireNonNull(request, "request");
        return new DadosCompraPjModel(obrigatorio(usuarioId, "usuarioId"), request.getInscricaoEstadual(),
                request.getRegimeTributario());
    }

    public PessoaFisicaResponse paraResponse(Usuario usuario, UsuarioPf perfil, EnderecoUsuario endereco) {
        Objects.requireNonNull(usuario, "usuario");
        Objects.requireNonNull(perfil, "perfil");
        validarMesmoUsuario(usuario.getId(), perfil.getUsuarioId());
        validarTipo(usuario, TipoPessoa.PF);
        if (endereco != null) validarMesmoUsuario(usuario.getId(), endereco.getUsuarioId());
        return new PessoaFisicaResponse(usuario.getId(), perfil.getCpf(), usuario.getNome(),
                perfil.getDataNascimento(), usuario.getEmail(), usuario.getTelefone(), paraResponse(endereco));
    }

    public PessoaJuridicaResponse paraResponse(Usuario usuario, UsuarioPj perfil, EnderecoUsuario endereco) {
        Objects.requireNonNull(usuario, "usuario");
        Objects.requireNonNull(perfil, "perfil");
        validarMesmoUsuario(usuario.getId(), perfil.getUsuarioId());
        validarTipo(usuario, TipoPessoa.PJ);
        if (endereco != null) validarMesmoUsuario(usuario.getId(), endereco.getUsuarioId());
        return new PessoaJuridicaResponse(usuario.getId(), perfil.getNomeFantasia(), perfil.getCnpj(),
                perfil.getRazaoSocial(), usuario.getTelefone(), usuario.getEmail(), paraResponse(endereco));
    }

    public EnderecoResponse paraResponse(EnderecoUsuario endereco) {
        if (endereco == null) return null;
        return new EnderecoResponse(endereco.getCep(), endereco.getCidade(), endereco.getBairro(),
                endereco.getRua(), endereco.getNumero(), endereco.getComplemento(), endereco.getUf());
    }

    private static void validarMesmoUsuario(UUID esperado, UUID recebido) {
        if (!Objects.equals(esperado, recebido)) {
            throw new IllegalArgumentException("Os dados pertencem a usuários diferentes");
        }
    }

    private static void validarTipo(Usuario usuario, TipoPessoa esperado) {
        if (usuario.getTipoPessoa() != esperado) {
            throw new IllegalArgumentException("Tipo de pessoa incompatível com a resposta");
        }
    }

    private static <T> T obrigatorio(T valor, String nome) {
        return Objects.requireNonNull(valor, nome);
    }

    @Getter
    @AllArgsConstructor
    private static final class UsuarioModel extends Usuario {
        private final UUID id;
        private final String nome;
        private final String email;
        private final String senhaHash;
        private final String telefone;
        private final TipoPessoa tipoPessoa;
        private final PapelUsuario papel;
        private final boolean ativo;
        private final Instant criadoEm;
        private final Instant atualizadoEm;

        @Override public boolean getAtivo() { return ativo; }
    }

    @Getter
    @AllArgsConstructor
    private static final class UsuarioPfModel extends UsuarioPf {
        private final UUID usuarioId;
        private final String cpf;
        private final LocalDate dataNascimento;
    }

    @Getter
    @AllArgsConstructor
    private static final class UsuarioPjModel extends UsuarioPj {
        private final UUID usuarioId;
        private final String cnpj;
        private final String razaoSocial;
        private final String nomeFantasia;
    }

    @Getter
    @AllArgsConstructor
    private static final class EnderecoUsuarioModel extends EnderecoUsuario {
        private final UUID usuarioId;
        private final String cep;
        private final String cidade;
        private final String bairro;
        private final String rua;
        private final String numero;
        private final String complemento;
        private final String uf;
    }

    @Getter
    @AllArgsConstructor
    private static final class DadosCompraPfModel extends DadosCompraPf {
        private final UUID usuarioId;
        private final String rg;
        private final String nomePai;
        private final String nomeMae;
        private final String naturalidade;
        private final String genero;
    }

    @Getter
    @AllArgsConstructor
    private static final class DadosCompraPjModel extends DadosCompraPj {
        private final UUID usuarioId;
        private final String inscricaoEstadual;
        private final String regimeTributario;
    }
}
