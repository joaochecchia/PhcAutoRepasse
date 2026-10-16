import http from 'k6/http';
import { check, sleep } from 'k6';
export const options = {
  vus: Number(__ENV.VUS || 5), duration: __ENV.DURATION || '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{etapa:marcas}': ['p(95)<50'],
    'http_req_duration{etapa:modelos}': ['p(95)<100'],
    'http_req_duration{etapa:anos}': ['p(95)<100'],
    'http_req_duration{etapa:preco}': ['p(95)<150'],
  },
};
const base = __ENV.BASE_URL || 'http://localhost:8080';
const headers = { Authorization: `Bearer ${__ENV.TOKEN}`, 'Content-Type': 'application/json' };
export function setup() {
  for (const name of ['TOKEN','REFERENCIA','MARCA','MODELO','ANO','MES']) {
    if (!__ENV[name]) throw new Error(`Defina ${name} para um catálogo de homologação previamente carregado`);
  }
}
export default function () {
  const tipo = __ENV.TIPO || 'cars';
  const query = `tipo=${tipo}&referencia=${__ENV.REFERENCIA}&tamanho=50`;
  for (const [etapa, extra] of [['marcas',''],['modelos',`&marcaId=${__ENV.MARCA}`],['anos',`&marcaId=${__ENV.MARCA}&modeloId=${__ENV.MODELO}`]]) {
    const r = http.get(`${base}/api/v1/fipe/catalogo/opcoes?${query}${extra}`, { headers, tags: { etapa } });
    check(r, { 'catálogo disponível e preenchido': r => r.status === 200 && r.json('opcoes').length > 0 });
  }
  const r = http.post(`${base}/api/v1/fipe/consultas`, JSON.stringify({tipo, marcaId:__ENV.MARCA, modeloId:__ENV.MODELO,
    anoId:__ENV.ANO, referencia:Number(__ENV.REFERENCIA),mesReferencia:__ENV.MES}), {headers,tags:{etapa:'preco'}});
  check(r, {'preço local válido': r => r.status === 200 && r.json('valorCentavos') > 0});
  sleep(Number(__ENV.PAUSE || 2));
}
