import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { anuncioApi } from '../api/anuncioApi';
import { TIPO_OFERTA_LABELS, formatMoeda } from '../utils/anuncio';

export default function PropertyCard({ anuncio }) {
  const [capa, setCapa] = useState(null);

  useEffect(() => {
    let active = true;
    anuncioApi.imagens.listar(anuncio.id)
      .then((imagens) => {
        if (!active || !imagens?.length) return;
        const principal = imagens.find((imagem) => imagem.principal) ?? imagens[0];
        setCapa(principal.url);
      })
      .catch(() => {});
    return () => { active = false; };
  }, [anuncio.id]);

  const precoTotal = Number(anuncio.precoAluguel ?? 0) + Number(anuncio.precoCondominio ?? 0);
  const vagasLabel = anuncio.tipoOferta === 'VAGA_COMPARTILHADA'
    ? `${anuncio.vagasDisponiveis} de ${anuncio.vagasTotal} vagas`
    : null;

  return (
    <article className="prop-card">
      <div className="card-img-wrap">
        {capa
          ? <img className="card-img" src={capa} alt={anuncio.titulo} />
          : <div className="card-img card-img-placeholder"><i className="fa-solid fa-house" /></div>}
        <span className="badge-type">{TIPO_OFERTA_LABELS[anuncio.tipoOferta] ?? anuncio.tipoOferta}</span>
      </div>

      <div className="card-body">
        {vagasLabel && (
          <div className="card-meta">
            <span className="card-travel"><i className="fa-solid fa-users" /> {vagasLabel}</span>
          </div>
        )}

        <h3 className="card-title">{anuncio.titulo}</h3>
        {anuncio.descricao && <p className="card-addr">{anuncio.descricao}</p>}

        <div className="card-footer">
          <div className="card-price">
            <span className="price-val">R$ {formatMoeda(precoTotal)}</span>
            <span className="price-per">/mês</span>
          </div>
          <Link to={`/imoveis/${anuncio.id}`} className="btn-details">Ver detalhes <i className="fa-solid fa-arrow-right" /></Link>
        </div>
      </div>
    </article>
  );
}
