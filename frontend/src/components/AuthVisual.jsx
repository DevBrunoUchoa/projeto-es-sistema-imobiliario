import { Link } from 'react-router-dom';

export default function AuthVisual({ cadastro = false }) {
  return (
    <div className="auth-visual">
      <div className="auth-visual-blobs"><div className="blob b1"/><div className="blob b2"/><div className="blob b3"/></div>
      <div className="auth-visual-content">
        <Link to="/" className="auth-visual-brand"><div className="logo-mark">C</div><span className="logo-text">Campus<strong>Living</strong></span></Link>
        <h2 className="auth-visual-tagline">
          {cadastro ? <>Junte-se à plataforma de <em>moradia</em> estudantil</> : <>Sua nova <em>moradia</em><br/>está aqui</>}
        </h2>
        <p className="auth-visual-sub">Conectamos estudantes a imóveis próximos à universidade com segurança, transparência e sem burocracia.</p>
      </div>
      <div className="auth-visual-stats">
        <div className="avs-item"><span className="avs-n">Grátis</span><span className="avs-l">Para começar</span></div>
        <div className="avs-item"><span className="avs-n">100%</span><span className="avs-l">Digital</span></div>
        <div className="avs-item"><span className="avs-n">24h</span><span className="avs-l">Disponível</span></div>
      </div>
    </div>
  );
}
