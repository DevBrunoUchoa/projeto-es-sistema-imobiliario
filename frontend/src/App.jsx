import { Navigate, Route, Routes } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import Perfil from './pages/Perfil';
import RecuperarSenha from './pages/RecuperarSenha';
import RedefinirSenha from './pages/RedefinirSenha';
import Roommates from './pages/Roommates';
import OAuthSuccess from './pages/OAuthSuccess';
import VerificarEmail from './pages/VerificarEmail';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './contexts/AuthContext';

export default function App() {
  const { user, authLoading } = useAuth();

  if (authLoading) {
    return <div className="app-loading"><i className="fa-solid fa-spinner fa-spin" /> Carregando...</div>;
  }

  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/cadastro" element={user ? <Navigate to="/" replace /> : <Cadastro />} />
      <Route path="/recuperar-senha" element={user ? <Navigate to="/" replace /> : <RecuperarSenha />} />
      <Route path="/google-login/success" element={<OAuthSuccess />} />
      <Route path="/verificar-email" element={<VerificarEmail />} />
      <Route path="/redefinir-senha" element={user ? <Navigate to="/" replace /> : <RedefinirSenha />} />
      <Route path="/perfil" element={<ProtectedRoute><Perfil /></ProtectedRoute>} />
      <Route path="/roommates" element={<ProtectedRoute><Roommates /></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
