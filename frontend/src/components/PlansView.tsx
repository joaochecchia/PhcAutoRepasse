import { useState } from "react";
import { Check, Megaphone, Store } from "lucide-react";
import { Modal } from "./Modal";

export function PlansView({ onClose }: { onClose: () => void }) {
  const [interest, setInterest] = useState(false);
  return (
    <Modal title="Mais espaço para os seus negócios." onClose={onClose} wide>
      <p className="muted">Planos PHC Auto para quem quer anunciar.</p>
      <div className="plan-preview">
        <div className="dialog-emblem">
          <Store size={30} />
        </div>
        <span className="eyebrow">PARA VOCÊ E SUA LOJA</span>
        <h3>Seu estoque em uma nova vitrine.</h3>
        <p>
          Os planos, valores e benefícios ainda estão sendo definidos. Você
          poderá comparar as opções e assinar por aqui.
        </p>
        <div className="plan-points">
          <span>
            <Check size={17} />
            Informações claras sobre cada plano
          </span>
          <span>
            <Check size={17} />
            Contratação em um só lugar
          </span>
        </div>
        <button className="button primary" onClick={() => setInterest(true)}>
          <Megaphone size={17} />
          Quero conhecer os planos
        </button>
        {interest && (
          <p role="status" className="inline-notice">
            Em breve você encontrará as opções nesta página. Nenhuma assinatura
            ou cobrança foi realizada.
          </p>
        )}
      </div>
    </Modal>
  );
}
