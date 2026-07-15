import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthVisual from '../components/AuthVisual';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';

const roles = [
  { key:'ESTUDANTE', icon:'fa-graduation-cap', title:'Estudante', text:'Busco moradia próxima à universidade' },
  { key:'LOCADOR', icon:'fa-house', title:'Locador', text:'Tenho imóvel e quero anunciar' },
];

export default function Cadastro() {
  const [step, setStep] = useState(1);
  const [role, setRole] = useState('ESTUDANTE');
  const [form, setForm] = useState({ nome:'', sobrenome:'', email:'', senha:'', confirmarSenha:'', aceiteLgpd:false });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault(); setError('');
    if (!form.nome.trim() || !form.sobrenome.trim()) return setError('Preencha nome e sobrenome.');
    if (form.senha.length < 6) return setError('A senha deve ter no mínimo 6 caracteres.');
    if (form.senha !== form.confirmarSenha) return setError('As senhas não coincidem.');
    if (!form.aceiteLgpd) return setError('É preciso aceitar os termos e a política de privacidade.');
    setLoading(true);
    try {
      await authApi.cadastrar({ nome:`${form.nome.trim()} ${form.sobrenome.trim()}`, email:form.email.trim(), senha:form.senha, aceiteLgpd:true, role });
      setStep(3);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  }

  return <div className="auth-screen"><AuthVisual cadastro/><div className="auth-panel"><div className="auth-form-box" style={{maxWidth:480}}>
    <div className="step-progress">{[1,2,3].map((n)=><div key={n} className={`step-item ${step===n?'active':''} ${step>n?'done':''}`}><div className="step-dot">{n===3?<i className="fa-solid fa-check"/>:n}</div><span className="step-label">{n===1?'Tipo de conta':n===2?'Seus dados':'Concluído'}</span></div>)}</div>
    {step===1 && <div style={{display:'flex',flexDirection:'column'}}><h2>Criar conta</h2><p className="auth-sub">Qual é o seu perfil?</p><div className="type-cards" style={{marginTop:4}}>{roles.map((item)=><button type="button" key={item.key} className={`type-card ${role===item.key?'selected':''}`} onClick={()=>setRole(item.key)}><div className="type-card-icon"><i className={`fa-solid ${item.icon}`}/></div><strong>{item.title}</strong><span>{item.text}</span></button>)}</div><button className="btn-full" style={{marginTop:24}} onClick={()=>setStep(2)}>Continuar <i className="fa-solid fa-arrow-right"/></button><p className="auth-footer">Já tem conta? <Link to="/login">Entrar</Link></p></div>}
    {step===2 && <form onSubmit={submit} style={{display:'flex',flexDirection:'column',gap:18}}><div><h2>Seus dados</h2><p className="auth-sub">Preencha as informações abaixo</p></div><button type="button" onClick={()=>setStep(1)} style={{background:'none',border:0,textAlign:'left',cursor:'pointer',color:'var(--text-2)'}}><i className="fa-solid fa-arrow-left"/> Voltar</button><Alert>{error}</Alert><div className="form-grid"><div className="form-group"><label className="form-label">Nome</label><input className="form-input" required value={form.nome} onChange={(e)=>setForm({...form,nome:e.target.value})}/></div><div className="form-group"><label className="form-label">Sobrenome</label><input className="form-input" required value={form.sobrenome} onChange={(e)=>setForm({...form,sobrenome:e.target.value})}/></div></div><div className="form-group"><label className="form-label">E-mail</label><input className="form-input" type="email" required value={form.email} onChange={(e)=>setForm({...form,email:e.target.value})}/></div><div className="form-group"><label className="form-label">Senha</label><div className="input-icon-wrap"><i className="fa-solid fa-lock input-icon"/><input className="form-input" type={showPassword?'text':'password'} required value={form.senha} onChange={(e)=>setForm({...form,senha:e.target.value})}/><button className="input-eye" type="button" onClick={()=>setShowPassword(!showPassword)}><i className={`fa-solid ${showPassword?'fa-eye-slash':'fa-eye'}`}/></button></div></div><div className="form-group"><label className="form-label">Confirmar senha</label><input className="form-input" type="password" required value={form.confirmarSenha} onChange={(e)=>setForm({...form,confirmarSenha:e.target.value})}/></div><label className="fp-check-item"><input type="checkbox" checked={form.aceiteLgpd} onChange={(e)=>setForm({...form,aceiteLgpd:e.target.checked})}/><span className="check-box"/><span>Aceito os Termos de Uso e a Política de Privacidade</span></label><button className="btn-full" disabled={loading}>{loading?'Criando conta...':'Criar conta'}</button></form>}
    {step===3 && <div style={{textAlign:'center',display:'flex',flexDirection:'column',gap:20,alignItems:'center'}}><div style={{fontSize:48,color:'#22c55e'}}><i className="fa-solid fa-circle-check"/></div><div><h2>Conta criada!</h2><p className="auth-sub">Confirme o e-mail e entre com seus dados.</p></div><Link to="/login" className="btn-full" style={{textDecoration:'none'}}>Ir para o login</Link></div>}
  </div></div></div>;
}
