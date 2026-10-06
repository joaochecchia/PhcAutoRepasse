import { useEffect, useState } from "react";
import {
  ArrowDown,
  ArrowRight,
  ArrowUpRight,
  CarFront,
  ChevronLeft,
  ChevronRight,
  Heart,
  MapPin,
  Menu,
  Moon,
  Plus,
  Search,
  ShieldCheck,
  SlidersHorizontal,
  Sun,
  UserRound,
  X,
  Zap,
} from "lucide-react";
import { brands, types, vehicles, type Vehicle } from "./data/catalog";
import type { Filters } from "./data/filters";
import { FilterChips, FilterPanel } from "./components/Filters";
import { VehicleCard } from "./components/VehicleCard";
import { Modal } from "./components/Modal";
import { PlansView } from "./components/PlansView";
import { readRoute, pageHash, pageTitles, type Page } from "./lib/routes";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ProfilePage } from "./pages/ProfilePage";
import { AnnouncePage } from "./pages/AnnouncePage";
import { VehiclePage, NotFoundPage } from "./pages/VehiclePage";
import type { Draft } from "./components/FormFields";

type Destination = "login" | "register" | "announce" | "plans";
const initialFilters = (): Filters =>
  Object.fromEntries(
    new URLSearchParams(window.location.hash.split("?")[1] || ""),
  );

