import { useEffect, useMemo, useRef, useState } from "react";
import {
  ArrowDown,
  ArrowRight,
  ArrowUpRight,
  Bike,
  CarFront,
  ChevronLeft,
  ChevronRight,
  Heart,
  MapPin,
  Menu,
  Moon,
  Plus,
  Search,
  Ship,
  ShieldCheck,
  SlidersHorizontal,
  Sun,
  Tractor,
  Truck,
  UserRound,
  X,
  Zap,
} from "lucide-react";
import { brands, types, vehicles, type Vehicle } from "./data/catalog";
import type { Filters } from "./data/filters";
import { FilterChips, FilterPanel } from "./components/Filters";
import { VehicleCard } from "./components/VehicleCard";
import { PickupSideIcon } from "./components/PickupSideIcon";
import { Modal } from "./components/Modal";
import { PlansView } from "./components/PlansView";
import { readRoute, pageHash, pageTitles, type Page } from "./lib/routes";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ProfilePage } from "./pages/ProfilePage";
import { AnnouncePage } from "./pages/AnnouncePage";
import { VehiclePage, NotFoundPage } from "./pages/VehiclePage";
import type { Draft } from "./components/FormFields";
import {
  apiErrorMessage,
  authApi,
  homeAds,
  profileToDraft,
  prepareDeviceLocation,
  searchAds,
  searchAdToVehicle,
} from "./lib/backend";
import type { Session } from "./lib/contracts";
import { findAddressByCep } from "./lib/viacep";

type Destination = "login" | "register" | "announce" | "plans";
const initialFilters = (): Filters =>
  Object.fromEntries(
    new URLSearchParams(window.location.hash.split("?")[1] || ""),
  );

