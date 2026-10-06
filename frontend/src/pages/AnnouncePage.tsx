import { useEffect, useRef, useState } from "react";
import {
  ArrowLeft,
  ArrowRight,
  Camera,
  CarFront,
  Check,
  ChevronDown,
  ChevronUp,
  Plus,
  Trash2,
} from "lucide-react";
import {
  AddressFields,
  addressFields,
  Fields,
  type Draft,
} from "../components/FormFields";
import {
  commonVehicleFields,
  motorFields,
  vehicleFields,
} from "../data/announcementFields";
import { types } from "../data/catalog";
import { PageBreadcrumb } from "./PageLayout";
import {
  adPayloadFromDraft,
  apiErrorMessage,
  createAd,
  uploadAdPhoto,
} from "../lib/backend";

type Photo = { id: string; file: File; alt: string };
function PhotoPreview({ file }: { file: File }) {
  const [url, setUrl] = useState("");
  useEffect(() => {
    const next = URL.createObjectURL(file);
    setUrl(next);
    return () => URL.revokeObjectURL(next);
  }, [file]);
  return <img src={url || undefined} alt="Prévia local da foto selecionada" />;
}
const stepLabels = ["Veículo", "Localização", "Fotos e anúncio", "Conferência"];
export function AnnouncePage({
  profile,
  usuarioId,
  onCreated,
}: {
  profile: Draft;
  usuarioId: string;
  onCreated: (anuncioId: string) => void;
}) {
  const [step, setStep] = useState(0);
  const [values, setValues] = useState<Draft>({
    tipoVeiculo: "CARRO",
    tipoPreco: "FIXO",
  });
  const [sameAddress, setSameAddress] = useState(false);
  const [motors, setMotors] = useState<Draft[]>([]);
  const [photos, setPhotos] = useState<Photo[]>([]);
  const [photoError, setPhotoError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const stepHeading = useRef<HTMLHeadingElement>(null);
  const initialStep = useRef(true);
  useEffect(() => {
    if (initialStep.current) {
      initialStep.current = false;
      return;
    }
    stepHeading.current?.focus();
    stepHeading.current?.scrollIntoView({
      block: "start",
      behavior: "instant",
    });
  }, [step]);
  const change = (key: string, value: string) =>
    setValues((prev) => ({ ...prev, [key]: value }));
  const specific = vehicleFields[values.tipoVeiculo];
  function copyAddress(checked: boolean) {
    setSameAddress(checked);
    if (checked)
      setValues((prev) => ({
        ...prev,
        ...Object.fromEntries(
          addressFields.map((f) => [
            `endereco.${f.key}`,
            profile[`endereco.${f.key}`] || "",
          ]),
        ),
      }));
  }
  function movePhoto(index: number, direction: number) {
    setPhotos((prev) => {
      const next = [...prev];
      [next[index], next[index + direction]] = [
        next[index + direction],
        next[index],
      ];
      return next;
    });
  }
  return (
    <section className="container dedicated-page">
      <PageBreadcrumb
        label="Seu veículo"
        parent={{ href: "#anunciar/dados", label: "Anunciar" }}
      />
      <div className="page-title-row">
        <div>
          <span className="eyebrow">SEU VEÍCULO EM UMA NOVA VITRINE</span>
          <h1>Vamos preparar seu anúncio.</h1>
          <p className="page-intro">
            Preencha os detalhes que ajudam alguém a encontrar o próximo
            veículo.
          </p>
        </div>
        <a href="#anunciar/dados" className="button secondary">
          Revisar meus dados
          <ArrowRight size={16} />
        </a>
      </div>
      <nav className="workflow-trail" aria-label="Etapas do anúncio">
        {stepLabels.map((label, i) => (
          <button
            type="button"
            key={label}
            className={step === i ? "active" : ""}
            aria-current={step === i ? "step" : undefined}
            disabled={i > step}
            onClick={() => setStep(i)}
          >
            {i < step ? <Check size={14} /> : `0${i + 1}`} · {label}
          </button>
        ))}
      </nav>
      <div className="editor-layout">
        <form
          className="page-form page-panel"
          onSubmit={async (e) => {
            e.preventDefault();
            if (step < 3) {
              setStep(step + 1);
              return;
            }
            setSubmitting(true);
            setSubmitError("");
            try {
              const anuncio = await createAd(
                adPayloadFromDraft(values, motors, usuarioId),
              );
              for (const [index, photo] of photos.entries()) {
                await uploadAdPhoto(anuncio.id, photo.file, index, photo.alt);
              }
              onCreated(anuncio.id);
            } catch (requestError) {
              setSubmitError(
                apiErrorMessage(requestError, "Não foi possível criar o anúncio."),
              );
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <div className="form-section-title">
            <span>
              <CarFront size={19} />
            </span>
            <div>
              <h2 ref={stepHeading} tabIndex={-1}>
                {
                  [
                    "Qual veículo você quer anunciar?",
                    "Onde está o veículo?",
                    "Faça seu veículo ser bem apresentado.",
                    "Confira antes do próximo passo.",
                  ][step]
                }
              </h2>
              <p>
                {step === 3
                  ? "Esta é uma conferência local, sem publicação."
                  : "Campos com * são necessários no contrato de cadastro."}
              </p>
            </div>
          </div>
          {step === 0 && (
            <>
              <Fields
                fields={[
                  {
                    key: "tipoVeiculo",
                    label: "Tipo de veículo",
                    required: true,
                    options: types.map(([v, l]) => [v, l]),
                  },
                ]}
                values={values}
                onChange={change}
              />
              <Fields
                fields={commonVehicleFields}
                values={values}
                onChange={change}
              />
              <section className="form-section">
                <div className="form-section-title">
                  <span>+</span>
                  <div>
                    <h2>Ficha técnica</h2>
                    <p>
                      Informações específicas para{" "}
                      {types
                        .find((t) => t[0] === values.tipoVeiculo)?.[1]
                        .toLowerCase()}
                      .
                    </p>
                  </div>
                </div>
                <Fields
                  fields={specific}
                  prefix={`${values.tipoVeiculo}.`}
                  values={values}
                  onChange={change}
                />
              </section>
              {values.tipoVeiculo === "BARCO" && (
                <section className="form-section">
                  <h2>Motores</h2>
                  <p className="field-note">
                    Se houver motores, informe cada um separadamente.
                  </p>
                  {motors.map((motor, i) => (
                    <div className="motor-editor" key={i}>
                      <div className="section-heading">
                        <h3>Motor {i + 1}</h3>
                        <button
                          type="button"
                          className="text-button"
                          onClick={() =>
                            setMotors((prev) => prev.filter((_, n) => n !== i))
                          }
                        >
                          Remover motor {i + 1}
                        </button>
                      </div>
                      <Fields
                        fields={motorFields}
                        values={motor}
                        onChange={(k, v) =>
                          setMotors((prev) =>
                            prev.map((m, n) =>
                              n === i ? { ...m, [k]: v } : m,
                            ),
                          )
                        }
                      />
                    </div>
                  ))}
                  <button
                    type="button"
                    className="button secondary"
                    onClick={() => setMotors([...motors, {}])}
                  >
                    <Plus size={16} />
                    Adicionar motor
                  </button>
                </section>
              )}
            </>
          )}
          {step === 1 && (
            <>
              <label className="check-label">
                <input
                  type="checkbox"
                  checked={sameAddress}
                  onChange={(e) => copyAddress(e.target.checked)}
                />
                O veículo está no endereço que informei na etapa anterior
              </label>
              {sameAddress && (
                <p className="inline-notice">
                  Os dados disponíveis foram copiados para este anúncio. Confira
                  e complete os campos abaixo.
                </p>
              )}
              <AddressFields values={values} onChange={change} />
              <p className="preview-caption">
                O endereço pertence a este anúncio. A localização pública será
                somente a cidade; rua, número e complemento não aparecem na
                vitrine.
              </p>
            </>
          )}
          {step === 2 && (
            <>
              <Fields
                fields={[
                  {
                    key: "titulo",
                    label: "Título do anúncio",
                    required: true,
                    placeholder: "Ex.: Toyota Corolla XEi 2.0",
                  },
                  {
                    key: "tipoPreco",
                    label: "Tipo de preço",
                    required: true,
                    options: [
                      ["FIXO", "Preço definido"],
                      ["SOB_CONSULTA", "Sob consulta"],
                    ],
                  },
                  ...(values.tipoPreco === "FIXO"
                    ? [
                        {
                          key: "precoReais",
                          label: "Preço (R$)",
                          type: "number",
                          required: true,
                        },
                      ]
                    : []),
                  {
                    key: "aceitaTroca",
                    label: "Aceita troca?",
                    options: [
                      ["true", "Sim"],
                      ["false", "Não"],
                    ],
                  },
                ]}
                values={values}
                onChange={change}
              />
              <label>
                Descrição
                <textarea
                  name="descricao"
                  rows={5}
                  value={values.descricao || ""}
                  onChange={(e) => change("descricao", e.target.value)}
                  placeholder="Conte sobre o estado de conservação, histórico de manutenção e outros detalhes do veículo."
                />
              </label>
              <div className="form-section">
                <h2>Fotos do veículo</h2>
                <p className="field-note">
                  Selecione até 8 imagens. Elas serão enviadas somente ao criar
                  o anúncio na última etapa.
                </p>
                <label className="photo-picker">
                  <Camera size={28} />
                  <span>Selecionar fotos</span>
                  <small>JPEG ou PNG · até 8 fotos</small>
                  <input
                    type="file"
                    accept="image/jpeg,image/png"
                    multiple
                    onChange={(e) => {
                      const files = Array.from(e.target.files || []);
                      const supported = files.filter((f) =>
                        ["image/jpeg", "image/png"].includes(
                          f.type,
                        ),
                      );
                      const available = Math.max(0, 8 - photos.length);
                      const selected = supported.slice(0, available);
                      setPhotoError(
                        supported.length > available
                          ? "Cada anúncio aceita no máximo 8 fotos."
                          : supported.length !== files.length
                            ? "Formato não aceito. Selecione imagens JPEG ou PNG."
                            : "",
                      );
                      setPhotos((prev) => [
                        ...prev,
                        ...selected.map((file) => ({
                          id: crypto.randomUUID(),
                          file,
                          alt: "",
                        })),
                      ]);
                      e.target.value = "";
                    }}
                  />
                </label>
                {photoError && (
                  <p className="inline-notice" role="alert">
                    {photoError}
                  </p>
                )}
                <div className="photo-editor-list">
                  {photos.map((photo, i) => (
                    <div className="photo-editor" key={photo.id}>
                      <PhotoPreview file={photo.file} />
                      <div>
                        <label>
                          Descrição da foto {i + 1}
                          <input
                            value={photo.alt}
                            onChange={(e) =>
                              setPhotos((prev) =>
                                prev.map((p) =>
                                  p.id === photo.id
                                    ? { ...p, alt: e.target.value }
                                    : p,
                                ),
                              )
                            }
                            placeholder="Ex.: Vista lateral do veículo"
                          />
                        </label>
                        <div className="photo-editor-actions">
                          <span>Posição {i + 1}</span>
                          <button
                            type="button"
                            className="icon-button"
                            aria-label={`Mover foto ${i + 1} para cima`}
                            disabled={i === 0}
                            onClick={() => movePhoto(i, -1)}
                          >
                            <ChevronUp size={16} />
                          </button>
                          <button
                            type="button"
                            className="icon-button"
                            aria-label={`Mover foto ${i + 1} para baixo`}
                            disabled={i === photos.length - 1}
                            onClick={() => movePhoto(i, 1)}
                          >
                            <ChevronDown size={16} />
                          </button>
                          <button
                            type="button"
                            className="icon-button"
                            aria-label={`Remover foto ${i + 1}`}
                            onClick={() =>
                              setPhotos((prev) =>
                                prev.filter((p) => p.id !== photo.id),
                              )
                            }
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}
          {step === 3 && (
            <>
              <div className="review-summary">
                <span className="eyebrow">PRÉVIA DO SEU ANÚNCIO</span>
                <h3>
                  {values.titulo || `${values.fabricante} ${values.modelo}`}
                </h3>
                <p>{values.versao || "Versão não informada"}</p>
                <dl>
                  <div>
                    <dt>Veículo</dt>
                    <dd>
                      {values.fabricante} {values.modelo}
                    </dd>
                  </div>
                  <div>
                    <dt>Ano</dt>
                    <dd>
                      {values.anoFabricacao} / {values.anoModelo}
                    </dd>
                  </div>
                  <div>
                    <dt>Cidade</dt>
                    <dd>
                      {values["endereco.cidade"]}, {values["endereco.uf"]}
                    </dd>
                  </div>
                  <div>
                    <dt>Preço informado</dt>
                    <dd>
                      {values.tipoPreco === "SOB_CONSULTA"
                        ? "Sob consulta"
                        : `R$ ${values.precoReais || "—"}`}
                    </dd>
                  </div>
                  <div>
                    <dt>Fotos selecionadas</dt>
                    <dd>{photos.length}</dd>
                  </div>
                </dl>
              </div>
              <details className="review-details">
                <summary>Conferir ficha técnica</summary>
                <dl>
                  {specific.map((f) => (
                    <div key={f.key}>
                      <dt>{f.label}</dt>
                      <dd>
                        {f.options?.find(
                          ([v]) =>
                            v === values[`${values.tipoVeiculo}.${f.key}`],
                        )?.[1] ||
                          values[`${values.tipoVeiculo}.${f.key}`] ||
                          "Não informado"}
                      </dd>
                    </div>
                  ))}
                </dl>
              </details>
              {values.tipoVeiculo === "BARCO" && (
                <p className="field-note">
                  {motors.length} motor(es) informado(s). Volte à etapa Veículo
                  para revisar cada motor.
                </p>
              )}
              {values.descricao && (
                <div className="review-description">
                  <h3>Descrição</h3>
                  <p>{values.descricao}</p>
                </div>
              )}
              <Fields
                fields={[
                  {
                    key: "publicarAgora",
                    label: "Ao enviar o anúncio, você deseja:",
                    options: [
                      ["false", "Salvar como rascunho"],
                      ["true", "Publicar agora"],
                    ],
                  },
                ]}
                values={values}
                onChange={change}
              />
              <p className="inline-notice">
                Ao concluir, os dados serão validados pelo servidor e as fotos
                serão enviadas na ordem apresentada.
              </p>
            </>
          )}
          <div className="page-form-actions">
            {step > 0 ? (
              <button
                className="button secondary"
                type="button"
                onClick={() => setStep(step - 1)}
              >
                <ArrowLeft size={16} />
                Voltar
              </button>
            ) : (
              <a href="#anunciar/dados" className="button secondary">
                Meus dados
              </a>
            )}
            <button className="button primary" type="submit" disabled={submitting}>
              {submitting
                ? "Enviando anúncio…"
                : step === 3
                  ? "Criar anúncio"
                  : "Continuar"}
              <ArrowRight size={17} />
            </button>
          </div>
          {submitError && (
            <p className="inline-notice error-notice" role="alert">{submitError}</p>
          )}
        </form>
        <aside className="editor-aside">
          <div className="page-panel">
            <span className="eyebrow">UM BOM ANÚNCIO COMEÇA NOS DETALHES</span>
            <h2>Mostre o que faz seu veículo ser único.</h2>
            <ul className="editor-tips">
              <li>Descreva o veículo com informações claras.</li>
              <li>Confira ano, versão e características.</li>
              <li>Prefira fotos bem iluminadas de diferentes ângulos.</li>
              <li>Informe a cidade onde o veículo se encontra.</li>
            </ul>
          </div>
          <div className="page-panel preview-panel">
            <h3>Dados protegidos pela sua sessão.</h3>
            <p>
              O servidor valida o perfil, os dados técnicos, a propriedade do
              anúncio e a publicação.
            </p>
            <a href="#anunciar/dados" className="text-button">
              Revisar dados do anunciante
              <ArrowRight size={15} />
            </a>
          </div>
        </aside>
      </div>
    </section>
  );
}
