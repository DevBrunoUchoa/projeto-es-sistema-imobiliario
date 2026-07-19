import { createContext, useContext, useMemo, useState } from 'react';
import { authApi } from '../api/authApi';

const STORAGE_KEY = 'usuarioLogado';
const AuthContext = createContext(null);

function readStoredUser() {
  try {
    return JSON.parse(sessionStorage.getItem(STORAGE_KEY));
  } catch {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  function armazenarUsuario(data) {
    const authenticatedUser = {
      id: data.id,
      nome: data.nome,
      email: data.email,
      role: data.role,
    };
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(authenticatedUser));
    setUser(authenticatedUser);
    return authenticatedUser;
  }

  async function login(credentials) {
    const data = await authApi.login(credentials);
    return armazenarUsuario(data);
  }

  // Usado após o redirect do login com Google: os cookies de sessão já foram
  // setados pelo backend, só falta descobrir quem é o usuário autenticado.
  async function restoreSession() {
    const data = await authApi.refresh();
    return armazenarUsuario(data);
  }

  function updateLocalUser(changes) {
    setUser((current) => {
      const next = { ...current, ...changes };
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
  }

  function clearLocalSession() {
    sessionStorage.removeItem(STORAGE_KEY);
    setUser(null);
  }

  const value = useMemo(() => ({ user, login, restoreSession, updateLocalUser, clearLocalSession }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return value;
}
