import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { LocateFixed, MapPin, Search, Store } from "lucide-react";
import { apiErrorMessage, authApi, currentDeviceLocation, publicProfile, searchAds, searchAdToVehicle, searchStores } from "../lib/backend";
import type { PublicProfile } from "../lib/contracts";
import type { Vehicle } from "../data/catalog";
import { ProfileAvatar } from "../components/ProfilePhoto";
import { VehicleCard } from "../components/VehicleCard";
import { PageBreadcrumb } from "./PageLayout";

type Localizacao = { cidade: string; uf: string } | { latitude: number; longitude: number };

export function StoresPage({ id, usuarioId, authChecked, favorites, onSave, onOpen }: {
  id?: string;
  usuarioId: string | null;
  authChecked: boolean;
  favorites: string[];
  onSave: (id: string) => void;
  onOpen: (vehicle: Vehicle) => void;
}) {
  const [localizacao, setLocalizacao] = useState<Localizacao | null>(null);
  const [origem, setOrigem] = useState<"cadastro" | "dispositivo" | "manual" | null>(null);
  const [nome, setNome] = useState("");
  const [cidade, setCidade] = useState("");
  const [uf, setUf] = useState("");
  const [nomeAplicado, setNomeAplicado] = useState("");
  const [localizando, setLocalizando] = useState(false);
  const [erroLocalizacao, setErroLocalizacao] = useState("");
  const [recarregarEndereco, setRecarregarEndereco] = useState(0);
  const [perfil, setPerfil] = useState<PublicProfile | null>(null);
  const [lojistas, setLojistas] = useState<PublicProfile[]>([]);
  const [carros, setCarros] = useState<Vehicle[]>([]);
  const [total, setTotal] = useState(0);
  const [raioKmAplicado, setRaioKmAplicado] = useState<number | null>(null);
  const [proximaPagina, setProximaPagina] = useState<number | null>(null);
  const [paginaLoja, setPaginaLoja] = useState(0);
  const [temProximaPaginaLoja, setTemProximaPaginaLoja] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const [carregandoMais, setCarregandoMais] = useState(false);
  const [erro, setErro] = useState("");
  const [repetir, setRepetir] = useState(0);

  useEffect(() => {
    if (id || !authChecked || !usuarioId) return;
    let ativo = true;
    setLocalizando(true);
    setErroLocalizacao("");
    authApi.profile(usuarioId).then((dados) => {
      if (!ativo) return;
      const { cidade, uf } = dados.endereco ?? {};
      if (!cidade || !uf) {
        setErroLocalizacao("Seu cadastro ainda não possui cidade e estado. Use a localização atual do dispositivo.");
        return;
      }
      setCidade(cidade);
      setUf(uf);
      setOrigem("cadastro");
      setLocalizacao({ cidade, uf });
    }).catch((falha) => {
      if (ativo) setErroLocalizacao(apiErrorMessage(falha, "Não foi possível consultar seu endereço cadastrado."));
    }).finally(() => { if (ativo) setLocalizando(false); });
    return () => { ativo = false; };
  }, [id, usuarioId, authChecked, recarregarEndereco]);

  useEffect(() => {
    if (!id) return;
    let ativo = true;
    setCarregando(true);
    setErro("");
    Promise.all([publicProfile(id), searchAds({ anuncianteId: id }, paginaLoja, 52)])
      .then(([dados, resultado]) => {
        if (!ativo) return;
        if (dados.tipoPessoa !== "PJ") throw new Error("Este perfil não é uma loja.");
        setPerfil(dados);
        setCarros(resultado.carros.map(searchAdToVehicle));
        setTotal(resultado.total);
        setTemProximaPaginaLoja(resultado.temProximaPagina);
      }).catch((falha) => { if (ativo) setErro(apiErrorMessage(falha, "Não foi possível carregar os veículos da loja.")); })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, [id, paginaLoja, repetir]);

  useEffect(() => {
    if (id || !authChecked || usuarioId && !localizacao) return;
    let ativo = true;
    setCarregando(true);
    setErro("");
    setLojistas([]);
    setProximaPagina(null);
    const criterio = { ...(localizacao ?? {}), ...(nomeAplicado ? { nome: nomeAplicado } : {}) };
    searchStores(criterio, 0).then((resultado) => {
      if (!ativo) return;
      setLojistas(resultado.lojistas);
      setTotal(resultado.total);
      setRaioKmAplicado(resultado.raioKmAplicado);
      setProximaPagina(resultado.temProximaPagina ? 1 : null);
    }).catch((falha) => { if (ativo) setErro(apiErrorMessage(falha, "Não foi possível carregar as lojas próximas.")); })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, [id, authChecked, usuarioId, localizacao, nomeAplicado, repetir]);

  function buscarPorFiltros(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const cidadeInformada = cidade.trim();
    const ufInformada = uf.trim();
    if (Boolean(cidadeInformada) !== Boolean(ufInformada)) {
      setErroLocalizacao("Informe cidade e estado juntos para pesquisar nessa região.");
      return;
    }
    if (!cidadeInformada && !localizacao && usuarioId) {
      setErroLocalizacao("Informe cidade e estado ou use sua localização atual.");
      return;
    }
    setErroLocalizacao("");
    setNomeAplicado(nome.trim());
    if (cidadeInformada) {
      setOrigem("manual");
      setLocalizacao({ cidade: cidadeInformada, uf: ufInformada });
    } else {
      setRepetir((valor) => valor + 1);
    }
  }

  async function usarLocalizacaoAtual() {
    setLocalizando(true);
    setErroLocalizacao("");
    try {
      const posicao = await currentDeviceLocation();
      setOrigem("dispositivo");
      setCidade("");
      setUf("");
      setNomeAplicado(nome.trim());
      setLocalizacao({ latitude: posicao.coords.latitude, longitude: posicao.coords.longitude });
    } catch (falha) {
      setErroLocalizacao(typeof falha === "object" && falha !== null && "code" in falha && falha.code === 1
        ? "A localização não foi autorizada. Permita o acesso no navegador para buscar perto de você."
        : "Não foi possível obter sua localização. Tente novamente.");
    } finally { setLocalizando(false); }
  }

  async function carregarMais() {
    if (proximaPagina === null || carregandoMais) return;
    setCarregandoMais(true);
    setErro("");
    try {
      const criterio = { ...(localizacao ?? {}), ...(nomeAplicado ? { nome: nomeAplicado } : {}) };
      const primeira = await searchStores(criterio, proximaPagina);
      let novos = primeira.lojistas;
      let paginaSeguinte: number | null = primeira.temProximaPagina ? proximaPagina + 1 : null;
      if (paginaSeguinte !== null) {
        const segunda = await searchStores(criterio, paginaSeguinte);
        novos = [...novos, ...segunda.lojistas];
        paginaSeguinte = segunda.temProximaPagina ? paginaSeguinte + 1 : null;
      }
      setLojistas((atuais) => [...atuais, ...novos]);
      setTotal(primeira.total);
      setRaioKmAplicado(primeira.raioKmAplicado);
      setProximaPagina(paginaSeguinte);
    } catch (falha) {
      setErro(apiErrorMessage(falha, "Não foi possível carregar mais lojas. Tente novamente."));
    } finally { setCarregandoMais(false); }
  }

  const localizacaoRegistrada = localizacao && "cidade" in localizacao;
  return <section className="container dedicated-page stores-page">
    <PageBreadcrumb label={id ? "Veículos da loja" : "Encontrar lojistas"}
      parent={id ? { href: "#lojistas", label: "Lojistas" } : undefined} />
    {id ? <div className="store-profile-heading">
      <ProfileAvatar src={perfil?.fotoUrl} name={perfil?.nome || "loja"} />
      <div><span className="eyebrow">LOJISTA</span><h1>{perfil?.nome || "Veículos da loja"}</h1>
        {perfil && <p><MapPin size={16} /> {[perfil.cidade, perfil.uf].filter(Boolean).join(", ")}</p>}
      </div>
    </div> : <>
      <span className="eyebrow">LOJAS PRÓXIMAS</span><h1>Encontre seu lojista</h1>
      <p>{localizacaoRegistrada
        ? `Lojas até ${raioKmAplicado ?? 100} km de ${localizacao.cidade}, ${localizacao.uf}${origem === "cadastro" ? ", conforme seu cadastro" : ""}.`
        : localizacao ? `Lojas até ${raioKmAplicado ?? 100} km da sua localização atual.`
          : usuarioId ? "Buscando lojas próximas ao endereço do seu cadastro."
            : "Lojas relevantes de todo o Brasil. Você também pode pesquisar por cidade, estado ou localização atual."}</p>
      <form className="store-filters page-panel" onSubmit={buscarPorFiltros}>
        <label>Nome do lojista<input value={nome} maxLength={160} onChange={(evento) => setNome(evento.target.value)} placeholder="Nome da loja" /></label>
        <label>Cidade<input value={cidade} maxLength={120} onChange={(evento) => setCidade(evento.target.value)} placeholder="Ex.: Brasília" /></label>
        <label>Estado<select value={uf} onChange={(evento) => setUf(evento.target.value)}>
          <option value="">Selecione</option>
          {"AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO".split(" ").map((sigla) => <option key={sigla}>{sigla}</option>)}
        </select></label>
        <button type="submit" className="button primary" disabled={localizando || carregando || carregandoMais}><Search size={17} />Buscar lojas</button>
      </form>
      <div className="store-location-actions">
        <button type="button" className="button secondary" disabled={localizando || carregando || carregandoMais}
          onClick={usarLocalizacaoAtual}><LocateFixed size={17} />{localizando ? "Obtendo localização…" : "Usar minha localização atual"}</button>
        {usuarioId && origem !== "cadastro" && <button type="button" className="button secondary"
          disabled={localizando || carregando || carregandoMais}
          onClick={() => { setLocalizacao(null); setOrigem(null); setRecarregarEndereco((valor) => valor + 1); }}>
          Usar endereço cadastrado
        </button>}
      </div>
      {erroLocalizacao && <div className="inline-notice error-notice" role="alert">{erroLocalizacao}
        {usuarioId && !localizacao && <button type="button" className="text-button"
          onClick={() => setRecarregarEndereco((valor) => valor + 1)}>Tentar endereço novamente</button>}
      </div>}
    </>}
    {carregando || localizando && !localizacao ? <p role="status">Carregando lojas…</p>
      : erro && (id || lojistas.length === 0) ? <div className="empty-state"><p role="alert">{erro}</p>
        <button className="button secondary" onClick={() => setRepetir((valor) => valor + 1)}>Tentar novamente</button></div>
      : id ? <>
        <p className="store-results-count">{total} {total === 1 ? "veículo publicado" : "veículos publicados"}</p>
        {total === 0 && <div className="empty-state"><Store size={32} /><h2>Esta loja ainda não possui anúncios publicados.</h2></div>}
        <div className="vehicle-grid">{carros.map((veiculo) => <VehicleCard key={veiculo.id} vehicle={veiculo}
          saved={favorites.includes(veiculo.id)} onSave={() => onSave(veiculo.id)} onOpen={() => onOpen(veiculo)} />)}</div>
        {(paginaLoja > 0 || temProximaPaginaLoja) && <nav className="store-pagination" aria-label="Paginação dos veículos">
          <button className="button secondary" disabled={paginaLoja === 0} onClick={() => setPaginaLoja(paginaLoja - 1)}>Anterior</button>
          <span>Página {paginaLoja + 1}</span>
          <button className="button secondary" disabled={!temProximaPaginaLoja} onClick={() => setPaginaLoja(paginaLoja + 1)}>Próxima</button>
        </nav>}
      </> : localizacao || !usuarioId && authChecked ? <>
        <p className="store-results-count">{total} {total === 1 ? "loja encontrada" : "lojas encontradas"}{localizacao ? " em sua região" : " em destaque"}</p>
        {total === 0 && <div className="empty-state"><Store size={32} /><h2>Nenhuma loja encontrada.</h2><p>Tente outra cidade, estado ou nome.</p></div>}
        <div className="stores-grid">{lojistas.map((loja) => <a className="store-card page-panel" href={`#loja/${loja.id}`}
          aria-label={`Ver veículos de ${loja.nome}`} key={loja.id}>
          <ProfileAvatar src={loja.fotoUrl} name={loja.nome} /><div><h2>{loja.nome}</h2>
            <p><MapPin size={14} /> {[loja.cidade, loja.uf].filter(Boolean).join(", ") || "Cidade não informada"}</p>
            <span>Ver veículos da loja →</span></div>
        </a>)}</div>
        {erro && <p role="alert" className="inline-notice error-notice">{erro}</p>}
        {proximaPagina !== null && <div className="store-pagination"><button type="button" className="button secondary"
          disabled={carregandoMais} onClick={carregarMais}>{carregandoMais ? "Carregando…" : "Carregar mais lojas"}</button></div>}
      </> : null}
  </section>;
}
