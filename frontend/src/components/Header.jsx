import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import NotificationBell from './NotificationBell';

const PODE_ANUNCIAR = ['LOCADOR', 'MISTO', 'ADMIN'];

export default function Header() {
  const { user, clearLocalSession } = useAuth();
  const podeAnunciar = user && PODE_ANUNCIAR.includes(user.role);

  return (
    <header className="navbar scrolled">
      <div className="nav-container">
        <Link to="/" className="nav-logo" aria-label="Página inicial EstudanteLar">
          <div className="logo-mark">E</div>
          <span className="logo-text">Estudante<strong>Lar</strong></span>
        </Link>

        <nav className="nav-links home-nav-links" aria-label="Navegação principal">
          <Link to="/#imoveis" className="nav-link">Alugar</Link>
          <Link to="/#como-funciona" className="nav-link">Como funciona</Link>
          <Link to="/roommates" className="nav-link">Roommates</Link>
          {podeAnunciar && <Link to="/criar-anuncio" className="nav-link">Anunciar</Link>}
          {podeAnunciar && <Link to="/meus-anuncios" className="nav-link">Meus anúncios</Link>}
          {user && <Link to="/avaliacoes" className="nav-link">Avaliações</Link>}
          {user && <Link to="/favoritos" className="nav-link"><i className="fa-solid fa-heart" style={{ marginRight: 4 }} />Favoritos</Link>}
          {user && <Link to="/mensagens" className="nav-link">Mensagens</Link>}
          {user?.role === 'ADMIN' && <Link to="/admin" className="nav-link">Admin</Link>}
        </nav>

        <div className="nav-actions">
          {user ? (
            <>
              <NotificationBell />
              <NavLink to="/perfil" className="profile-nav-link">
                <span className="profile-nav-avatar">{user.nome?.charAt(0)?.toUpperCase() || 'U'}</span>
                <span className="profile-nav-copy">
                  <small>Olá,</small>
                  <strong>{user.nome?.split(' ')[0] || 'Usuário'}</strong>
                </span>
              </NavLink>
              <button type="button" className="btn-ghost" onClick={clearLocalSession}>Sair</button>
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
