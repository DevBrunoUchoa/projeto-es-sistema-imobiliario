import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import AuthVisual from '../components/AuthVisual';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';

export default function RedefinirSenha() {
  const [searchParams] = useSearchParams();
  const tokenFromLink = searchParams.get('token') || '';
  const [form, setForm] = useState({ token: tokenFromLink, novaSenha: '', confirmarSenha: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    if (form.novaSenha !== form.confirmarSenha) {
      setError('As senhas não coincidem.');
      return;
    }
    setLoading(true);
    try {
      await authApi.redefinirSenha({ token: form.token.trim(), novaSenha: form.novaSenha });
      setDone(true);
    } catch (err) {
      setError(err.message || 'Não foi possível redefinir a senha. O link pode ter expirado.');
    } finally { setLoading(false); }
  }

  return <div className="auth-screen">
    <AuthVisual />
    <div className="auth-panel"><div className="auth-form-box">
      {!done ? <>
        <div>
          <h2>Redefinir senha</h2>
          <p className="auth-sub">Informe o código recebido por e-mail e escolha uma nova senha</p>
        </div>
        <Link to="/login" style={{display:'inline-flex',alignItems:'center',gap:6,color:'var(--text-2)',fontSize:13,marginTop:-8}}>
          <i className="fa-solid fa-arrow-left"/> Voltar ao login
        </Link>
        <Alert>{error}</Alert>
        <form onSubmit={handleSubmit}><div style={{display:'flex',flexDirection:'column',gap:16}}>
          <div className="form-group">
            <label className="form-label" htmlFor="token">Código de recuperação</label>
            <div className="input-icon-wrap">
              <i className="fa-solid fa-key input-icon"/>
              <input id="token" className="form-input" required placeholder="Cole aqui o código recebido" value={form.token} onChange={(e)=>setForm({...form,token:e.target.value})}/>
            </div>
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="novaSenha">Nova senha</label>
            <div className="input-icon-wrap">
              <i className="fa-solid fa-lock input-icon"/>
              <input id="novaSenha" type={showPassword?'text':'password'} className="form-input" required minLength={6} placeholder="••••••••" value={form.novaSenha} onChange={(e)=>setForm({...form,novaSenha:e.target.value})}/>
              <button type="button" className="input-eye" onClick={()=>setShowPassword(!showPassword)}><i className={`fa-solid ${showPassword?'fa-eye-slash':'fa-eye'}`}/></button>
            </div>
            <span className="form-hint">Mínimo de 6 caracteres</span>
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="confirmarSenha">Confirmar nova senha</label>
            <div className="input-icon-wrap">
              <i className="fa-solid fa-lock input-icon"/>
              <input id="confirmarSenha" type={showPassword?'text':'password'} className="form-input" required minLength={6} placeholder="••••••••" value={form.confirmarSenha} onChange={(e)=>setForm({...form,confirmarSenha:e.target.value})}/>
            </div>
          </div>
          <button className="btn-full" disabled={loading}>{loading?'Salvando...':'Redefinir senha'}</button>
        </div></form>
      </> : <div style={{textAlign:'center'}}>
        <div style={{display:'flex',flexDirection:'column',alignItems:'center',gap:20,padding:'20px 0'}}>
          <div style={{width:72,height:72,borderRadius:'50%',background:'rgba(123,105,248,.12)',border:'2px solid rgba(123,105,248,.3)',display:'grid',placeItems:'center',fontSize:28,color:'var(--primary)'}}>
            <i className="fa-solid fa-circle-check"/>
          </div>
          <div>
            <h2>Senha redefinida!</h2>
            <p style={{color:'var(--text-2)',fontSize:15,marginTop:8,lineHeight:1.6}}>Já pode entrar com a nova senha.</p>
          </div>
          <button className="btn-full" onClick={()=>navigate('/login', {replace:true})}>Ir para o login</button>
        </div>
      </div>}
    </div></div>
  </div>;
}
