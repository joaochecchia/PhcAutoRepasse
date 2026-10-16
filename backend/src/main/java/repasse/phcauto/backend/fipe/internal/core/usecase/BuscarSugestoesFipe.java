package repasse.phcauto.backend.fipe.internal.core.usecase;

import java.text.Normalizer;
import java.util.*;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.gateway.OpcoesFipeGateway;

/** Descobre apenas referências reais e conservadoras; não estima preço ou inventa versão. */
public final class BuscarSugestoesFipe implements BuscarSugestoesFipeUseCase {
    private final OpcoesFipeGateway opcoes;
    public BuscarSugestoesFipe(OpcoesFipeGateway opcoes) { this.opcoes = opcoes; }

    @Override public List<SugestaoFipe> execute(repasse.phcauto.backend.fipe.internal.core.domain.BuscarSugestoesFipe c) {
        var meses = opcoes.opcoes(new ConsultaOpcoesFipe(null, null, null, null, "", 0, 50));
        var mes = meses.stream().filter(o -> o.mesReferencia() != null).findFirst()
                .orElseThrow(() -> new FipeException(FipeException.Motivo.NAO_ENCONTRADA, "Mês FIPE vigente não encontrado"));
        int referencia;
        try { referencia = Integer.parseInt(mes.codigo()); }
        catch (NumberFormatException e) { throw new FipeException(FipeException.Motivo.INDISPONIVEL, "Referência FIPE inválida"); }

        var marcas = opcoes.opcoes(new ConsultaOpcoesFipe(c.tipoFornecedor(), null, null, referencia, "", 0, 50));
        var marca = marcas.stream().filter(o -> marca(o.nome()).equals(marca(c.fabricante()))).findFirst()
                .orElseThrow(() -> new FipeException(FipeException.Motivo.NAO_ENCONTRADA, "Marca não encontrada na FIPE"));
        var modelos = opcoes.opcoes(new ConsultaOpcoesFipe(c.tipoFornecedor(), marca.codigo(), null, referencia, "", 0, 50));
        var ordenados = modelos.stream().map(o -> Map.entry(o, pontuar(c, o.nome())))
                .filter(e -> e.getValue() >= 0)
                .sorted(Comparator.<Map.Entry<OpcaoFipe,Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(e -> e.getKey().nome()))
                .limit(8).toList();
        var resultado = new ArrayList<SugestaoFipe>();
        for (var candidato : ordenados) {
            var anos = opcoes.opcoes(new ConsultaOpcoesFipe(c.tipoFornecedor(), marca.codigo(),
                    candidato.getKey().codigo(), referencia, "", 0, 50));
            anos.stream().filter(ano -> anoCompativel(c, candidato.getKey().nome(), ano))
                    .forEach(ano -> resultado.add(new SugestaoFipe(c.tipoFornecedor(), marca.codigo(),
                            candidato.getKey().codigo(), ano.codigo(), referencia, mes.mesReferencia(),
                            marca.nome(), candidato.getKey().nome(), ano.nome())));
        }
        return resultado.stream().distinct().limit(12).toList();
    }

    private static int pontuar(repasse.phcauto.backend.fipe.internal.core.domain.BuscarSugestoesFipe c, String descricao) {
        String alvo = normalizar(descricao), versao = normalizar(c.versao()), modelo = normalizar(c.modelo());
        if (alvo.equals(versao)) return 100;
        if (!contemTokens(alvo, modelo)) return -1;
        var tokens = tokens(versao);
        long encontrados = tokens.stream().filter(alvo::contains).count();
        if (tokens.isEmpty() || encontrados != tokens.size()) return -1;
        return 50 + tokens.size();
    }
    private static boolean anoCompativel(repasse.phcauto.backend.fipe.internal.core.domain.BuscarSugestoesFipe c,
            String descricao, OpcaoFipe ano) {
        String codigo = ano.codigo();
        boolean anoOk = c.condicao().equals("ZERO_KM") ? codigo.startsWith("32000-")
                : codigo.startsWith(c.anoModelo() + "-");
        if (!anoOk) return false;
        String informado = normalizar(c.combustivel()), esperado = normalizar(ano.nome());
        if (esperado.contains(informado)) return true;
        return informado.equals("FLEX") && (esperado.contains("GASOLINA")
                && (normalizar(descricao).contains("FLEX") || normalizar(descricao).contains("FLEXONE")));
    }
    private static boolean contemTokens(String texto, String procurado) {
        var partes = tokens(procurado); return !partes.isEmpty() && partes.stream().allMatch(texto::contains);
    }
    private static List<String> tokens(String valor) {
        return Arrays.stream(normalizar(valor).split(" ")).filter(s -> s.length() > 1)
                .filter(s -> !Set.of("DE", "DO", "DA", "DOS", "DAS").contains(s)).distinct().toList();
    }
    private static String marca(String valor) {
        return switch (normalizar(valor)) {
            case "VW", "VW VOLKSWAGEN", "VOLKSWAGEN", "VW CAMINHOES", "VOLKSWAGEN CAMINHOES E ONIBUS" -> "VOLKSWAGEN";
            case "GM CHEVROLET", "CHEVROLET", "GM" -> "CHEVROLET";
            case "MERCEDES", "MB", "MERCEDES BENZ" -> "MERCEDES BENZ";
            default -> normalizar(valor);
        };
    }
    private static String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").trim();
    }
}
