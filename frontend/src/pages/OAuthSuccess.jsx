import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function OAuthSuccess() {
  const [error, setError] = useState('');
  const { completeOAuthLogin } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    completeOAuthLogin()
      .then(() => {
        if (active) navigate('/', { replace: true });
      })
      .catch((err) => {
        if (active) setError(err.message || 'Não foi possível concluir o login com Google.');
      });

    return () => { active = false; };
  }, [completeOAuthLogin, navigate]);

  return (
    <div className="auth-screen auth-single-panel">
      <div className="auth-panel"><div className="auth-form-box auth-status-box">
        <div className={`status-icon ${error ? 'error' : ''}`}>
          <i className={`fa-solid ${error ? 'fa-triangle-exclamation' : 'fa-spinner fa-spin'}`} />
        </div>
        <h2>{error ? 'Falha no login' : 'Concluindo seu login'}</h2>
        <p className="auth-sub">{error || 'Estamos validando sua conta Google com segurança.'}</p>
        {error && <button className="btn-full" onClick={() => navigate('/login', { replace: true })}>Voltar ao login</button>}
      </div></div>
    </div>
  );
}
