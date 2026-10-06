import type { ReactNode } from "react";
import { ChevronRight, ArrowUpRight } from "lucide-react";

export function PageBreadcrumb({
  label,
  parent,
}: {
  label: string;
  parent?: { href: string; label: string };
}) {
  return (
    <nav className="breadcrumbs" aria-label="Caminho da página">
      <a href="#inicio">Início</a>
      <ChevronRight size={13} />
      {parent && (
        <>
          <a href={parent.href}>{parent.label}</a>
          <ChevronRight size={13} />
        </>
      )}
      <span aria-current="page">{label}</span>
    </nav>
  );
}
export function AccountLayout({
  title,
  eyebrow,
  description,
  children,
  wide = false,
}: {
  title: string;
  eyebrow: string;
  description: string;
  children: ReactNode;
  wide?: boolean;
}) {
  return (
    <section className="container dedicated-page">
      <PageBreadcrumb label={eyebrow} />
      <div className={`account-page-layout ${wide ? "account-page-wide" : ""}`}>
        <aside className="account-story">
          <img src="/images/hero.jpg" alt="" />
          <div className="account-story-content">
            <span className="eyebrow">PHC AUTO REPASSE</span>
            <h2>
              Um novo caminho.
              <br />O seu próximo
              <br />
              bom negócio.
            </h2>
            <p>
              Encontre oportunidades, anuncie seu veículo e faça parte dessa
              história.
            </p>
            <a href="#buscar">
              Explorar a vitrine
              <ArrowUpRight size={17} />
            </a>
          </div>
          <span className="account-story-foot">PAIXÃO POR VEÍCULOS.</span>
        </aside>
        <div className="account-page-form">
          <span className="eyebrow">{eyebrow}</span>
          <h1>{title}</h1>
          <p className="page-intro">{description}</p>
          {children}
        </div>
      </div>
    </section>
  );
}
