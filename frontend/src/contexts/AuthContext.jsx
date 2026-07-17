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

  async function login(credentials) {
    const data = await authApi.login(credentials);
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

  const value = useMemo(() => ({ user, login, updateLocalUser, clearLocalSession }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return value;
}
