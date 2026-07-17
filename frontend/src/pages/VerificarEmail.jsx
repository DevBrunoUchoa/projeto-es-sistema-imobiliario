import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/authApi';

export default function VerificarEmail() {
  const [params] = useSearchParams();
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('Verificando seu e-mail...');

  useEffect(() => {
    const token = params.get('token');
    if (!token) {
      setStatus('error');
      setMessage('O link de verificação está incompleto.');
      return;
    }

    authApi.verificarEmail(token)
      .then((data) => {
        setStatus('success');
        setMessage(typeof data === 'string' ? data : 'E-mail verificado com sucesso!');
      })
      .catch((err) => {
        setStatus('error');
        setMessage(err.message || 'O link é inválido, expirou ou já foi utilizado.');
      });
  }, [params]);

  return (
    <div className="auth-screen auth-single-panel">
      <div className="auth-panel"><div className="auth-form-box auth-status-box">
        <div className={`status-icon ${status}`}>
          <i className={`fa-solid ${status === 'loading' ? 'fa-spinner fa-spin' : status === 'success' ? 'fa-check' : 'fa-triangle-exclamation'}`} />
        </div>
        <h2>{status === 'loading' ? 'Aguarde' : status === 'success' ? 'E-mail confirmado' : 'Não foi possível confirmar'}</h2>
        <p className="auth-sub">{message}</p>
        {status !== 'loading' && <Link className="btn-full" to="/login">Ir para o login</Link>}
      </div></div>
    </div>
  );
}
