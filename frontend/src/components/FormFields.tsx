import { useEffect, useId, useRef, useState } from "react";
import {
  CheckCircle2,
  CircleAlert,
  Eye,
  EyeOff,
  LoaderCircle,
  ChevronDown,
} from "lucide-react";
import { findAddressByCep } from "../lib/viacep";

export type Draft = Record<string, string>;
export type Field = {
  key: string;
  unprefixed?: boolean;
  label: string;
  type?: string;
  required?: boolean;
  options?: [string, string][];
  placeholder?: string;
  autoComplete?: string;
  hint?: string;
  suggestions?: { value: string; aliases?: string[] }[];
  strictSuggestions?: boolean;
  autoCorrectSuggestions?: boolean;
  disabled?: boolean;
  min?: number;
  step?: number | "any";
  pattern?: string;
  maxLength?: number;
};

function SiteSelect({ field, id, value, onChange, showRequirement }: {
  field: Field; id: string; value: string; onChange: (value: string) => void; showRequirement: boolean;
}) {
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const options: [string, string][] = [["", "Selecione"], ...(field.options || [])];
  const choose = (index: number) => { onChange(options[index][0]); setOpen(false); };
  return <div className="field-autocomplete site-select" onBlur={(event) => {
    if (!event.currentTarget.contains(event.relatedTarget)) setOpen(false);
  }}>
    <input id={id} aria-label={field.label} role="combobox" aria-expanded={open} aria-controls={`${id}-options`}
      aria-autocomplete="none" aria-activedescendant={open ? `${id}-option-${active}` : undefined}
      required={field.required} disabled={field.disabled} autoComplete="off"
      value={field.options?.find(([key]) => key === value)?.[1] || ""}
      placeholder={showRequirement ? `${field.required ? "Obrigatório" : "Opcional"} — selecione` : "Selecione"}
      onChange={() => {}} onClick={() => setOpen(!open)}
      onKeyDown={(event) => {
        if (event.key === "ArrowDown" || event.key === "ArrowUp") {
          event.preventDefault(); setOpen(true);
          setActive((index) => (index + (event.key === "ArrowDown" ? 1 : options.length - 1)) % options.length);
        } else if (event.key === "Enter" || event.key === " ") {
          event.preventDefault(); if (open) choose(active); else setOpen(true);
        } else if (event.key === "Escape" || event.key === "Tab") setOpen(false);
      }} />
    <ChevronDown className="site-select-chevron" size={16} aria-hidden="true" />
    {open && <div id={`${id}-options`} role="listbox" className="field-suggestions" aria-label={field.label}>
      {options.map(([key, label], index) => <button type="button" role="option" tabIndex={-1}
        id={`${id}-option-${index}`} key={key} aria-selected={value === key}
        className={active === index ? "is-active" : ""}
        onPointerDown={(event) => event.preventDefault()} onClick={(event) => { event.preventDefault(); choose(index); }}>{label}</button>)}
    </div>}
  </div>;
}

const normalized = (value: string) =>
  value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[^a-zA-Z0-9]/g, "").toLocaleLowerCase("pt-BR");

function editDistance(left: string, right: string) {
  const previous = Array.from({ length: right.length + 1 }, (_, index) => index);
  for (let leftIndex = 1; leftIndex <= left.length; leftIndex += 1) {
    let diagonal = previous[0];
    previous[0] = leftIndex;
    for (let rightIndex = 1; rightIndex <= right.length; rightIndex += 1) {
      const above = previous[rightIndex];
      previous[rightIndex] = left[leftIndex - 1] === right[rightIndex - 1]
        ? diagonal
        : Math.min(diagonal, above, previous[rightIndex - 1]) + 1;
      diagonal = above;
    }
  }
  return previous[right.length];
}

function suggestionScore(query: string, suggestion: { value: string; aliases?: string[] }) {
  return Math.min(...[suggestion.value, ...(suggestion.aliases || [])].map((candidate) => {
    const option = normalized(candidate);
    if (option === query) return 0;
    if (option.startsWith(query)) return 1;
    if (option.includes(query)) return 2;
    return 3 + editDistance(query, option);
  }));
}

