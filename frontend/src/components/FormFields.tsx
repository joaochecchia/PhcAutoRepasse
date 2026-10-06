import { useEffect, useId, useRef, useState } from "react";
import {
  CheckCircle2,
  CircleAlert,
  Eye,
  EyeOff,
  LoaderCircle,
} from "lucide-react";
import { findAddressByCep } from "../lib/viacep";

export type Draft = Record<string, string>;
export type Field = {
  key: string;
  label: string;
  type?: string;
  required?: boolean;
  options?: [string, string][];
  placeholder?: string;
  autoComplete?: string;
  hint?: string;
};
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
}: {
  fields: Field[];
  values: Draft;
  onChange: (key: string, value: string) => void;
  prefix?: string;
}) {
  const id = useId();
  return (
    <div className="page-form-grid">
      {fields.map((f) => (
        <label key={f.key} htmlFor={`${id}-${f.key}`}>
          {f.label}
          {f.required && (
            <span className="required-mark" aria-hidden="true">
              {" "}
              *
            </span>
          )}
          {f.options ? (
            <select
              id={`${id}-${f.key}`}
              name={`${prefix}${f.key}`}
              value={values[`${prefix}${f.key}`] || ""}
              onChange={(e) => onChange(`${prefix}${f.key}`, e.target.value)}
              required={f.required}
              autoComplete={f.autoComplete}
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
              id={`${id}-${f.key}`}
              name={`${prefix}${f.key}`}
              type={f.type || "text"}
              step={f.type === "number" ? "any" : undefined}
              placeholder={f.placeholder}
              value={values[`${prefix}${f.key}`] || ""}
              onChange={(e) => onChange(`${prefix}${f.key}`, e.target.value)}
              required={f.required}
              autoComplete={f.autoComplete}
            />
          )}
          {f.hint && <small className="field-note">{f.hint}</small>}
        </label>
      ))}
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
        Senha <span className="required-mark">*</span>
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
        ]}
      />
      <Fields
        values={values}
        onChange={onChange}
        fields={[
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
          <ProfileAddressFields values={values} onChange={onChange} />
        </section>
      )}
    </>
  );
}

type CepStatus = "idle" | "loading" | "success" | "error";

function ProfileAddressFields({
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
