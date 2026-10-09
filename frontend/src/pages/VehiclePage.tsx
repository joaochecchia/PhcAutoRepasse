import { useEffect, useState } from "react";
import {
  ArrowRight,
  ArrowUpRight,
  CalendarDays,
  Camera,
  CameraOff,
  ChevronLeft,
  ChevronRight,
  Fuel,
  Gavel,
  Gauge,
  Heart,
  MapPin,
  MessageCircle,
  Settings2,
  Share2,
  ShieldCheck,
  Store,
  TriangleAlert,
  ZoomIn,
  ZoomOut,
} from "lucide-react";
import type { Vehicle } from "../data/catalog";
import { VehicleCard } from "../components/VehicleCard";
import { PageBreadcrumb } from "./PageLayout";
import { apiErrorMessage, listAdPhotos, requestWhatsappContact } from "../lib/backend";
import type { AdPhoto } from "../lib/contracts";

export function VehiclePage({
  vehicle,
  favorites,
  onSave,
  onOpen,
  related,
  authenticated,
}: {
  vehicle: Vehicle;
  favorites: string[];
  onSave: (id: string) => void;
  onOpen: (id: string) => void;
  related: Vehicle[];
  authenticated: boolean;
}) {
  const [zoom, setZoom] = useState(false);
  const [imageFailed, setImageFailed] = useState(false);
  const [photos, setPhotos] = useState<AdPhoto[]>([]);
  const [photoIndex, setPhotoIndex] = useState(0);
  const [photosLoading, setPhotosLoading] = useState(false);
  const [photosError, setPhotosError] = useState(false);
  const [contactError, setContactError] = useState("");
  const [contactLoading, setContactLoading] = useState(false);
  const [shared, setShared] = useState("");
  const [shareFallback, setShareFallback] = useState(false);
  const saved = favorites.includes(vehicle.id);
  const fallbackImage = vehicle.imageUrl || (vehicle.image ? `/images/${vehicle.image}.jpg` : null);
  const currentPhoto = photos[photoIndex];
  const imageSource = currentPhoto?.url || fallbackImage;
  const imageAlt = currentPhoto?.textoAlternativo?.trim() || `${vehicle.brand} ${vehicle.model}`;
  useEffect(() => {
    let active = true;
    setPhotos([]);
    setPhotoIndex(0);
    setImageFailed(false);
    setPhotosError(false);
    if (!vehicle.live) return () => { active = false; };
    setPhotosLoading(true);
    listAdPhotos(vehicle.id)
      .then((result) => {
        if (active) setPhotos([...result].sort((left, right) => left.posicao - right.posicao));
      })
      .catch(() => {
        if (active) setPhotosError(true);
      })
      .finally(() => {
        if (active) setPhotosLoading(false);
      });
    return () => { active = false; };
  }, [vehicle.id, vehicle.live]);
  useEffect(() => {
    setImageFailed(false);
    setZoom(false);
  }, [photoIndex, imageSource]);
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
            {imageSource && !imageFailed ? (
              <img
                src={imageSource}
                alt={imageAlt}
                onError={() => setImageFailed(true)}
              />
            ) : (
              <div className="vehicle-image-placeholder vehicle-image-placeholder-large" role="img" aria-label="Anúncio sem fotos">
                <CameraOff size={46} />
                <span>Este anúncio ainda não possui fotos</span>
              </div>
            )}
            <span className="gallery-caption">
              <Camera size={15} /> {imageSource && !imageFailed
                ? photos.length > 1 ? `${photoIndex + 1} de ${photos.length}` : "Foto do veículo"
                : "Sem fotos cadastradas"}
            </span>
            {photos.length > 1 && !imageFailed && (
              <>
                <button
                  className="gallery-navigation gallery-previous"
                  onClick={() => setPhotoIndex((photoIndex - 1 + photos.length) % photos.length)}
                  aria-label="Foto anterior"
                >
                  <ChevronLeft size={24} />
                </button>
                <button
                  className="gallery-navigation gallery-next"
                  onClick={() => setPhotoIndex((photoIndex + 1) % photos.length)}
                  aria-label="Próxima foto"
                >
                  <ChevronRight size={24} />
                </button>
              </>
            )}
            {imageSource && !imageFailed && (
              <button
                className="gallery-zoom"
                onClick={() => setZoom(!zoom)}
                aria-label={zoom ? "Reduzir foto" : "Ampliar foto"}
                aria-pressed={zoom}
              >
                {zoom ? <ZoomOut size={20} /> : <ZoomIn size={20} />}
              </button>
            )}
          </div>
          {photos.length > 1 && (
            <div className="vehicle-gallery-thumbnails" aria-label="Fotos do veículo">
              {photos.map((photo, index) => (
                <button
                  key={photo.id}
                  className={index === photoIndex ? "is-active" : ""}
                  onClick={() => setPhotoIndex(index)}
                  aria-label={`Exibir foto ${index + 1} de ${photos.length}`}
                  aria-current={index === photoIndex ? "true" : undefined}
                >
                  <img src={photo.url} alt="" loading="lazy" />
                </button>
              ))}
            </div>
          )}
          {photosLoading && <p className="gallery-status" role="status">Carregando fotos…</p>}
          {photosError && <p className="gallery-status gallery-error" role="status">Não foi possível carregar todas as fotos.</p>}
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
          <section className="vehicle-detail-section vehicle-history-section">
            <span className="eyebrow">TRANSPARÊNCIA DO ANÚNCIO</span>
            <h2>Histórico declarado</h2>
            <p>Informações fornecidas pelo anunciante sobre o histórico deste veículo.</p>
            <div className="vehicle-history-details">
              {[
                {
                  label: "Passagem por leilão",
                  value: vehicle.auctionHistory,
                  icon: Gavel,
                },
                {
                  label: "Registro de sinistro",
                  value: vehicle.accidentHistory,
                  icon: TriangleAlert,
                },
              ].map(({ label, value, icon: Icon }) => (
                <div
                  className={`vehicle-history-detail ${value == null ? "is-unknown" : value ? "has-record" : "is-clear"}`}
                  key={label}
                >
                  <span className="vehicle-history-detail-icon"><Icon size={18} /></span>
                  <span>
                    <small>{label}</small>
                    <strong>{value == null ? "Não informado" : value ? "Sim" : "Não"}</strong>
                  </span>
                  {value === false && <ShieldCheck className="history-state-icon" size={17} aria-hidden="true" />}
                </div>
              ))}
            </div>
          </section>
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
            <button className="button primary" disabled={contactLoading} onClick={async () => {
              if (!authenticated) {
                setContactError("Você precisa estar logado para falar com o anunciante.");
                return;
              }
              const whatsappTab = window.open("about:blank", "_blank");
              if (!whatsappTab) {
                setContactError("Permita a abertura de novas guias para conversar com o anunciante.");
                return;
              }
              whatsappTab.opener = null;
              whatsappTab.document.title = "Abrindo WhatsApp…";
              setContactLoading(true);
              setContactError("");
              try {
                const { url } = await requestWhatsappContact(vehicle.id);
                whatsappTab.location.replace(url);
              } catch (error) {
                whatsappTab.close();
                setContactError(apiErrorMessage(error, "Não foi possível abrir o contato do anunciante."));
              } finally {
                setContactLoading(false);
              }
            }}>
              <MessageCircle size={18} />
              {contactLoading ? "Abrindo WhatsApp…" : "Falar com anunciante"}
              <ArrowUpRight size={17} />
            </button>
            {contactError && <p className="inline-notice error-notice" role="alert">{contactError}</p>}
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
