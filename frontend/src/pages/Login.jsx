import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import AuthVisual from '../components/AuthVisual';
import Alert from '../components/Alert';
import { useAuth } from '../contexts/AuthContext';

export default function Login() {
  const [form, setForm] = useState({ email: '', senha: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  async function handleSubmit(event) {
    event.preventDefault();
    setError(''); setLoading(true);
    try {
      await login({ email: form.email.trim(), senha: form.senha });
      navigate(location.state?.from || '/perfil', { replace: true });
    } catch (err) {
      setError(err.message || 'E-mail ou senha inválidos.');
    } finally { setLoading(false); }
  }

  return <div className="auth-screen">
    <AuthVisual />
    <div className="auth-panel"><div className="auth-form-box">
      <div><h2>Bem-vindo de volta</h2><p className="auth-sub">Entre com sua conta para continuar</p></div>
      <button className="btn-google" type="button" onClick={() => { window.location.href = '/oauth2/authorization/google'; }}>
        <i className="fa-brands fa-google"/> Continuar com Google
      </button>
      <div className="divider">ou</div>
      <Alert>{error}</Alert>
      <form onSubmit={handleSubmit}><div style={{display:'flex', flexDirection:'column', gap:16}}>
        <div className="form-group"><label className="form-label" htmlFor="email">E-mail</label><div className="input-icon-wrap"><i className="fa-solid fa-envelope input-icon"/><input id="email" className="form-input" type="email" required placeholder="seu@email.com" value={form.email} onChange={(e)=>setForm({...form,email:e.target.value})}/></div></div>
        <div className="form-group"><div style={{display:'flex',justifyContent:'space-between'}}><label className="form-label" htmlFor="senha">Senha</label><Link to="/recuperar-senha" style={{fontSize:13,color:'var(--text-2)'}}>Esqueci minha senha</Link></div><div className="input-icon-wrap"><i className="fa-solid fa-lock input-icon"/><input id="senha" className="form-input" type={showPassword?'text':'password'} required placeholder="••••••••" value={form.senha} onChange={(e)=>setForm({...form,senha:e.target.value})}/><button type="button" className="input-eye" onClick={()=>setShowPassword(!showPassword)}><i className={`fa-solid ${showPassword?'fa-eye-slash':'fa-eye'}`}/></button></div></div>
        <button className="btn-full" disabled={loading}>{loading?'Entrando...':'Entrar'}</button>
      </div></form>
      <p className="auth-footer">Não tem uma conta? <Link to="/cadastro">Cadastre-se grátis</Link></p>
    </div></div>
  </div>;
}
