import { useState } from "react";
import {
  ArrowRight,
  ArrowUpRight,
  CalendarDays,
  Camera,
  Fuel,
  Gauge,
  Heart,
  MapPin,
  MessageCircle,
  Settings2,
  Share2,
  ShieldCheck,
  Store,
  ZoomIn,
  ZoomOut,
} from "lucide-react";
import type { Vehicle } from "../data/catalog";
import { VehicleCard } from "../components/VehicleCard";
import { PageBreadcrumb } from "./PageLayout";

export function VehiclePage({
  vehicle,
  favorites,
  onSave,
  onOpen,
  related,
}: {
  vehicle: Vehicle;
  favorites: string[];
  onSave: (id: string) => void;
  onOpen: (id: string) => void;
  related: Vehicle[];
}) {
  const [zoom, setZoom] = useState(false);
  const [contact, setContact] = useState(false);
  const [shared, setShared] = useState("");
  const [shareFallback, setShareFallback] = useState(false);
  const saved = favorites.includes(vehicle.id);
  const specs = [
    ["Marca", vehicle.brand],
    ["Modelo", vehicle.model],
    ["Versão", vehicle.version],
    ["Ano de fabricação / modelo", vehicle.year],
    ["Quilometragem", vehicle.mileage ? `${vehicle.mileage} km` : "Não informada"],
    ["Câmbio", vehicle.transmission],
    ["Combustível", vehicle.fuel],
    ["Condição", vehicle.condition || "Não informada"],
    ["Cor", "Não informada"],
    ["Carroceria", "Não informada"],
    ["Motorização", "Não informada"],
    ["Cilindrada", "Não informada"],
    ["Tração", "Não informada"],
    ["Direção", "Não informada"],
    ["Freios", "Não informados"],
    ["Portas", "Não informado"],
    ["Lugares", "Não informado"],
    ["Placa disponibilizada", "Não informada"],
  ];
  async function share() {
    try {
      await navigator.clipboard.writeText(window.location.href);
      setShared("Link do anúncio copiado.");
    } catch {
      setShareFallback(true);
      setShared("Selecione e copie o endereço abaixo.");
    }
  }
  return (
    <section className="container dedicated-page vehicle-page">
      <PageBreadcrumb
        label={`${vehicle.brand} ${vehicle.model}`}
        parent={{ href: "#buscar", label: "Comprar veículos" }}
      />
      <div className="vehicle-page-header">
        <div>
          <span className="eyebrow">{vehicle.brand}</span>
          <h1>{vehicle.model}</h1>
          <p>{vehicle.version}</p>
          <span className="vehicle-page-location">
            <MapPin size={15} />
            {vehicle.location}
          </span>
        </div>
        <div className="vehicle-page-actions">
          <button
            className={`button secondary ${saved ? "is-saved" : ""}`}
            aria-pressed={saved}
            onClick={() => onSave(vehicle.id)}
          >
            <Heart size={17} fill={saved ? "currentColor" : "none"} />
            {saved ? "Salvo" : "Salvar"}
          </button>
          <button className="button secondary" onClick={share}>
            <Share2 size={17} />
            Compartilhar
          </button>
        </div>
      </div>
      {shared && (
        <div className="share-feedback">
          <p role="status">{shared}</p>
          {shareFallback && (
            <input
              aria-label="Link do anúncio"
              readOnly
              value={window.location.href}
              onFocus={(e) => e.target.select()}
            />
          )}
        </div>
      )}
      <div className="vehicle-page-layout">
        <div className="vehicle-page-main">
          <div className={`vehicle-gallery ${zoom ? "is-zoomed" : ""}`}>
            <img
              src={`/images/${vehicle.image}.jpg`}
              alt={`${vehicle.brand} ${vehicle.model}`}
            />
            <span className="gallery-caption">
              <Camera size={15} /> Imagem de referência
            </span>
            <button
              className="gallery-zoom"
              onClick={() => setZoom(!zoom)}
              aria-label={zoom ? "Reduzir foto" : "Ampliar foto"}
              aria-pressed={zoom}
            >
              {zoom ? <ZoomOut size={20} /> : <ZoomIn size={20} />}
            </button>
          </div>
          <div className="vehicle-highlights">
            <div>
              <CalendarDays />
              <span>
                Ano<strong>{vehicle.year}</strong>
              </span>
            </div>
            <div>
              <Gauge />
              <span>
                Quilometragem<strong>{vehicle.mileage ? `${vehicle.mileage} km` : "Não informada"}</strong>
              </span>
            </div>
            <div>
              <Settings2 />
              <span>
                Câmbio<strong>{vehicle.transmission}</strong>
              </span>
            </div>
            <div>
              <Fuel />
              <span>
                Combustível<strong>{vehicle.fuel}</strong>
              </span>
            </div>
          </div>
          <section className="vehicle-detail-section">
            <span className="eyebrow">CONHEÇA OS DETALHES</span>
            <h2>Sobre este veículo</h2>
            <p>
              Este {vehicle.brand} {vehicle.model} faz parte da vitrine da PHC
              Auto. Consulte a ficha técnica e converse com o anunciante antes
              de tomar sua decisão.
            </p>
          </section>
          <section className="vehicle-detail-section">
            <h2>Ficha técnica</h2>
            <dl className="full-specs">
              {specs.map(([label, value]) => (
                <div key={label}>
                  <dt>{label}</dt>
                  <dd>{value}</dd>
                </div>
              ))}
            </dl>
          </section>
          <section className="vehicle-detail-section">
            <h2>Informações adicionais</h2>
            <dl className="full-specs">
              {[
                "Único dono",
                "IPVA pago",
                "Licenciado",
                "Blindado",
                "Aceita troca",
              ].map((label) => (
                <div key={label}>
                  <dt>{label}</dt>
                  <dd>Não informado</dd>
                </div>
              ))}
            </dl>
          </section>
          <section className="vehicle-detail-section location-detail">
            <MapPin size={24} />
            <div>
              <h2>Localização</h2>
              <p>{vehicle.location}</p>
              <small>
                O endereço completo do veículo não é exibido publicamente.
              </small>
            </div>
          </section>
        </div>
        <aside className="vehicle-contact-aside">
          <div className="vehicle-contact-card">
            <span className="eyebrow">VALOR ANUNCIADO</span>
            <div className="price">
              <small>R$</small> {vehicle.price}
            </div>
            <span className="contact-rule" />
            <div className="seller-profile">
              <span>
                <Store size={25} />
              </span>
              <div>
                <small>ANUNCIANTE</small>
                <h2>{vehicle.seller}</h2>
                <p>
                  <MapPin size={13} />
                  {vehicle.location}
                </p>
              </div>
            </div>
            <button className="button primary" onClick={() => setContact(true)}>
              <MessageCircle size={18} />
              Falar com anunciante
              <ArrowUpRight size={17} />
            </button>
            {contact && (
              <p className="inline-notice" role="status">
                Use os dados de contato disponibilizados pelo anunciante. O
                envio de mensagens pela plataforma ainda não está disponível.
              </p>
            )}
            <p className="contact-note">
              Gostou do veículo? Salve para consultar novamente durante esta
              visita.
            </p>
          </div>
          <div className="vehicle-info-note">
            <ShieldCheck size={22} />
            <div>
              <h3>Informação antes da decisão.</h3>
              <p>
                Confira os dados e converse com o anunciante antes de fechar seu
                negócio.
              </p>
            </div>
          </div>
          <a href="#anunciar/dados" className="vehicle-announce-link">
            Também quer anunciar?
            <span>
              Prepare seu anúncio
              <ArrowRight size={16} />
            </span>
          </a>
        </aside>
      </div>
      <section className="related-vehicles">
        <div className="section-heading">
          <div>
            <span className="eyebrow">CONTINUE EXPLORANDO</span>
            <h2>Outras oportunidades na vitrine.</h2>
          </div>
          <a className="text-button" href="#buscar">
            Ver todos
            <ArrowUpRight size={17} />
          </a>
        </div>
        <div className="vehicle-grid">
          {related
            .filter((v) => v.id !== vehicle.id)
            .slice(0, 4)
            .map((v) => (
              <VehicleCard
                key={v.id}
                vehicle={v}
                saved={favorites.includes(v.id)}
                onSave={() => onSave(v.id)}
                onOpen={() => onOpen(v.id)}
              />
            ))}
        </div>
      </section>
    </section>
  );
}

export function NotFoundPage() {
  return (
    <section className="container dedicated-page">
      <PageBreadcrumb label="Página não encontrada" />
      <div className="empty-state">
        <MapPin size={40} />
        <h1>Esse caminho não está disponível.</h1>
        <p>
          O anúncio ou a página que você tentou acessar não foi encontrado nesta
          prévia.
        </p>
        <a className="button primary" href="#buscar">
          Explorar veículos
          <ArrowRight size={17} />
        </a>
      </div>
    </section>
  );
}
