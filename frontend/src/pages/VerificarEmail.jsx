import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import AuthVisual from '../components/AuthVisual';
import { authApi } from '../api/authApi';

export default function VerificarEmail() {
  const { token } = useParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('carregando');
  const [erro, setErro] = useState('');

  useEffect(() => {
    authApi.verificarEmail(token)
      .then(() => setStatus('sucesso'))
      .catch((err) => { setErro(err.message || 'Não foi possível verificar seu e-mail.'); setStatus('erro'); });
  }, [token]);

  return <div className="auth-screen">
    <AuthVisual />
    <div className="auth-panel"><div className="auth-form-box">
      <div style={{ textAlign: 'center' }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, padding: '20px 0' }}>
          {status === 'carregando' && (
            <>
              <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'rgba(123,105,248,.12)', border: '2px solid rgba(123,105,248,.3)', display: 'grid', placeItems: 'center', fontSize: 28, color: 'var(--primary)' }}>
                <i className="fa-solid fa-spinner fa-spin" />
              </div>
              <h2>Verificando seu e-mail...</h2>
            </>
          )}
          {status === 'sucesso' && (
            <>
              <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'rgba(34,197,94,.12)', border: '2px solid rgba(34,197,94,.3)', display: 'grid', placeItems: 'center', fontSize: 28, color: '#22c55e' }}>
                <i className="fa-solid fa-circle-check" />
              </div>
              <div>
                <h2>E-mail verificado!</h2>
                <p style={{ color: 'var(--text-2)', fontSize: 15, marginTop: 8, lineHeight: 1.6 }}>Sua conta já está confirmada. Pode entrar normalmente.</p>
              </div>
              <button className="btn-full" onClick={() => navigate('/login', { replace: true })}>Ir para o login</button>
            </>
          )}
          {status === 'erro' && (
            <>
              <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'rgba(248,113,113,.12)', border: '2px solid rgba(248,113,113,.3)', display: 'grid', placeItems: 'center', fontSize: 28, color: '#f87171' }}>
                <i className="fa-solid fa-circle-xmark" />
              </div>
              <div>
                <h2>Não deu pra verificar</h2>
                <p style={{ color: 'var(--text-2)', fontSize: 15, marginTop: 8, lineHeight: 1.6 }}>{erro} O link pode ter expirado — tente fazer login e pedir um novo.</p>
              </div>
              <button className="btn-full" onClick={() => navigate('/login', { replace: true })}>Ir para o login</button>
            </>
          )}
        </div>
      </div>
    </div></div>
  </div>;
}