export default function App() {
  const [route, setRoute] = useState(readRoute);
  const { page, id } = route;
  const [profileDraft, setProfileDraft] = useState<Draft>({ tipoPessoa: "PF" });
  const vehicle = vehicles.find((v) => String(v.id) === id);
  const [dark, setDark] = useState(
    () => matchMedia("(prefers-color-scheme: dark)").matches,
  );
  const [menu, setMenu] = useState(false);
  const [dialog, setDialog] = useState<"plans" | null>(null);
  const [favorites, setFavorites] = useState<number[]>([]);
  const [filters, setFilters] = useState<Filters>(initialFilters);
  const [applied, setApplied] = useState<Filters>(initialFilters);
  const [mobileFilters, setMobileFilters] = useState(false);
  const [brand, setBrand] = useState("");
  const [type, setType] = useState("CARRO");
  const [notice, setNotice] = useState("");
  const [loading, setLoading] = useState(false);
  const [previewState, setPreviewState] = useState("ready");

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? "dark" : "light";
  }, [dark]);
  useEffect(() => {
    const onHash = () => {
      setRoute(readRoute());
      setMobileFilters(false);
      setDialog(null);
      const f = initialFilters();
      setFilters(f);
      setApplied(f);
      setMenu(false);
      window.scrollTo({ top: 0, behavior: "instant" });
    };
    window.addEventListener("hashchange", onHash);
    return () => window.removeEventListener("hashchange", onHash);
  }, []);
  useEffect(() => {
    document.title = `${page === "vehicle" && vehicle ? `${vehicle.brand} ${vehicle.model}` : pageTitles[page]} • PHC Auto`;
    document.getElementById("conteudo")?.focus({ preventScroll: true });
  }, [page, id, vehicle]);
  useEffect(() => {
    if (notice) {
      const t = setTimeout(() => setNotice(""), 4200);
      return () => clearTimeout(t);
    }
  }, [notice]);
  useEffect(() => {
    if (loading) {
      const t = setTimeout(() => setLoading(false), 450);
      return () => clearTimeout(t);
    }
  }, [loading]);

  function navigate(next: Page, values: Filters = {}, id?: string) {
    const query = new URLSearchParams(
      Object.entries(values).filter(([, v]) => v),
    );
    window.location.hash = `${pageHash(next, id)}${next === "search" && query.size ? `?${query}` : ""}`;
    setRoute({ page: next, id });
    setMenu(false);
    setMobileFilters(false);
    setPreviewState("ready");
    if (next === "search") {
      setFilters(values);
      setApplied(values);
      setLoading(true);
    }
    window.scrollTo({ top: 0, behavior: "instant" });
  }
  function openDestination(d: Destination) {
    if (d === "plans") setDialog(d);
    else navigate(d === "announce" ? "profile" : d);
    setMenu(false);
  }
  function save(id: number) {
    const exists = favorites.includes(id);
    setFavorites((prev) =>
      exists ? prev.filter((v) => v !== id) : [...prev, id],
    );
    setNotice(
      exists
        ? "Veículo removido dos favoritos."
        : "Salvo nos favoritos desta visita.",
    );
  }
  const shown =
    page === "favorites"
      ? vehicles.filter((v) => favorites.includes(v.id))
      : vehicles;
  const card = (v: Vehicle) => (
    <VehicleCard
      key={v.id}
      vehicle={v}
      saved={favorites.includes(v.id)}
      onSave={() => save(v.id)}
      onOpen={() => navigate("vehicle", {}, String(v.id))}
    />
  );

  return (
    <>
      <a
        href="#conteudo"
        className="skip-link"
        onClick={(e) => {
          e.preventDefault();
          document.getElementById("conteudo")?.focus();
          document.getElementById("conteudo")?.scrollIntoView();
        }}
      >
        Pular para o conteúdo
      </a>
      <div className="top-strip">
        <div className="container">
          <span>BOAS OPORTUNIDADES. NOVOS CAMINHOS.</span>
          <span>
            PHC Auto Repasse <span className="strip-dot">•</span> Conectando
            você ao próximo veículo
          </span>
        </div>
      </div>
      <header className="header">
        <div className="container header-inner">
          <a href="#inicio" className="logo" aria-label="PHC Auto — início">
            <img
              className="site-logo"
              src="/images/phc-auto-logo.jpeg"
              alt="PHC Auto"
              width={80}
              height={80}
            />
          </a>
          <nav className="desktop-nav" aria-label="Navegação principal">
            <a href="#buscar" className={page === "search" ? "active" : ""}>
              Comprar veículo
            </a>
            <button onClick={() => openDestination("announce")}>
              Vender meu veículo
            </button>
            <button onClick={() => openDestination("plans")}>
              Nossos planos<small>NOVO</small>
            </button>
          </nav>
          <div className="header-actions">
            <button
              className="icon-button theme-button"
              onClick={() => setDark(!dark)}
              aria-label={dark ? "Ativar modo claro" : "Ativar modo escuro"}
            >
              {dark ? <Sun size={20} /> : <Moon size={20} />}
            </button>
            <button
              className="icon-button favorites-nav"
              onClick={() => navigate("favorites")}
              aria-label={`Favoritos (${favorites.length})`}
            >
              <Heart size={20} />
              {favorites.length > 0 && <b>{favorites.length}</b>}
            </button>
            <span className="header-divider" />
            <button
              className="login-link"
              onClick={() => openDestination("login")}
            >
              <UserRound size={18} />
              <span>Entrar</span>
            </button>
            <button
              className="button primary header-announce"
              onClick={() => openDestination("announce")}
            >
              <Plus size={17} />
              Anunciar
            </button>
            <button
              className="icon-button mobile-menu"
              onClick={() => setMenu(!menu)}
              aria-label={menu ? "Fechar menu" : "Abrir menu"}
              aria-expanded={menu}
            >
              {menu ? <X /> : <Menu />}
            </button>
          </div>
        </div>
        {menu && (
          <nav className="mobile-nav" aria-label="Navegação móvel">
            <a href="#buscar" onClick={() => navigate("search")}>
              Comprar veículo
            </a>
            <button onClick={() => openDestination("announce")}>
              Anunciar meu veículo
            </button>
            <button onClick={() => openDestination("plans")}>
              Nossos planos
            </button>
            <button onClick={() => navigate("favorites")}>
              Favoritos ({favorites.length})
            </button>
            <button onClick={() => openDestination("login")}>Entrar</button>
            <button onClick={() => openDestination("register")}>
              Criar conta
            </button>
          </nav>
        )}
      </header>

      <main id="conteudo" tabIndex={-1}>
        {page === "login" ? (
          <LoginPage />
        ) : page === "register" ? (
          <RegisterPage />
        ) : page === "profile" ? (
          <ProfilePage values={profileDraft} setValues={setProfileDraft} />
        ) : page === "announce" ? (
          <AnnouncePage profile={profileDraft} />
        ) : page === "vehicle" ? (
          vehicle ? (
            <VehiclePage
              key={id}
              vehicle={vehicle}
              favorites={favorites}
              onSave={save}
              onOpen={(id) => navigate("vehicle", {}, String(id))}
            />
          ) : (
            <NotFoundPage />
          )
        ) : page === "not-found" ? (
          <NotFoundPage />
        ) : page === "home" ? (
          <>
            <section className="hero">
              <img
                className="hero-image"
                src="/images/hero.jpg"
                alt="Porsche em uma estrada arborizada"
                fetchPriority="high"
              />
              <div className="hero-shade" />
              <div className="container hero-content">
                <div className="hero-kicker">
                  <span />O SEU PRÓXIMO CAPÍTULO
                </div>
                <h1>
                  Bom negócio.
                  <br />
                  Melhor caminho.
                </h1>
                <p>
                  O veículo que você procura.
                  <br className="mobile-only" /> A oportunidade que você
                  esperava.
                </p>
                <div className="hero-links">
                  <button
                    className="hero-link"
                    onClick={() =>
                      document
                        .getElementById("catalogo")
                        ?.scrollIntoView({ behavior: "smooth" })
                    }
                  >
                    Explore as oportunidades
                    <ArrowDown size={17} />
                  </button>
                  <button
                    className="hero-sell"
                    onClick={() => openDestination("announce")}
                  >
                    Anunciar meu veículo
                    <ArrowUpRight size={16} />
                  </button>
                </div>
                <span className="hero-caption">
                  PAIXÃO POR VEÍCULOS. CONEXÃO COM BONS NEGÓCIOS.
                </span>
              </div>
              <div className="hero-number">
                01 <span>/ 01</span>
              </div>
            </section>
            <section
              className="container search-container"
              aria-label="Pesquisar veículos"
            >
              <div className="search-box">
                <div className="search-top">
                  <div
                    className="vehicle-tabs"
                    role="group"
                    aria-label="Tipo de veículo"
                  >
                    {types.slice(0, 3).map(([v, label]) => (
                      <button
                        key={v}
                        className={type === v ? "selected" : ""}
                        aria-pressed={type === v}
                        onClick={() => setType(v)}
                      >
                        {v === "CARRO" && <CarFront size={19} />}
                        {label}
                      </button>
                    ))}
                    <select
                      aria-label="Outros tipos de veículo"
                      value={
                        types.slice(3).some((t) => t[0] === type) ? type : ""
                      }
                      onChange={(e) => setType(e.target.value)}
                    >
                      <option value="" disabled>
                        Outros
                      </option>
                      {types.slice(3).map(([v, l]) => (
                        <option value={v} key={v}>
                          {l}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className="search-intro">
                    Qual vai ser o seu próximo?
                  </span>
                </div>
                <form
                  className="hero-search"
                  onSubmit={(e) => {
                    e.preventDefault();
                    navigate("search", {
                      tipoVeiculo: type,
                      marca: brand,
                      modoLocalizacao: "BRASIL",
                    });
                  }}
                >
                  <label className="brand-search">
                    <Search size={22} />
                    <span>
                      <small>ENCONTRE SEU VEÍCULO</small>
                      <select
                        value={brand}
                        onChange={(e) => setBrand(e.target.value)}
                        aria-label="Escolha a marca"
                      >
                        <option value="">Qual marca você procura?</option>
                        {[
                          ...brands.map((b) => b[0]),
                          "Audi",
                          "Porsche",
                          "Mercedes-Benz",
                          "Ford",
                        ].map((b) => (
                          <option key={b}>{b}</option>
                        ))}
                      </select>
                    </span>
                  </label>
                  <button
                    type="button"
                    className="location-search"
                    onClick={() => {
                      navigate("search", {
                        tipoVeiculo: type,
                        marca: brand,
                        modoLocalizacao: "CIDADE",
                      });
                      setMobileFilters(true);
                    }}
                  >
                    <MapPin size={21} />
                    <span>
                      <small>LOCALIZAÇÃO</small>Todo o Brasil
                    </span>
                    <ChevronRight size={16} />
                  </button>
                  <button
                    type="submit"
                    className="button primary search-submit"
                  >
                    Buscar veículos
                    <ArrowRight size={19} />
                  </button>
                </form>
                <div className="search-bottom">
                  <span>
                    <ShieldCheck size={15} />
                    Seu próximo negócio começa com uma boa escolha.
                  </span>
                  <button
                    className="text-button"
                    onClick={() => navigate("search", { tipoVeiculo: type })}
                  >
                    Busca avançada
                    <SlidersHorizontal size={14} />
                  </button>
                </div>
              </div>
            </section>
            <section className="container brands-section">
              <div className="section-heading">
                <div>
                  <span className="eyebrow">ENCONTRE A SUA FAVORITA</span>
                  <h2>O próximo pode ser um...</h2>
                </div>
                <button
                  className="text-button dark-link"
                  onClick={() => navigate("search")}
                >
                  Ver todas as marcas
                  <ArrowUpRight size={17} />
                </button>
              </div>
              <div className="brands-list">
                {brands.map(([name, slug]) => (
                  <button
                    className="brand-tile"
                    key={slug}
                    onClick={() =>
                      navigate("search", { marca: name, tipoVeiculo: "CARRO" })
                    }
                  >
                    <span className="brand-image">
                      <img
                        src={`/brands/${slug}.png`}
                        alt=""
                        onError={(e) => {
                          e.currentTarget.style.display = "none";
                        }}
                      />
                      <span className="brand-fallback">
                        {name.slice(0, 2).toUpperCase()}
                      </span>
                    </span>
                    <span>{name}</span>
                  </button>
                ))}
              </div>
            </section>
            <section className="catalog-section" id="catalogo">
              <div className="container">
                <div className="section-heading">
                  <div>
                    <span className="eyebrow">
                      <span className="red-dot" />A SUA PRÓXIMA CONQUISTA
                    </span>
                    <h2>Vale a pena conhecer.</h2>
                    <p>Explore veículos e encontre o que combina com você.</p>
                  </div>
                  <button
                    className="button secondary"
                    onClick={() => navigate("search")}
                  >
                    Ver todos os veículos
                    <ArrowUpRight size={17} />
                  </button>
                </div>
                <div className="catalog-tabs">
                  <span className="selected">
                    <Zap size={15} />
                    Vitrine PHC
                  </span>
                  <span className="demo-label">
                    Catálogo demonstrativo · imagens ilustrativas
                  </span>
                </div>
                <div className="vehicle-grid">
                  {vehicles.slice(0, 4).map(card)}
                </div>
                <div className="vehicle-grid second-row">
                  {vehicles.slice(4).map(card)}
                </div>
              </div>
            </section>
            <section className="container sell-section">
              <div className="sell-content">
                <span className="eyebrow">
                  O PRÓXIMO BOM NEGÓCIO PODE SER SEU
                </span>
                <h2>
                  Seu veículo merece
                  <br />
                  uma nova vitrine.
                </h2>
                <p>
                  Deixe seu anúncio pronto para quem está
                  <br className="desktop-only" /> procurando exatamente o que
                  você tem.
                </p>
                <button
                  className="button white"
                  onClick={() => openDestination("announce")}
                >
                  Quero anunciar meu veículo
                  <ArrowUpRight size={18} />
                </button>
              </div>
              <div className="sell-art">
                <span className="sell-outline">
                  VAMOS
                  <br />
                  NEGOCIAR.
                </span>
                <span className="sell-arrow">↗</span>
              </div>
            </section>
            <section className="container account-strip">
              <div>
                <UserRound size={25} />
                <div>
                  <h3>Uma conta. Novas possibilidades.</h3>
                  <p>
                    Prepare-se para comprar, anunciar e acompanhar seus
                    favoritos.
                  </p>
                </div>
              </div>
              <button
                className="button secondary"
                onClick={() => openDestination("register")}
              >
                Criar minha conta
                <ArrowRight size={17} />
              </button>
            </section>
          </>
        ) : (
          <section className="container results-page">
            <div className="breadcrumbs">
              <a href="#inicio">Início</a>
              <ChevronRight size={13} />
              <span>
                {page === "favorites" ? "Favoritos" : "Comprar veículos"}
              </span>
            </div>
            <div className="section-heading">
              <div>
                <span className="eyebrow">
                  {page === "favorites"
                    ? "PARA OLHAR MAIS UMA VEZ"
                    : "ENCONTRE O SEU PRÓXIMO CAMINHO"}
                </span>
                <h1>
                  {page === "favorites"
                    ? "Seus favoritos."
                    : "Boas escolhas começam aqui."}
                </h1>
                <p>
                  {page === "favorites"
                    ? "Sua seleção fica disponível durante esta visita."
                    : "Explore a vitrine e prepare uma busca do seu jeito."}
                </p>
              </div>
              <button
                className="button secondary"
                onClick={() => navigate("home")}
              >
                <ChevronLeft size={16} />
                Voltar ao início
              </button>
            </div>
            <div
              className={`results-layout ${page === "favorites" ? "no-sidebar" : ""}`}
            >
              {page === "search" && (
                <aside className="desktop-filters">
                  <FilterPanel
                    values={filters}
                    setValues={setFilters}
                    onApply={() => navigate("search", filters)}
                    onClear={() => navigate("search", {})}
                  />
                </aside>
              )}
              <div className="results-content">
                {page === "search" && (
                  <>
                    <div className="results-banner">
                      <div>
                        <span className="eyebrow">PHC AUTO REPASSE</span>
                        <h3>
                          Seu próximo veículo.
                          <br />
                          Sua próxima história.
                        </h3>
                      </div>
                      <CarFront size={78} strokeWidth={1} />
                      <button onClick={() => openDestination("announce")}>
                        Tem um veículo para vender?
                        <span>
                          Anuncie aqui
                          <ArrowUpRight size={16} />
                        </span>
                      </button>
                    </div>
                    <div className="preview-notice">
                      <span className="red-dot" />
                      <p>
                        Você está explorando uma{" "}
                        <strong>vitrine demonstrativa</strong>. Os filtros
                        selecionados ficam preparados na interface; os
                        resultados serão consultados após a integração.
                      </p>
                    </div>
                    <FilterChips
                      values={applied}
                      onRemove={(key) => {
                        const next = { ...applied };
                        delete next[key];
                        navigate("search", next);
                      }}
                    />
                  </>
                )}
                <div className="results-toolbar">
                  <span>
                    <strong>
                      {previewState === "empty" ? 0 : shown.length}
                    </strong>{" "}
                    veículos {page === "favorites" ? "salvos" : "na prévia"}
                  </span>
                  {page === "search" && (
                    <>
                      <button
                        className="button secondary mobile-filter-button"
                        onClick={() => setMobileFilters(true)}
                      >
                        <SlidersHorizontal size={16} />
                        Filtros
                      </button>
                      <label className="state-select">
                        Prévia
                        <select
                          aria-label="Estado da prévia"
                          value={previewState}
                          onChange={(e) => setPreviewState(e.target.value)}
                        >
                          <option value="ready">Vitrine</option>
                          <option value="loading">Carregando</option>
                          <option value="empty">Sem resultados</option>
                          <option value="error">Falha de conexão</option>
                        </select>
                      </label>
                    </>
                  )}
                </div>
                {previewState === "loading" || loading ? (
                  <div
                    className="vehicle-grid results-grid"
                    aria-label="Carregando veículos"
                    aria-busy="true"
                  >
                    {[1, 2, 3, 4, 5, 6].map((n) => (
                      <div className="skeleton-card" key={n}>
                        <div />
                        <span />
                        <span />
                        <span />
                      </div>
                    ))}
                  </div>
                ) : previewState === "error" ? (
                  <div className="empty-state" role="alert">
                    <CarFront size={40} />
                    <h2>Não foi possível carregar a vitrine.</h2>
                    <p>
                      Esta é uma simulação do estado de falha. Tente novamente
                      para voltar à prévia.
                    </p>
                    <button
                      className="button primary"
                      onClick={() => {
                        setPreviewState("ready");
                        setLoading(true);
                      }}
                    >
                      Tentar novamente
                    </button>
                  </div>
                ) : !shown.length || previewState === "empty" ? (
                  <div className="empty-state">
                    <Heart size={40} />
                    <h2>
                      {page === "favorites"
                        ? "Os seus favoritos começam aqui."
                        : "Ainda não encontramos esse caminho."}
                    </h2>
                    <p>
                      {page === "favorites"
                        ? "Toque no coração de um veículo para guardá-lo nesta visita."
                        : "Experimente ampliar a busca ou remover alguns filtros. Este estado é demonstrativo."}
                    </p>
                    <button
                      className="button primary"
                      onClick={() => navigate("search")}
                    >
                      Explorar veículos
                      <ArrowRight size={17} />
                    </button>
                  </div>
                ) : (
                  <div className="vehicle-grid results-grid">
                    {shown.map(card)}
                  </div>
                )}
                {shown.length > 0 && previewState === "ready" && !loading && (
                  <div className="catalog-end">
                    <span>Você viu todos os veículos desta prévia.</span>
                    <button
                      className="text-button"
                      onClick={() =>
                        window.scrollTo({ top: 0, behavior: "smooth" })
                      }
                    >
                      Voltar ao topo ↑
                    </button>
                  </div>
                )}
              </div>
            </div>
          </section>
        )}
      </main>
      <footer>
        <div className="container footer-main">
          <a href="#inicio" className="logo footer-logo">
            <img
              className="site-logo"
              src="/images/phc-auto-logo.jpeg"
              alt="PHC Auto"
              width={80}
              height={80}
            />
          </a>
          <p>
            Boas oportunidades.
            <br />
            Novos caminhos.
          </p>
          <nav aria-label="Links do rodapé">
            <a href="#buscar">Comprar</a>
            <button onClick={() => openDestination("announce")}>
              Anunciar
            </button>
            <button onClick={() => openDestination("plans")}>Planos</button>
            <button onClick={() => openDestination("register")}>
              Criar conta
            </button>
          </nav>
          <button className="theme-footer" onClick={() => setDark(!dark)}>
            {dark ? <Sun size={16} /> : <Moon size={16} />}
            {dark ? "Modo claro" : "Modo escuro"}
          </button>
        </div>
        <div className="container footer-bottom">
          <span>© {new Date().getFullYear()} PHC Auto Repasse.</span>
          <span>
            Prévia visual · Veículos, valores e fotografias ilustrativos.
          </span>
        </div>
      </footer>
      {notice && (
        <div className="toast" role="status">
          <Heart size={17} />
          {notice}
          <button onClick={() => setNotice("")} aria-label="Fechar mensagem">
            <X size={15} />
          </button>
        </div>
      )}
      {dialog === "plans" && <PlansView onClose={() => setDialog(null)} />}
      {mobileFilters && (
        <Modal title="Refine sua busca" onClose={() => setMobileFilters(false)}>
          <FilterPanel
            values={filters}
            setValues={setFilters}
            onApply={() => navigate("search", filters)}
            onClear={() => setFilters({})}
          />
        </Modal>
      )}
    </>
  );
}
