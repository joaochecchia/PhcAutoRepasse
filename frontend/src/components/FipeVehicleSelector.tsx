import { useEffect, useRef, useState } from "react";
import { apiErrorMessage, getFipeActiveEdition, getFipePage, resolveFipeReference, type FipeOption, type FipeOptionsQuery, type FipeReference } from "../lib/backend";

export const fipeVehicleTypes = { CARRO: "cars", CAMINHONETE: "cars", MOTO: "motorcycles", CAMINHAO: "trucks" } as const;

type Choice = FipeOption | undefined;
// While the backend reports the catalog as pending it keeps loading it in the background; poll for ~1 minute.
const PENDING_RETRY_MS = 6000;
const MAX_PENDING_RETRIES = 10;
const ZERO_KM = "32000";

function CatalogInput({ label, value, query, onPick, onType, disabled, placeholder, hint, className, numeric, required = true }: {
  label: string; value: string; query: FipeOptionsQuery | null;
  onPick: (choice: Choice) => void; onType: (value: string) => void;
  disabled?: boolean; placeholder?: string; hint?: string; className?: string; numeric?: boolean; required?: boolean;
}) {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<FipeOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  const [pending, setPending] = useState(false);
  const autoRetries = useRef(0);
  const queryKey = JSON.stringify(query);
  useEffect(() => { setItems([]); setPending(false); autoRetries.current = 0; }, [queryKey]);
  useEffect(() => {
    if (!pending || !open || autoRetries.current >= MAX_PENDING_RETRIES) return;
    const timer = window.setTimeout(() => { autoRetries.current += 1; setRetry((n) => n + 1); }, PENDING_RETRY_MS);
    return () => window.clearTimeout(timer);
  }, [pending, open, retry]);
  useEffect(() => {
    if (!open || !query || disabled) return;
    const controller = new AbortController();
    const timer = window.setTimeout(() => {
      setLoading(true);
      setError("");
      getFipePage({ ...query, termo: value, pagina: 0 }, controller.signal)
        .then((result) => { if (!controller.signal.aborted) { setItems(result.opcoes); setPending(result.catalogoPendente); } })
        .catch((reason) => { if (!controller.signal.aborted) setError(apiErrorMessage(reason, "Sugestões indisponíveis. Digite os dados manualmente.")); })
        .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    }, value ? 250 : 0);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [open, queryKey, value, disabled, retry]);
  return <label className={`fipe-integrated-field${className ? ` ${className}` : ""}`}>
    {label} {required && <span className="required-mark" aria-hidden="true">*</span>}
    <div className="field-autocomplete">
      <input required={required} value={value} disabled={disabled} maxLength={100} autoComplete="off"
        placeholder={placeholder || `Obrigatório — ${label.toLowerCase()}`} inputMode={numeric ? "numeric" : undefined}
        role="combobox" aria-expanded={open} aria-autocomplete="list"
        onFocus={() => setOpen(true)} onBlur={() => setOpen(false)}
        onChange={(event) => { onType(event.target.value); setOpen(true); }} />
      {open && items.length > 0 && <div className="field-suggestions" role="listbox" aria-label={`Sugestões de ${label}`}>
        {items.map((item) => <button key={`${item.codigo}:${item.anoId ?? ""}`} type="button" role="option" aria-selected={item.nome === value}
          onMouseDown={(event) => event.preventDefault()}
          onClick={() => { onPick(item); setOpen(false); }}>{item.nome}</button>)}
      </div>}
    </div>
    {(loading || (pending && autoRetries.current < MAX_PENDING_RETRIES)) && open && items.length === 0
      ? <small role="status">Buscando na tabela FIPE… Se preferir, digite manualmente.</small>
      : loading && <small role="status">Carregando sugestões…</small>}
    {open && !loading && !error && query && items.length === 0 && (pending
      ? autoRetries.current >= MAX_PENDING_RETRIES && <small role="status">Tabela FIPE indisponível no momento. Digite manualmente ou <button type="button" onMouseDown={(event) => event.preventDefault()} onClick={() => { autoRetries.current = 0; setRetry((n) => n + 1); }}>tente novamente</button>.</small>
      : <small>Nenhuma opção FIPE encontrada. Você pode digitar manualmente.</small>)}
    {open && hint && !query && <small>{hint}</small>}
    {error && <small role="alert">{error} <button type="button" onClick={() => setRetry((n) => n + 1)}>Tentar novamente</button></small>}
  </label>;
}

export function FipeSummary({ reference }: { reference: FipeReference }) {
  return <div className="fipe-result" role="status">
    <strong>Referência FIPE</strong>
    <span>{reference.codigoFipe} · {new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric", timeZone: "UTC" }).format(new Date(`${reference.mesReferencia}T00:00:00Z`))}</span>
    <strong>{new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(reference.valorCentavos / 100)}</strong>
    <small>Valor de referência; informe o preço do anúncio separadamente.</small>
  </div>;
}

export function FipeVehicleFields({ vehicleType, fabricante, modelo, anoModelo, reference, onChange, onResolved, onClear }: {
  vehicleType: keyof typeof fipeVehicleTypes;
  fabricante: string; modelo: string; anoModelo: string; reference: FipeReference | null;
  onChange: (key: string, value: string) => void;
  onResolved: (reference: FipeReference) => void; onClear: () => void;
}) {
  const [month, setMonth] = useState<Choice>();
  const [brand, setBrand] = useState<Choice>();
  // Ano-modelo escolhido ou digitado (32000 = zero km); os modelos sugeridos dependem de marca + ano.
  const [year, setYear] = useState<Choice>();
  const [error, setError] = useState("");
  const [editionLoading, setEditionLoading] = useState(true);
  const [loading, setLoading] = useState(false);
  const pending = useRef<AbortController | null>(null);
  useEffect(() => {
    const controller = new AbortController();
    getFipeActiveEdition(fipeVehicleTypes[vehicleType], controller.signal)
      .then((result) => { if (!controller.signal.aborted) setMonth(result); })
      .catch(async () => {
        try {
          const result = await getFipePage({}, controller.signal);
          if (!controller.signal.aborted) setMonth(result.opcoes[0]);
        } catch { if (!controller.signal.aborted) setError("Catálogo local indisponível. Continue o preenchimento manual."); }
      }).finally(() => { if (!controller.signal.aborted) setEditionLoading(false); });
    return () => { controller.abort(); pending.current?.abort(); };
  }, []);
  const clear = () => { pending.current?.abort(); setLoading(false); setError(""); onClear(); };
  const edition = month ? Number(month.codigo) : undefined;
  const query = { tipo: fipeVehicleTypes[vehicleType], referencia: edition };
  const zeroKm = year?.codigo === ZERO_KM;
  function pickYear(choice: Choice) {
    clear(); setYear(choice);
    // Zero km não tem ano civil na FIPE: o campo mostra "Zero km" e o ano do modelo fica para o usuário.
    onChange("anoModelo", !choice || choice.codigo === ZERO_KM ? "" : choice.codigo);
  }
  async function selectModel(choice: Choice) {
    clear();
    onChange("modelo", choice?.nome.split(" — ")[0] || "");
    if (!choice?.anoId || !brand || !month?.mesReferencia) return;
    const controller = new AbortController(); pending.current = controller; setLoading(true);
    try {
      const result = await resolveFipeReference({ tipo: fipeVehicleTypes[vehicleType], marcaId: brand.codigo,
        modeloId: choice.codigo, anoId: choice.anoId, referencia: Number(month.codigo), mesReferencia: month.mesReferencia }, controller.signal);
      if (!controller.signal.aborted) onResolved(result);
    } catch (reason) { if (!controller.signal.aborted) setError(apiErrorMessage(reason, "Referência indisponível. Continue o cadastro manualmente.")); }
    finally { if (!controller.signal.aborted) setLoading(false); }
  }
  return <>
    <div className="page-form-grid">
      <CatalogInput label="Marca / fabricante" value={fabricante} query={edition ? query : null}
        onType={(value) => { clear(); setBrand(undefined); setYear(undefined); onChange("fabricante", value); }}
        onPick={(choice) => { clear(); setBrand(choice); setYear(undefined); onChange("fabricante", choice?.nome || ""); }} />
      <CatalogInput label="Ano do modelo" value={zeroKm ? "Zero km" : anoModelo} numeric={!zeroKm}
        query={brand ? { ...query, marcaId: brand.codigo, anos: true } : null}
        placeholder="Obrigatório — selecione ou digite o ano" hint="Escolha a marca para ver os anos disponíveis."
        onType={(value) => {
          const digits = value.replace(/[^0-9]/g, "").slice(0, 4);
          clear(); setYear(digits.length === 4 ? { codigo: digits, nome: digits, mesReferencia: null } : undefined);
          onChange("anoModelo", digits);
        }}
        onPick={pickYear} />
      <CatalogInput label="Modelo / versão" value={modelo} className="fipe-model-field"
        query={brand && year ? { ...query, marcaId: brand.codigo, ano: Number(year.codigo) } : null}
        placeholder="Obrigatório — selecione ou digite modelo e versão" hint="Escolha a marca e o ano para ver os modelos daquele ano."
        onType={(value) => { clear(); onChange("modelo", value); }}
        onPick={selectModel} />
    </div>
    {loading && <p role="status">Consultando referência FIPE local…</p>}
    {!editionLoading && !month && !error && <p className="inline-notice">O catálogo FIPE ainda não está carregado. Preencha os dados manualmente.</p>}
    {error && <p role="alert" className="inline-notice">{error}</p>}
    {reference && <FipeSummary reference={reference} />}
  </>;
}
