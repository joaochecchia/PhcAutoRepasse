import { ChevronDown, SlidersHorizontal, X } from "lucide-react";
import { filterGroups, type Filters as Values } from "../data/filters";
import { types } from "../data/catalog";

export function FilterPanel({
  values,
  setValues,
  onApply,
  onClear,
}: {
  values: Values;
  setValues: (v: Values) => void;
  onApply: () => void;
  onClear: () => void;
}) {
  const update = (key: string, value: string) => {
    const next = { ...values, [key]: value };
    // Reset dependent inputs when switching the visible location form.
    if (key === "modoLocalizacao") {
      delete next.cidade;
      delete next.uf;
    }
    setValues(next);
  };
  return (
    <div className="filter-panel">
      <div className="filter-heading">
        <h2>
          <SlidersHorizontal size={18} /> Filtros
        </h2>
        <button className="text-button" onClick={onClear}>
          Limpar
        </button>
      </div>
      <div className="filter-section">
        <label>
          Tipo de veículo
          <select
            value={values.tipoVeiculo || ""}
            onChange={(e) => update("tipoVeiculo", e.target.value)}
          >
            <option value="">Todos os veículos</option>
            {types.map(([v, l]) => (
              <option key={v} value={v}>
                {l}
              </option>
            ))}
          </select>
        </label>
      </div>
      {filterGroups.map((g, i) => (
        <details className="filter-section" key={g.title} open={i < 3}>
          <summary>
            {g.title}
            <ChevronDown size={16} />
          </summary>
          <div
            className={
              g.title === "Preço e ano"
                ? "filter-fields paired"
                : "filter-fields"
            }
          >
            {g.fields.map((f) => {
              if (f.key === "cidade" && values.modoLocalizacao !== "CIDADE")
                return null;
              if (
                f.key === "uf" &&
                !["CIDADE", "UF"].includes(values.modoLocalizacao)
              )
                return null;
              return (
                <label key={f.key}>
                  {f.label}
                  {f.options ? (
                    <select
                      value={values[f.key] || ""}
                      onChange={(e) => update(f.key, e.target.value)}
                    >
                      <option value="">Selecione</option>
                      {f.options.map(([v, l]) => (
                        <option key={v} value={v}>
                          {l}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <input
                      type={f.type || "text"}
                      step={f.type === "number" ? "any" : undefined}
                      placeholder={f.placeholder}
                      value={values[f.key] || ""}
                      onChange={(e) => update(f.key, e.target.value)}
                    />
                  )}
                </label>
              );
            })}
            {g.title === "Localização" &&
              ["DISPOSITIVO", "ENDERECO_CADASTRADO"].includes(
                values.modoLocalizacao,
              ) && (
                <p className="field-note">
                  {values.modoLocalizacao === "DISPOSITIVO"
                    ? "A permissão de localização será solicitada quando a busca estiver conectada."
                    : "A busca pelo seu endereço estará disponível após a integração do login."}
                </p>
              )}
          </div>
        </details>
      ))}
      <div className="filter-action">
        <button className="button primary" onClick={onApply}>
          Aplicar filtros
        </button>
      </div>
    </div>
  );
}

export function FilterChips({
  values,
  onRemove,
}: {
  values: Values;
  onRemove: (key: string) => void;
}) {
  const fields = filterGroups.flatMap((g) => g.fields);
  return (
    <div className="filter-chips">
      {Object.entries(values)
        .filter(([, v]) => v && v !== "BRASIL")
        .map(([k, v]) => {
          const field = fields.find((f) => f.key === k);
          const label =
            k === "tipoVeiculo"
              ? types.find((t) => t[0] === v)?.[1]
              : field?.options?.find((o) => o[0] === v)?.[1] || v;
          return (
            <button
              key={k}
              onClick={() => onRemove(k)}
              aria-label={`Remover filtro ${field?.label || "Tipo"}: ${label}`}
            >
              {field?.label ? `${field.label}: ` : ""}
              {label}
              <X size={13} />
            </button>
          );
        })}
    </div>
  );
}