export default function App() {
  const [route, setRoute] = useState(readRoute);
  const { page, id } = route;
  const [profileDraft, setProfileDraft] = useState<Draft>({ tipoPessoa: "PF" });
  const [session, setSession] = useState<Session | null>(null);
  const [authChecked, setAuthChecked] = useState(false);
  const [announceIntent, setAnnounceIntent] = useState(false);
  const [homeVehicles, setHomeVehicles] = useState<Vehicle[]>([]);
  const [searchVehicles, setSearchVehicles] = useState<Vehicle[]>([]);
  const [catalogError, setCatalogError] = useState("");
  const [searchTotal, setSearchTotal] = useState(0);
  const [searchPage, setSearchPage] = useState(0);
  const [hasNextPage, setHasNextPage] = useState(false);
  const vehicle = [...searchVehicles, ...homeVehicles, ...vehicles].find(
    (v) => v.id === id,
  );
  const [dark, setDark] = useState(
    () => matchMedia("(prefers-color-scheme: dark)").matches,
  );
  const [menu, setMenu] = useState(false);
  const [dialog, setDialog] = useState<"plans" | null>(null);
  const [favorites, setFavorites] = useState<string[]>([]);
  const [filters, setFilters] = useState<Filters>(initialFilters);
  const [applied, setApplied] = useState<Filters>(initialFilters);
  const [mobileFilters, setMobileFilters] = useState(false);
  const [brand, setBrand] = useState("");
  const [model, setModel] = useState("");
  const [vehicleQuery, setVehicleQuery] = useState("");
  const [suggestionsOpen, setSuggestionsOpen] = useState(false);
  const [type, setType] = useState("CARRO");
  const [locationOpen, setLocationOpen] = useState(false);
  const [locationMode, setLocationMode] = useState("BRASIL");
  const [locationCity, setLocationCity] = useState("");
  const [locationUf, setLocationUf] = useState("");
  const [locationCep, setLocationCep] = useState("");
  const [locationError, setLocationError] = useState("");
  const [resolvingCep, setResolvingCep] = useState(false);
  const [requestingDeviceLocation, setRequestingDeviceLocation] = useState(false);
  const [notice, setNotice] = useState("");
  const [loading, setLoading] = useState(false);
  const homeRequestStarted = useRef(false);
  const vehicleSearchRef = useRef<HTMLDivElement>(null);
  const locationSearchRef = useRef<HTMLDivElement>(null);
  const vehicleSuggestions = useMemo(() => {
    const availableVehicles = [...homeVehicles, ...vehicles];
    const brandNames = Array.from(
      new Set([...brands.map(([name]) => name), ...availableVehicles.map((item) => item.brand)]),
    );
    const models = Array.from(
      new Map(
        availableVehicles.map((item) => [
          `${item.brand.toLocaleLowerCase("pt-BR")}|${item.model.toLocaleLowerCase("pt-BR")}`,
          { brand: item.brand, model: item.model },
        ]),
      ).values(),
    );
    const query = vehicleQuery.trim().toLocaleLowerCase("pt-BR");
    const matches = (value: string) => !query || value.toLocaleLowerCase("pt-BR").includes(query);
    return [
      ...brandNames.filter(matches).map((name) => ({ kind: "brand" as const, label: name, brand: name, model: "" })),
      ...models.filter((item) => matches(item.model)).map((item) => ({ kind: "model" as const, label: item.model, brand: "", model: item.model, context: item.brand })),
      ...models.filter((item) => matches(`${item.brand} ${item.model}`)).map((item) => ({ kind: "both" as const, label: `${item.brand} ${item.model}`, brand: item.brand, model: item.model })),
    ].slice(0, 10);
  }, [homeVehicles, vehicleQuery]);

  const locationLabel =
    locationMode === "DISPOSITIVO"
      ? "Perto de mim"
      : locationMode === "CIDADE"
        ? `${locationCity}, ${locationUf}`
        : locationMode === "UF"
          ? locationUf
          : "Todo o Brasil";

  useEffect(() => {
    function closeFloatingPanels(event: PointerEvent) {
      const target = event.target as Node;
      if (!vehicleSearchRef.current?.contains(target)) {
        setSuggestionsOpen(false);
      }
      if (!locationSearchRef.current?.contains(target)) {
        setLocationOpen(false);
      }
    }
    document.addEventListener("pointerdown", closeFloatingPanels);
    return () => document.removeEventListener("pointerdown", closeFloatingPanels);
  }, []);

  function heroFilters(): Filters {
    return {
      tipoVeiculo: type,
      marca: brand,
      modelo: model,
      termo: !brand && !model ? vehicleQuery.trim() : "",
      modoLocalizacao: locationMode,
      cidade: locationMode === "CIDADE" ? locationCity : "",
      uf: locationMode === "CIDADE" || locationMode === "UF" ? locationUf : "",
    };
  }

  function chooseVehicleSuggestion(item: (typeof vehicleSuggestions)[number]) {
    setBrand(item.brand);
    setModel(item.model);
    setVehicleQuery(item.label);
    setSuggestionsOpen(false);
  }

  async function authorizeDeviceLocation(showInLocationPanel = false) {
    setRequestingDeviceLocation(true);
    if (showInLocationPanel) setLocationError("");
    try {
      await prepareDeviceLocation();
      return true;
    } catch (error) {
      const denied = typeof error === "object" && error !== null && "code" in error && error.code === 1;
      const message = denied
        ? "A localização não foi autorizada. Permita o acesso no navegador para buscar perto de você."
        : "Não foi possível obter sua localização. Tente novamente ou escolha uma cidade.";
      if (showInLocationPanel) setLocationError(message);
      else setNotice(message);
      return false;
    } finally {
      setRequestingDeviceLocation(false);
    }
  }

  async function applySearch(values: Filters) {
    if (values.modoLocalizacao === "DISPOSITIVO" && !(await authorizeDeviceLocation())) return;
    navigate("search", values);
  }

  async function applyTypedLocation() {
    setLocationError("");
    let city = locationCity.trim();
    let uf = locationUf;
    const cep = locationCep.replace(/\D/g, "");
    if (cep) {
      if (cep.length !== 8) {
        setLocationError("Digite um CEP com 8 números.");
        return;
      }
      setResolvingCep(true);
      try {
        const address = await findAddressByCep(cep);
        city = address.localidade;
        uf = address.uf;
        setLocationCity(city);
        setLocationUf(uf);
      } catch {
        setLocationError("Não foi possível localizar esse CEP.");
        return;
      } finally {
        setResolvingCep(false);
      }
    }
    if (city && !uf) {
      setLocationError("Escolha a UF da cidade.");
      return;
    }
    if (!city && !uf) {
      setLocationError("Informe cidade, UF ou CEP.");
      return;
    }
    setLocationCity(city);
    setLocationUf(uf);
    setLocationMode(city ? "CIDADE" : "UF");
    setLocationOpen(false);
  }

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
    authApi
      .session()
      .then(async (current) => {
        setSession(current.autenticado ? current : null);
        if (current.autenticado && current.usuarioId) {
          const profile = await authApi.profile(current.usuarioId);
          setProfileDraft(profileToDraft(profile));
        }
      })
      .catch(() => setSession(null))
      .finally(() => setAuthChecked(true));
  }, []);

  useEffect(() => {
    if (homeRequestStarted.current) return;
    homeRequestStarted.current = true;
    setLoading(true);
    homeAds()
      .then((result) => {
        setHomeVehicles(result.carros.map(searchAdToVehicle));
        setCatalogError("");
      })
      .catch((error) =>
        setCatalogError(apiErrorMessage(error, "Não foi possível carregar os veículos.")),
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (page !== "search") return;
    setLoading(true);
    setCatalogError("");
    searchAds(applied, 0, 52)
      .then((result) => {
        setSearchVehicles(result.carros.map(searchAdToVehicle));
        setSearchTotal(result.total);
        setSearchPage(0);
        setHasNextPage(result.temProximaPagina);
      })
      .catch((error) => {
        setSearchVehicles([]);
        setCatalogError(apiErrorMessage(error, "Não foi possível realizar a busca."));
      })
      .finally(() => setLoading(false));
  }, [page, applied]);

  useEffect(() => {
    if (!authChecked || session?.autenticado) return;
    if (page === "profile" || page === "announce") {
      setAnnounceIntent(true);
      navigate("login");
    }
  }, [authChecked, page, session]);

  function navigate(next: Page, values: Filters = {}, id?: string) {
    const query = new URLSearchParams(
      Object.entries(values).filter(([, v]) => v),
    );
    window.location.hash = `${pageHash(next, id)}${next === "search" && query.size ? `?${query}` : ""}`;
    setRoute({ page: next, id });
    setMenu(false);
    setMobileFilters(false);
    if (next === "search") {
      setFilters(values);
      setApplied(values);
      setLoading(true);
    }
    window.scrollTo({ top: 0, behavior: "instant" });
  }
  function openDestination(d: Destination) {
    if (d === "plans") setDialog(d);
    else if (d === "announce") {
      setAnnounceIntent(true);
      navigate(session?.autenticado ? "profile" : "login");
    } else navigate(d);
    setMenu(false);
  }
  function save(id: string) {
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
  const allKnownVehicles = [...searchVehicles, ...homeVehicles].filter(
    (item, index, list) => list.findIndex((other) => other.id === item.id) === index,
  );
  const shown =
    page === "favorites"
      ? allKnownVehicles.filter((v) => favorites.includes(v.id))
      : searchVehicles;
  const card = (v: Vehicle) => (
    <VehicleCard
      key={v.id}
      vehicle={v}
      saved={favorites.includes(v.id)}
      onSave={() => save(v.id)}
      onOpen={() => navigate("vehicle", {}, String(v.id))}
    />
  );

  async function handleAuthenticated(current: Session, draft?: Draft) {
    setSession(current);
    if (draft) setProfileDraft(draft);
    if (current.usuarioId) {
      try {
        setProfileDraft(profileToDraft(await authApi.profile(current.usuarioId)));
      } catch {
        // The registration draft remains available if the read projection has
        // not received the newly created profile yet.
      }
    }
    navigate(announceIntent || draft ? "profile" : "home");
  }

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      setSession(null);
      setProfileDraft({ tipoPessoa: "PF" });
      setAnnounceIntent(false);
      navigate("home");
    }
  }

  async function loadMore() {
    const nextPage = searchPage + 1;
    setLoading(true);
    try {
      const result = await searchAds(applied, nextPage, 52);
      setSearchVehicles((current) => [
        ...current,
        ...result.carros.map((ad, index) =>
          searchAdToVehicle(ad, current.length + index),
        ),
      ]);
      setSearchPage(nextPage);
      setHasNextPage(result.temProximaPagina);
    } catch (error) {
      setCatalogError(apiErrorMessage(error, "Não foi possível carregar mais veículos."));
    } finally {
      setLoading(false);
    }
  }

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
            <button className="login-link" onClick={() => session ? logout() : openDestination("login")}>
              <UserRound size={18} />
              <span>{session ? "Sair" : "Entrar"}</span>
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
            <button onClick={() => session ? logout() : openDestination("login")}>
              {session ? "Sair" : "Entrar"}
            </button>
            {!session && (
              <button onClick={() => openDestination("register")}>
                Criar conta
              </button>
            )}
          </nav>
        )}
      </header>

      <main id="conteudo" tabIndex={-1}>
        {page === "login" ? (
          <LoginPage onAuthenticated={handleAuthenticated} />
        ) : page === "register" ? (
          <RegisterPage onAuthenticated={handleAuthenticated} />
        ) : page === "profile" ? (
          session?.usuarioId ? (
            <ProfilePage
              values={profileDraft}
              setValues={setProfileDraft}
              usuarioId={session.usuarioId}
              onContinue={() => navigate("announce")}
            />
          ) : null
        ) : page === "announce" ? (
          session?.usuarioId ? (
            <AnnouncePage
              profile={profileDraft}
              usuarioId={session.usuarioId}
              onCreated={() => {
                setNotice("Anúncio criado com sucesso.");
                setAnnounceIntent(false);
                navigate("search");
              }}
            />
          ) : null
        ) : page === "vehicle" ? (
          vehicle ? (
            <VehiclePage
              key={id}
              vehicle={vehicle}
              favorites={favorites}
              onSave={save}
              onOpen={(id) => navigate("vehicle", {}, String(id))}
              related={allKnownVehicles}
              authenticated={Boolean(session?.autenticado)}
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
                    {types.map(([v, label]) => (
                      <button
                        key={v}
                        type="button"
                        className={type === v ? "selected" : ""}
                        aria-pressed={type === v}
                        onClick={() => setType(v)}
                      >
                        {type === v && v === "CARRO" && <CarFront size={19} />}
                        {type === v && v === "MOTO" && <Bike size={19} />}
                        {type === v && v === "CAMINHONETE" && <PickupSideIcon size={19} />}
                        {type === v && v === "CAMINHAO" && <Truck size={19} />}
                        {type === v && v === "BARCO" && <Ship size={19} />}
                        {type === v && v === "LINHA_AMARELA" && <Tractor size={19} />}
                        {label}
                      </button>
                    ))}
                  </div>
                  <span className="search-intro">
                    Qual vai ser o seu próximo?
                  </span>
                </div>
                <form
                  className="hero-search"
                  onSubmit={async (e) => {
                    e.preventDefault();
                    await applySearch(heroFilters());
                  }}
                >
                  <div className="brand-search" ref={vehicleSearchRef}>
                    <Search size={22} />
                    <span>
                      <small>ENCONTRE SEU VEÍCULO</small>
                      <input
                        value={vehicleQuery}
                        onChange={(event) => {
                          setVehicleQuery(event.target.value);
                          setBrand("");
                          setModel("");
                          setSuggestionsOpen(true);
                        }}
                        onFocus={() => setSuggestionsOpen(true)}
                        onKeyDown={(event) => {
                          if (event.key === "Escape") setSuggestionsOpen(false);
                        }}
                        role="combobox"
                        aria-label="Buscar por marca ou modelo"
                        aria-controls="vehicle-suggestions"
                        aria-expanded={suggestionsOpen}
                        aria-autocomplete="list"
                        placeholder="Digite uma marca ou modelo"
                        autoComplete="off"
                      />
                    </span>
                    {suggestionsOpen && (
                      <div className="vehicle-suggestions" id="vehicle-suggestions" role="listbox">
                        {vehicleSuggestions.length ? (
                          vehicleSuggestions.map((item, index) => (
                            <button
                              type="button"
                              role="option"
                              aria-selected={brand === item.brand && model === item.model}
                              key={`${item.kind}-${item.label}-${index}`}
                              onMouseDown={(event) => event.preventDefault()}
                              onClick={() => chooseVehicleSuggestion(item)}
                            >
                              <span>{item.label}</span>
                              <small>
                                {item.kind === "brand"
                                  ? "Marca"
                                  : item.kind === "model"
                                    ? `Modelo · ${item.context}`
                                    : "Marca + modelo"}
                              </small>
                            </button>
                          ))
                        ) : (
                          <p>Nenhuma sugestão encontrada.</p>
                        )}
                      </div>
                    )}
                  </div>
                  <div className="location-control" ref={locationSearchRef}>
                  <button
                    type="button"
                    className="location-search"
                    onClick={() => setLocationOpen((open) => !open)}
                    aria-expanded={locationOpen}
                    aria-controls="location-panel"
                  >
                    <MapPin size={21} />
                    <span>
                      <small>LOCALIZAÇÃO</small>{locationLabel}
                    </span>
                    <ChevronRight size={16} />
                  </button>
                  {locationOpen && (
                    <div className="location-panel" id="location-panel">
                      <div className="location-panel-head">
                        <div>
                          <small>ONDE PROCURAR</small>
                          <strong>Escolha sua localização</strong>
                        </div>
                        <button type="button" onClick={() => setLocationOpen(false)} aria-label="Fechar localização">
                          <X size={17} />
                        </button>
                      </div>
                      <button
                        type="button"
                        className={locationMode === "BRASIL" ? "location-choice selected" : "location-choice"}
                        onClick={() => {
                          setLocationMode("BRASIL");
                          setLocationOpen(false);
                        }}
                      >
                        <MapPin size={18} />
                        <span><strong>Todo o Brasil</strong><small>Ver anúncios de qualquer região</small></span>
                      </button>
                      <div className="location-fields">
                        <label>
                          Cidade
                          <input value={locationCity} onChange={(event) => setLocationCity(event.target.value)} placeholder="Ex.: São Paulo" />
                        </label>
                        <label>
                          UF
                          <select value={locationUf} onChange={(event) => setLocationUf(event.target.value)}>
                            <option value="">UF</option>
                            {"AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO".split(" ").map((uf) => <option key={uf}>{uf}</option>)}
                          </select>
                        </label>
                        <label className="location-cep">
                          CEP
                          <input inputMode="numeric" value={locationCep} onChange={(event) => setLocationCep(event.target.value)} placeholder="00000-000" />
                        </label>
                      </div>
                      {locationError && <p className="location-error" role="alert">{locationError}</p>}
                      <button type="button" className="button secondary location-apply" disabled={resolvingCep} onClick={applyTypedLocation}>
                        {resolvingCep ? "Localizando CEP…" : "Usar cidade, UF ou CEP"}
                      </button>
                      <div className="location-divider"><span>ou</span></div>
                      <button
                        type="button"
                        className="location-device"
                        disabled={requestingDeviceLocation}
                        onClick={async () => {
                          if (!(await authorizeDeviceLocation(true))) return;
                          setLocationMode("DISPOSITIVO");
                          setLocationOpen(false);
                        }}
                      >
                        <MapPin size={18} />
                        <span>
                          <strong>{requestingDeviceLocation ? "Aguardando sua permissão…" : "Usar minha localização"}</strong>
                          <small>O navegador pedirá sua permissão antes da busca</small>
                        </span>
                        <ChevronRight size={16} />
                      </button>
                    </div>
                  )}
                  </div>
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
                    onClick={() => void applySearch(heroFilters())}
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
                  <span className="demo-label">Até 20 oportunidades recentes</span>
                </div>
                {loading && homeVehicles.length === 0 ? (
                  <div className="vehicle-grid" aria-busy="true">
                    {[1, 2, 3, 4].map((n) => <div className="skeleton-card" key={n}><div /><span /><span /><span /></div>)}
                  </div>
                ) : catalogError && homeVehicles.length === 0 ? (
                  <div className="empty-state" role="alert">
                    <CarFront size={40} />
                    <h2>Não foi possível carregar a vitrine.</h2>
                    <p>{catalogError}</p>
                  </div>
                ) : homeVehicles.length === 0 ? (
                  <div className="empty-state">
                    <CarFront size={40} />
                    <h2>Ainda não há veículos publicados.</h2>
                    <p>Os primeiros anúncios publicados aparecerão aqui.</p>
                  </div>
                ) : (
                  <div className="vehicle-grid">{homeVehicles.slice(0, 20).map(card)}</div>
                )}
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
                    onApply={() => void applySearch(filters)}
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
                      {page === "favorites" ? shown.length : searchTotal}
                    </strong>{" "}
                    veículos {page === "favorites" ? "salvos" : "encontrados"}
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
                    </>
                  )}
                </div>
                {loading && shown.length === 0 ? (
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
                ) : catalogError && page === "search" ? (
                  <div className="empty-state" role="alert">
                    <CarFront size={40} />
                    <h2>Não foi possível carregar a vitrine.</h2>
                    <p>{catalogError}</p>
                    <button
                      className="button primary"
                      onClick={() => navigate("search", applied)}
                    >
                      Tentar novamente
                    </button>
                  </div>
                ) : !shown.length ? (
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
                        : "Experimente ampliar a busca ou remover alguns filtros."}
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
                {shown.length > 0 && !loading && (
                  <div className="catalog-end">
                    <span>{page === "search" && hasNextPage ? `${shown.length} de ${searchTotal} veículos` : "Você viu todos os veículos encontrados."}</span>
                    {page === "search" && hasNextPage ? (
                      <button className="button secondary" onClick={loadMore}>Carregar mais 52</button>
                    ) : (
                      <button className="text-button" onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}>Voltar ao topo ↑</button>
                    )}
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
            {!session?.autenticado && (
              <button onClick={() => openDestination("register")}>
                Criar conta
              </button>
            )}
          </nav>
          <button className="theme-footer" onClick={() => setDark(!dark)}>
            {dark ? <Sun size={16} /> : <Moon size={16} />}
            {dark ? "Modo claro" : "Modo escuro"}
          </button>
        </div>
        <div className="container footer-bottom">
          <span>© {new Date().getFullYear()} PHC Auto Repasse.</span>
          <span>Dados dos anúncios enviados pelos anunciantes.</span>
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
            onApply={() => void applySearch(filters)}
            onClear={() => setFilters({})}
          />
        </Modal>
      )}
    </>
  );
}
