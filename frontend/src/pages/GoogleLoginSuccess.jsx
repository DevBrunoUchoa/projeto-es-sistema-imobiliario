import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthVisual from '../components/AuthVisual';
import { useAuth } from '../contexts/AuthContext';

// Destino do redirect feito pelo OAuth2SuccessHandler depois do login com
// Google: os cookies de sessão (jwt/refresh_token) já foram setados pelo
// backend, essa página só precisa descobrir quem é o usuário autenticado.
export default function GoogleLoginSuccess() {
  const navigate = useNavigate();
  const { restoreSession } = useAuth();
  const [erro, setErro] = useState('');

  useEffect(() => {
    restoreSession()
      .then(() => navigate('/perfil', { replace: true }))
      .catch((err) => setErro(err.message || 'Não foi possível concluir o login com Google.'));
  }, []);

  return <div className="auth-screen">
    <AuthVisual />
    <div className="auth-panel"><div className="auth-form-box">
      <div style={{ textAlign: 'center' }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, padding: '20px 0' }}>
          {!erro ? (
            <>
              <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'rgba(123,105,248,.12)', border: '2px solid rgba(123,105,248,.3)', display: 'grid', placeItems: 'center', fontSize: 28, color: 'var(--primary)' }}>
                <i className="fa-solid fa-spinner fa-spin" />
              </div>
              <h2>Concluindo login com Google...</h2>
            </>
          ) : (
            <>
              <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'rgba(248,113,113,.12)', border: '2px solid rgba(248,113,113,.3)', display: 'grid', placeItems: 'center', fontSize: 28, color: '#f87171' }}>
                <i className="fa-solid fa-circle-xmark" />
              </div>
              <div>
                <h2>Não deu pra entrar com Google</h2>
                <p style={{ color: 'var(--text-2)', fontSize: 15, marginTop: 8, lineHeight: 1.6 }}>{erro}</p>
              </div>
              <button className="btn-full" onClick={() => navigate('/login', { replace: true })}>Ir para o login</button>
            </>
          )}
        </div>
      </div>
    </div></div>
  </div>;
}
