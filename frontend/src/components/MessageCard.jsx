import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { anuncioApi } from '../api/anuncioApi';
import { userApi } from '../api/userApi';

const STATUS_LABELS = { ENVIADO: 'Enviado', LIDO: 'Lido', RESPONDIDO: 'Respondido' };

function formatarData(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' });
}

export default function MessageCard({ contato, mostrarAutor }) {
  const [anuncio, setAnuncio] = useState(null);
  const [autor, setAutor] = useState(null);

  useEffect(() => {
    anuncioApi.detalhes(contato.adId).then(setAnuncio).catch(() => {});
  }, [contato.adId]);

  useEffect(() => {
    if (!mostrarAutor) return;
    userApi.publico(contato.estudanteId).then(setAutor).catch(() => {});
  }, [mostrarAutor, contato.estudanteId]);

  return (
    <div className="card-section" style={{ marginBottom: 0 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
        <div style={{ flex: 1, minWidth: 200 }}>
          <h4 style={{ margin: '0 0 4px', fontSize: 14, fontWeight: 700, color: 'var(--text-1)' }}>{anuncio ? anuncio.titulo : 'Carregando anúncio...'}</h4>
          {mostrarAutor && autor && <p style={{ margin: '0 0 6px', fontSize: 12.5, color: 'var(--text-3)' }}>De: {autor.nome}</p>}
          <p style={{ margin: 0, fontSize: 13.5, color: 'var(--text-2)' }}>{contato.mensagem}</p>
        </div>
        <div style={{ textAlign: 'right', flexShrink: 0 }}>
          <span className={`chip ${contato.status === 'ENVIADO' ? 'chip-pending' : 'chip-approved'}`}>{STATUS_LABELS[contato.status] ?? contato.status}</span>
          <p style={{ margin: '6px 0 0', fontSize: 11.5, color: 'var(--text-3)' }}>{formatarData(contato.dataCriacao)}</p>
        </div>
      </div>
      <div style={{ display: 'flex', gap: 8, marginTop: 10, flexWrap: 'wrap' }}>
        <Link to={`/imoveis/${contato.adId}`} className="btn-sm btn-outline" style={{ textDecoration: 'none' }}>Ver anúncio</Link>
        {mostrarAutor && (
          <Link to={`/usuarios/${contato.estudanteId}`} className="btn-sm btn-outline" style={{ textDecoration: 'none' }}>
            <i className="fa-solid fa-user" style={{ marginRight: 6 }} />Ver perfil
          </Link>
        )}
      </div>
    </div>
  );
}
