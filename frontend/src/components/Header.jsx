import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function Header() {
  const { user, logout } = useAuth();

  return (
    <header className="navbar scrolled">
      <div className="nav-container">
        <Link to="/" className="nav-logo" aria-label="Página inicial EstudanteLar">
          <div className="logo-mark">E</div>
          <span className="logo-text">Estudante<strong>Lar</strong></span>
        </Link>

        <nav className="nav-links home-nav-links" aria-label="Navegação principal">
          <a href="#imoveis" className="nav-link">Alugar</a>
          <a href="#como-funciona" className="nav-link">Como funciona</a>
          <Link to="/roommates" className="nav-link">Roommates</Link>
        </nav>

        <div className="nav-actions">
          {user ? (
            <>
              <NavLink to="/perfil" className="profile-nav-link">
                <span className="profile-nav-avatar">
                  {user.fotoUrl
                    ? <img src={user.fotoUrl} alt="" />
                    : (user.nome?.charAt(0)?.toUpperCase() || 'U')}
                </span>
                <span className="profile-nav-copy">
                  <small>Olá,</small>
                  <strong>{user.nome?.split(' ')[0] || 'Usuário'}</strong>
                </span>
              </NavLink>
              <button type="button" className="btn-ghost" onClick={logout}>Sair</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-ghost">Entrar</Link>
              <Link to="/cadastro" className="btn-primary-sm">Cadastrar</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
