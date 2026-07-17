import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user, authLoading } = useAuth();
  const location = useLocation();
  if (authLoading) return <div className="app-loading"><i className="fa-solid fa-spinner fa-spin" /> Carregando...</div>;
  return user ? children : <Navigate to="/login" replace state={{ from: location.pathname }} />;
}
