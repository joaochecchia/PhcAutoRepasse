import type { Dispatch, SetStateAction } from "react";
import { ArrowRight, UserRound, CarFront, ShieldCheck } from "lucide-react";
import { ProfileFields, type Draft } from "../components/FormFields";
import { PageBreadcrumb } from "./PageLayout";

export function ProfilePage({
  values,
  setValues,
}: {
  values: Draft;
  setValues: Dispatch<SetStateAction<Draft>>;
}) {
  return (
    <section className="container dedicated-page">
      <PageBreadcrumb label="Dados do anunciante" />
      <div className="page-title-row">
        <div>
          <span className="eyebrow">VAMOS PREPARAR SEU ANÚNCIO</span>
          <h1>Primeiro, vamos conhecer você.</h1>
          <p className="page-intro">
            Confira os dados do anunciante e depois preencha as informações do
            veículo.
          </p>
        </div>
        <a href="#entrar" className="button secondary">
          <UserRound size={17} />
          Já tenho uma conta
        </a>
      </div>
      <div className="workflow-trail">
        <span className="active">01 · Seus dados</span>
        <span>02 · Seu veículo</span>
        <span>03 · Revisão do anúncio</span>
      </div>
      <div className="editor-layout">
        <form
          className="page-form page-panel"
          onSubmit={(e) => {
            e.preventDefault();
            window.location.hash = "anunciar/veiculo";
          }}
        >
          <div className="form-section-title">
            <span>
              <UserRound size={17} />
            </span>
            <div>
              <h2>Dados do anunciante</h2>
              <p>
                Prévia sem login: preencha os campos para experimentar o fluxo.
                * Campos necessários no cadastro.
              </p>
            </div>
          </div>
          <ProfileFields
            values={values}
            onChange={(key, value) =>
              setValues((current) => ({ ...current, [key]: value }))
            }
          />
          <div className="page-form-actions">
            <a href="#inicio" className="button secondary">
              Voltar ao início
            </a>
            <button className="button primary">
              Continuar para o veículo
              <ArrowRight size={17} />
            </button>
          </div>
        </form>
        <aside className="editor-aside">
          <div className="page-panel">
            <span className="dialog-emblem">
              <ShieldCheck size={27} />
            </span>
            <h2>Cada informação no seu lugar.</h2>
            <p>
              Seus documentos e endereço completo fazem parte do cadastro. A
              vitrine pública mostra apenas a cidade do veículo.
            </p>
            <p>
              Quando sua conta estiver conectada, esta etapa poderá trazer os
              dados já cadastrados para conferência.
            </p>
            <a className="text-button" href="#cadastro">
              Ainda não tem conta? Cadastre-se
              <ArrowRight size={15} />
            </a>
          </div>
          <div className="page-panel preview-panel">
            <CarFront size={24} />
            <h3>Só quer conhecer a tela?</h3>
            <p>
              Você também pode explorar o formulário do veículo sem preencher
              dados pessoais nesta demonstração.
            </p>
            <a className="text-button" href="#anunciar/veiculo">
              Ver formulário do veículo
              <ArrowRight size={15} />
            </a>
          </div>
        </aside>
      </div>
    </section>
  );
}
