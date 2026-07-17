import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthVisual from '../components/AuthVisual';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';

export default function RecuperarSenha() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  async function sendReset(event) {
    event?.preventDefault();
    setError(''); setLoading(true);
    try {
      await authApi.esqueciSenha({ email: email.trim() });
      setSent(true);
    } catch (err) {
      setError(err.message || 'Não foi possível enviar o link de recuperação.');
    } finally { setLoading(false); }
  }

  return <div className="auth-screen">
    <AuthVisual />
    <div className="auth-panel"><div className="auth-form-box">
      {!sent ? <>
        <div>
          <h2>Recuperar senha</h2>
          <p className="auth-sub">Informe seu e-mail cadastrado</p>
        </div>
        <Link to="/login" style={{display:'inline-flex',alignItems:'center',gap:6,color:'var(--text-2)',fontSize:13,marginTop:-8}}>
          <i className="fa-solid fa-arrow-left"/> Voltar ao login
        </Link>
        <Alert>{error}</Alert>
        <form onSubmit={sendReset}><div style={{display:'flex',flexDirection:'column',gap:16}}>
          <div className="form-group">
            <label className="form-label" htmlFor="recEmail">E-mail cadastrado</label>
            <div className="input-icon-wrap">
              <i className="fa-solid fa-envelope input-icon"/>
              <input id="recEmail" type="email" className="form-input" placeholder="seu@email.com" required value={email} onChange={(e)=>setEmail(e.target.value)}/>
            </div>
            <span className="form-hint">Enviaremos o link de recuperação para este e-mail</span>
          </div>
          <button className="btn-full" disabled={loading}>
            <i className="fa-solid fa-paper-plane" style={{marginRight:8}}/>
            {loading ? 'Enviando...' : 'Enviar link de recuperação'}
          </button>
        </div></form>
        <p className="auth-footer">Lembrou a senha? <Link to="/login">Entrar</Link></p>
      </> : <div style={{textAlign:'center'}}>
        <div style={{display:'flex',flexDirection:'column',alignItems:'center',gap:20,padding:'20px 0'}}>
          <div style={{width:72,height:72,borderRadius:'50%',background:'rgba(123,105,248,.12)',border:'2px solid rgba(123,105,248,.3)',display:'grid',placeItems:'center',fontSize:28,color:'var(--primary)'}}>
            <i className="fa-solid fa-envelope-circle-check"/>
          </div>
          <div>
            <h2>E-mail enviado!</h2>
            <p style={{color:'var(--text-2)',fontSize:15,marginTop:8,lineHeight:1.6}}>
              Verifique sua caixa de entrada.<br/>
              O link expira em <strong style={{color:'var(--text-1)'}}>1 hora</strong>.
            </p>
          </div>
          <div className="alert alert-info" style={{textAlign:'left',width:'100%'}}>
            <i className="fa-solid fa-circle-info"/>
            <span>Não recebeu? Verifique o spam ou <button type="button" onClick={sendReset} style={{background:'none',border:'none',color:'var(--primary)',fontWeight:600,cursor:'pointer',padding:0,fontFamily:'var(--font-body)',fontSize:14}}>reenviar agora</button>.</span>
          </div>
          <Link to="/login" className="btn-full" style={{textDecoration:'none',display:'flex',alignItems:'center',justifyContent:'center',gap:8}}>
            Voltar ao login
          </Link>
        </div>
      </div>}
    </div></div>
  </div>;
}
