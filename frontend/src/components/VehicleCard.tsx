import {
  ArrowUpRight,
  CalendarDays,
  CameraOff,
  Gauge,
  Heart,
  MapPin,
  ShieldCheck,
  TriangleAlert,
} from "lucide-react";
import { useState } from "react";
import type { Vehicle } from "../data/catalog";

export function VehicleCard({
  vehicle: v,
  saved,
  onSave,
  onOpen,
}: {
  vehicle: Vehicle;
  saved: boolean;
  onSave: () => void;
  onOpen: () => void;
}) {
  const [imageFailed, setImageFailed] = useState(false);
  const imageSource = v.imageUrl || (v.image ? `/images/${v.image}.jpg` : null);
  const reportedHistory = [
    v.auctionHistory ? "Leilão" : "",
    v.accidentHistory ? "Sinistro" : "",
  ].filter(Boolean);
  const historyIsComplete = v.auctionHistory != null && v.accidentHistory != null;
  const historyLabel = reportedHistory.length
    ? `${reportedHistory.join(" e ")} ${reportedHistory.length > 1 ? "informados" : "informado"}`
    : historyIsComplete
      ? "Sem leilão ou sinistro"
      : "";
  return (
    <article className="vehicle-card">
      <div className="vehicle-photo">
        <button
          className="photo-button"
          onClick={onOpen}
          aria-label={`Ver ${v.brand} ${v.model}`}
        >
          {imageSource && !imageFailed ? (
            <img
              src={imageSource}
              alt={`${v.brand} ${v.model}`}
              loading="lazy"
              onError={() => setImageFailed(true)}
            />
          ) : (
            <span className="vehicle-image-placeholder" role="img" aria-label="Anúncio sem fotos">
              <CameraOff size={30} />
              <span>Sem fotos</span>
            </span>
          )}
        </button>
        {v.tag && <span className="vehicle-tag">{v.tag}</span>}
        <button
          className={`save-button ${saved ? "saved" : ""}`}
          aria-label={`${saved ? "Remover" : "Salvar"} ${v.brand} ${v.model} ${saved ? "dos" : "nos"} favoritos`}
          aria-pressed={saved}
          onClick={onSave}
        >
          <Heart size={18} fill={saved ? "currentColor" : "none"} />
        </button>
      </div>
      <div className="vehicle-info">
        <span className="vehicle-brand">{v.brand}</span>
        <button className="vehicle-title" onClick={onOpen}>
          {v.model}
        </button>
        <p className="version">{v.version}</p>
        {historyLabel && (
          <div
            className={`vehicle-history ${reportedHistory.length ? "history-alert" : "history-clear"}`}
            aria-label={`Histórico do veículo: ${historyLabel}`}
          >
            {reportedHistory.length ? <TriangleAlert size={13} /> : <ShieldCheck size={13} />}
            <span>{historyLabel}</span>
          </div>
        )}
        <div className="vehicle-specs">
          <span>
            <CalendarDays size={14} />
            {v.year}
          </span>
          <span>
            <Gauge size={14} />
            {v.mileage ? `${v.mileage} km` : "Não informada"}
          </span>
        </div>
        <div className="vehicle-location">
          <MapPin size={14} />
          <span>{v.location || "Localização não informada"}</span>
        </div>
        <div className="price">
          <small>R$</small> {v.price}
        </div>
        <div className="vehicle-bottom">
          <span>Ver anúncio completo</span>
          <button onClick={onOpen} aria-label={`Detalhes de ${v.model}`}>
            <ArrowUpRight size={19} />
          </button>
        </div>
      </div>
    </article>
  );
}