function SuggestedInput({
  field,
  id,
  name,
  value,
  onChange,
}: {
  field: Field;
  id: string;
  name: string;
  value: string;
  onChange: (value: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const [invalid, setInvalid] = useState(false);
  const input = useRef<HTMLInputElement>(null);
  const query = normalized(value);
  const ranked = (field.suggestions || [])
    .map((item) => ({ item, score: suggestionScore(query, item) }))
    .sort((left, right) => left.score - right.score || left.item.value.localeCompare(right.item.value, "pt-BR"));
  const distanceLimit = Math.max(1, Math.floor(query.length * 0.25));
  const choices = ranked.filter(({ score }) => !query || score <= 2 || score - 3 <= distanceLimit)
    .slice(0, 12).map(({ item }) => item);
  const canonical = (candidate: string) => (field.suggestions || []).find((item) =>
    [item.value, ...(item.aliases || [])].some((option) => normalized(option) === normalized(candidate)),
  );
  const closest = (candidate: string) => {
    const exact = canonical(candidate);
    if (exact) return exact;
    const candidateQuery = normalized(candidate);
    if (!candidateQuery) return undefined;
    const nearest = (field.suggestions || [])
      .map((item) => ({ item, distance: Math.min(...[item.value, ...(item.aliases || [])]
        .map((option) => editDistance(candidateQuery, normalized(option)))) }))
      .sort((left, right) => left.distance - right.distance)[0];
    const limit = Math.max(1, Math.floor(candidateQuery.length * 0.25));
    return nearest && nearest.distance <= limit ? nearest.item : undefined;
  };
  const commit = (next: string) => {
    input.current?.setCustomValidity("");
    setInvalid(false);
    onChange(next);
    setOpen(false);
  };
  const finishTyping = (candidate: string, report = false) => {
    const match = field.autoCorrectSuggestions === false
      ? canonical(candidate)
      : closest(candidate);
    if (match) {
      commit(match.value);
      return true;
    }
    const isInvalid = Boolean(field.strictSuggestions && candidate);
    input.current?.setCustomValidity(isInvalid ? "Escolha uma das opções sugeridas." : "");
    setInvalid(isInvalid);
    if (isInvalid && report) input.current?.reportValidity();
    return false;
  };
  return (
    <div className="field-autocomplete">
      <input
        ref={input}
        id={id}
        name={name}
        type={field.type || "text"}
        value={value}
        required={field.required}
        pattern={field.pattern}
        maxLength={field.maxLength}
        disabled={field.disabled}
        autoComplete="off"
        role="combobox"
        aria-expanded={open}
        aria-autocomplete="list"
        aria-invalid={invalid}
        placeholder={field.placeholder || `${field.required ? "Obrigatório" : "Opcional"} — digite para buscar`}
        onFocus={() => setOpen(true)}
        onChange={(event) => {
          event.currentTarget.setCustomValidity("");
          setInvalid(false);
          onChange(event.target.value);
          setOpen(true);
        }}
        onBlur={(event) => {
          finishTyping(event.target.value);
          setOpen(false);
        }}
        onKeyDown={(event) => {
          if (event.key === "Enter") {
            event.preventDefault();
            finishTyping(event.currentTarget.value, true);
          }
          if (event.key === "Escape") setOpen(false);
        }}
      />
      {open && choices.length > 0 && (
        <div className="field-suggestions" role="listbox" aria-label={`Sugestões para ${field.label}`}>
          {choices.map((item) => (
            <button
              type="button"
              role="option"
              key={item.value}
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => commit(item.value)}
            >
              <span>{item.value}</span>
              {item.aliases?.length ? <small>{item.aliases.join(" · ")}</small> : null}
            </button>
          ))}
        </div>
      )}
      {invalid && <small className="field-validation" role="alert">Escolha uma opção válida da lista.</small>}
    </div>
  );
}
export const states =
  "AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO"
    .split(" ")
    .map((s) => [s, s] as [string, string]);
export const addressFields: Field[] = [
  {
    key: "cep",
    label: "CEP",
    required: true,
    autoComplete: "postal-code",
    placeholder: "00000000",
  },
  {
    key: "uf",
    label: "Estado",
    required: true,
    options: states,
    autoComplete: "address-level1",
  },
  {
    key: "cidade",
    label: "Cidade",
    required: true,
    autoComplete: "address-level2",
  },
  {
    key: "bairro",
    label: "Bairro",
    required: true,
    autoComplete: "address-level3",
  },
  {
    key: "rua",
    label: "Rua / avenida",
    required: true,
    autoComplete: "address-line1",
  },
  { key: "numero", label: "Número", autoComplete: "address-line2" },
  {
    key: "complemento",
    label: "Complemento",
    autoComplete: "address-line3",
    placeholder: "Apartamento, bloco…",
  },
];
export function Fields({
  fields,
  values,
  onChange,
  prefix = "",
  showRequirement = true,
}: {
  fields: Field[];
  values: Draft;
  onChange: (key: string, value: string) => void;
  prefix?: string;
  showRequirement?: boolean;
}) {
  const id = useId();
  return (
    <div className="page-form-grid">
      {fields.map((f) => {
        const valueKey = f.unprefixed ? f.key : `${prefix}${f.key}`;
        return (
        <label key={f.key} htmlFor={`${id}-${f.key}`}>
          {f.label}
          {showRequirement && f.required && (
            <span className="required-mark" aria-hidden="true">
              {" "}
              *
            </span>
          )}
          {f.options ? (
            <SiteSelect field={f} id={`${id}-${f.key}`} value={values[valueKey] || ""}
              showRequirement={showRequirement} onChange={(value) => onChange(valueKey, value)} />
          ) : f.suggestions ? (
            <SuggestedInput
              field={f}
              id={`${id}-${f.key}`}
              name={valueKey}
              value={values[valueKey] || ""}
              onChange={(value) => onChange(valueKey, value)}
            />
          ) : (
            <input
              id={`${id}-${f.key}`}
              name={valueKey}
              type={f.type || "text"}
              step={f.step ?? (f.type === "number" ? "any" : undefined)}
              min={f.min}
              pattern={f.pattern}
              maxLength={f.maxLength}
              placeholder={showRequirement
                ? `${f.required ? "Obrigatório" : "Opcional"}${f.placeholder ? ` — ${f.placeholder}` : ""}`
                : f.placeholder}
              value={values[valueKey] || ""}
              onChange={(e) => {
                const value = e.target.value;
                if (f.type === "number" && f.step === 1 && value && !/^\d+$/.test(value)) return;
                onChange(valueKey, value);
              }}
              onKeyDown={(e) => {
                if (f.type === "number" && ["e", "E", "+", "-", ...(f.step === 1 ? [".", ","] : [])].includes(e.key)) e.preventDefault();
              }}
              required={f.required}
              autoComplete={f.autoComplete}
              disabled={f.disabled}
            />
          )}
          {f.hint && <small className="field-note">{f.hint}</small>}
        </label>
        );
      })}
    </div>
  );
}
export function PasswordField({
  value,
  onChange,
  registration = false,
}: {
  value: string;
  onChange: (v: string) => void;
  registration?: boolean;
}) {
  const id = useId();
  const [show, setShow] = useState(false);
  return (
    <div className="page-password">
      <label htmlFor={id}>
        Senha {registration && <span className="required-mark">*</span>}
      </label>
      <div className="password-field">
        <input
          id={id}
          name="senha"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          type={show ? "text" : "password"}
          autoComplete={registration ? "new-password" : "current-password"}
          required
          placeholder="Digite sua senha"
        />
        <button
          type="button"
          aria-label={show ? "Ocultar senha" : "Mostrar senha"}
          onClick={() => setShow(!show)}
        >
          {show ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>
    </div>
  );
}
export function ProfileFields({
  values,
  onChange,
  address = true,
}: {
  values: Draft;
  onChange: (key: string, value: string) => void;
  address?: boolean;
}) {
  return (
    <>
      <Fields
        values={values}
        onChange={onChange}
        fields={[
          {
            key: "tipoPessoa",
            label: "Tipo de pessoa",
            required: true,
            options: [
              ["PF", "Pessoa física"],
              ["PJ", "Pessoa jurídica"],
            ],
          },
          {
            key: "nome",
            label:
              values.tipoPessoa === "PJ" ? "Nome fantasia" : "Nome completo",
            required: true,
            autoComplete: values.tipoPessoa === "PJ" ? "organization" : "name",
          },
          {
            key: "email",
            label: "E-mail",
            type: "email",
            required: true,
            autoComplete: "email",
          },
          {
            key: "telefone",
            label: "Telefone de contato",
            type: "tel",
            required: true,
            autoComplete: "tel",
            placeholder: "DDD + número",
          },
          ...(values.tipoPessoa === "PJ"
            ? [
                {
                  key: "cnpj",
                  label: "CNPJ",
                  required: true,
                  placeholder: "Somente números",
                },
                { key: "razaoSocial", label: "Razão social", required: true },
              ]
            : [
                {
                  key: "cpf",
                  label: "CPF",
                  required: true,
                  placeholder: "Somente números",
                },
                {
                  key: "dataNascimento",
                  label: "Data de nascimento",
                  type: "date",
                  required: true,
                  autoComplete: "bday",
                },
              ]),
        ]}
      />
      {address && (
        <section className="form-section">
          <div className="form-section-title">
            <span>02</span>
            <div>
              <h2>Seu endereço</h2>
              <p>
                Informações de cadastro. O endereço completo não aparece no
                anúncio público.
              </p>
            </div>
          </div>
          <AddressFields values={values} onChange={onChange} />
        </section>
      )}
    </>
  );
}

type CepStatus = "idle" | "loading" | "success" | "error";

export function AddressFields({
  values,
  onChange,
}: {
  values: Draft;
  onChange: (key: string, value: string) => void;
}) {
  const [status, setStatus] = useState<CepStatus>("idle");
  const [message, setMessage] = useState("");
  const onChangeRef = useRef(onChange);
  onChangeRef.current = onChange;
  const rawCep = values["endereco.cep"] || "";
  const cep = rawCep.replace(/\D/g, "");

  useEffect(() => {
    if (cep.length !== 8) {
      setStatus("idle");
      setMessage(
        cep.length > 0 ? "Digite os 8 números do CEP para buscar." : "",
      );
      return;
    }

    const controller = new AbortController();
    setStatus("loading");
    setMessage("Buscando endereço…");

    findAddressByCep(cep, controller.signal)
      .then((address) => {
        const update = onChangeRef.current;
        update("endereco.rua", address.logradouro || "");
        update("endereco.bairro", address.bairro || "");
        update("endereco.cidade", address.localidade || "");
        update("endereco.uf", address.uf || "");
        if (address.complemento) {
          update("endereco.complemento", address.complemento);
        }
        setStatus("success");
        setMessage("Endereço encontrado. Confira os dados e informe o número.");
      })
      .catch((error: unknown) => {
        if (axiosIsCancellation(error)) return;
        setStatus("error");
        setMessage(
          error instanceof Error && error.message === "CEP_NOT_FOUND"
            ? "CEP não encontrado. Confira o número ou preencha manualmente."
            : "Não foi possível consultar o CEP. Preencha o endereço manualmente.",
        );
      });

    return () => controller.abort();
  }, [cep]);

  return (
    <>
      <Fields
        fields={addressFields}
        values={values}
        onChange={onChange}
        prefix="endereco."
      />
      {message && (
        <p className={`cep-feedback ${status}`} role="status" aria-live="polite">
          {status === "loading" && <LoaderCircle size={16} aria-hidden="true" />}
          {status === "success" && <CheckCircle2 size={16} aria-hidden="true" />}
          {status === "error" && <CircleAlert size={16} aria-hidden="true" />}
          {message}
        </p>
      )}
    </>
  );
}

function axiosIsCancellation(error: unknown) {
  return (
    typeof error === "object" &&
    error !== null &&
    "code" in error &&
    error.code === "ERR_CANCELED"
  );
}
