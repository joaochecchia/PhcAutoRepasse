import { useEffect, useId, useRef, useState } from "react";
import { Check, ChevronDown, SlidersHorizontal, X } from "lucide-react";
import { filterGroups, type Filters as Values } from "../data/filters";
import { types } from "../data/catalog";

function FilterSelect({
  label,
  value,
  options,
  placeholder = "Selecione",
  onChange,
}: {
  label: string;
  value: string;
  options: [string, string][];
  placeholder?: string;
  onChange: (value: string) => void;
}) {
  const id = useId();
  const root = useRef<HTMLDivElement>(null);
  const [open, setOpen] = useState(false);
  const selectedIndex = options.findIndex(([key]) => key === value);
  const [activeIndex, setActiveIndex] = useState(Math.max(0, selectedIndex));
  const selectedLabel = selectedIndex >= 0 ? options[selectedIndex][1] : placeholder;

  useEffect(() => {
    const closeOnOutsideClick = (event: PointerEvent) => {
      if (!root.current?.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener("pointerdown", closeOnOutsideClick);
    return () => document.removeEventListener("pointerdown", closeOnOutsideClick);
  }, []);

  const choose = (index: number) => {
    onChange(options[index][0]);
    setActiveIndex(index);
    setOpen(false);
  };

  return (
    <div className="filter-select" ref={root}>
      <button
        type="button"
        className="filter-select-trigger"
        role="combobox"
        aria-label={label}
        aria-expanded={open}
        aria-controls={`${id}-options`}
        aria-activedescendant={open ? `${id}-option-${activeIndex}` : undefined}
        onClick={() => {
          setActiveIndex(Math.max(0, selectedIndex));
          setOpen((current) => !current);
        }}
        onKeyDown={(event) => {
          if (event.key === "ArrowDown" || event.key === "ArrowUp") {
            event.preventDefault();
            setOpen(true);
            setActiveIndex((current) =>
              (current + (event.key === "ArrowDown" ? 1 : options.length - 1)) % options.length,
            );
          } else if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            if (open) choose(activeIndex);
            else setOpen(true);
          } else if (event.key === "Escape") {
            setOpen(false);
          }
        }}
      >
        <span className={selectedIndex < 0 ? "is-placeholder" : ""}>{selectedLabel}</span>
        <ChevronDown size={15} aria-hidden="true" />
      </button>
      {open && (
        <div className="filter-select-options" id={`${id}-options`} role="listbox" aria-label={label}>
          {options.map(([key, optionLabel], index) => (
            <button
              type="button"
              role="option"
              id={`${id}-option-${index}`}
              aria-selected={key === value}
              className={index === activeIndex ? "is-active" : ""}
              key={key || "all"}
              onPointerEnter={() => setActiveIndex(index)}
              onClick={() => choose(index)}
            >
              <span>{optionLabel}</span>
              {key === value && <Check size={14} aria-hidden="true" />}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

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
          <FilterSelect
            label="Tipo de veículo"
            value={values.tipoVeiculo || ""}
            options={[
              ["", "Todos os veículos"],
              ...types.map(([key, label]) => [key, label] as [string, string]),
            ]}
            onChange={(value) => update("tipoVeiculo", value)}
          />
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
                    <FilterSelect
                      label={f.label}
                      value={values[f.key] || ""}
                      options={[["", "Selecione"], ...f.options]}
                      onChange={(value) => update(f.key, value)}
                    />
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
