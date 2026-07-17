import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
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

function normalizeUser(data) {
  return {
    id: data.id,
    nome: data.nome,
    email: data.email,
    role: data.role,
    fotoUrl: data.fotoUrl || null,
  };
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);
  const [authLoading, setAuthLoading] = useState(true);

  const persistUser = useCallback((data) => {
    const authenticatedUser = normalizeUser(data);
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(authenticatedUser));
    setUser(authenticatedUser);
    return authenticatedUser;
  }, []);

  useEffect(() => {
    let active = true;

    async function restoreSession() {
      try {
        const data = await authApi.usuarioAtual();
        if (active) persistUser(data);
      } catch {
        if (active) {
          sessionStorage.removeItem(STORAGE_KEY);
          setUser(null);
        }
      } finally {
        if (active) setAuthLoading(false);
      }
    }

    restoreSession();
    return () => { active = false; };
  }, []);

  const login = useCallback(async (credentials) => {
    const data = await authApi.login(credentials);
    return persistUser(data);
  }, [persistUser]);

  const completeOAuthLogin = useCallback(async () => {
    const data = await authApi.usuarioAtual();
    return persistUser(data);
  }, [persistUser]);

  function updateLocalUser(changes) {
    setUser((current) => {
      const next = { ...current, ...changes };
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
  }

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } finally {
      sessionStorage.removeItem(STORAGE_KEY);
      setUser(null);
    }
  }, []);

  function clearLocalSession() {
    sessionStorage.removeItem(STORAGE_KEY);
    setUser(null);
  }

  const value = useMemo(() => ({
    user,
    authLoading,
    login,
    completeOAuthLogin,
    logout,
    updateLocalUser,
    clearLocalSession,
  }), [user, authLoading, login, completeOAuthLogin, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return value;
}
