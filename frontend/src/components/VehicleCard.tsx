import { ArrowUpRight, CalendarDays, Gauge, Heart, MapPin } from "lucide-react";
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
  return (
    <article className="vehicle-card">
      <div className="vehicle-photo">
        <button
          className="photo-button"
          onClick={onOpen}
          aria-label={`Ver ${v.brand} ${v.model}`}
        >
          <img
            src={`/images/${v.image}.jpg`}
            alt={`${v.brand} ${v.model}, imagem ilustrativa`}
            loading="lazy"
            onError={(e) => {
              e.currentTarget.style.display = "none";
            }}
          />
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
        <div className="vehicle-specs">
          <span>
            <CalendarDays size={14} />
            {v.year}
          </span>
          <span>
            <Gauge size={14} />
            {v.mileage} km
          </span>
        </div>
        <div className="price">
          <small>R$</small> {v.price}
        </div>
        <div className="vehicle-bottom">
          <span>
            <MapPin size={14} />
            {v.location}
          </span>
          <button onClick={onOpen} aria-label={`Detalhes de ${v.model}`}>
            <ArrowUpRight size={19} />
          </button>
        </div>
      </div>
    </article>
  );
}
