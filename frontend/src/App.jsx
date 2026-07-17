import { Navigate, Route, Routes } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import Perfil from './pages/Perfil';
import RecuperarSenha from './pages/RecuperarSenha';
import RedefinirSenha from './pages/RedefinirSenha';
import VerificarEmail from './pages/VerificarEmail';
import Roommates from './pages/Roommates';
import DetalheImovel from './pages/DetalheImovel';
import CriarAnuncio from './pages/CriarAnuncio';
import EditarAnuncio from './pages/EditarAnuncio';
import MeusAnuncios from './pages/MeusAnuncios';
import Avaliacoes from './pages/Avaliacoes';
import AdminDashboard from './pages/AdminDashboard';
import Favoritos from './pages/Favoritos';
import Mensagens from './pages/Mensagens';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './contexts/AuthContext';

const PODE_ANUNCIAR = ['LOCADOR', 'MISTO', 'ADMIN'];

export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/cadastro" element={user ? <Navigate to="/" replace /> : <Cadastro />} />
      <Route path="/recuperar-senha" element={user ? <Navigate to="/" replace /> : <RecuperarSenha />} />
      <Route path="/redefinir-senha" element={user ? <Navigate to="/" replace /> : <RedefinirSenha />} />
      <Route path="/verificar-email/:token" element={<VerificarEmail />} />
      <Route path="/perfil" element={<ProtectedRoute><Perfil /></ProtectedRoute>} />
      <Route path="/roommates" element={<ProtectedRoute><Roommates /></ProtectedRoute>} />
      <Route path="/imoveis/:id" element={<DetalheImovel />} />
      <Route path="/criar-anuncio" element={<ProtectedRoute roles={PODE_ANUNCIAR}><CriarAnuncio /></ProtectedRoute>} />
      <Route path="/editar-anuncio/:id" element={<ProtectedRoute roles={PODE_ANUNCIAR}><EditarAnuncio /></ProtectedRoute>} />
      <Route path="/meus-anuncios" element={<ProtectedRoute roles={PODE_ANUNCIAR}><MeusAnuncios /></ProtectedRoute>} />
      <Route path="/avaliacoes" element={<ProtectedRoute><Avaliacoes /></ProtectedRoute>} />
      <Route path="/favoritos" element={<ProtectedRoute><Favoritos /></ProtectedRoute>} />
      <Route path="/mensagens" element={<ProtectedRoute><Mensagens /></ProtectedRoute>} />
      <Route path="/admin" element={<ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
