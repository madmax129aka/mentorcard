import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  api,
  clearSession,
  getForcePasswordChange,
  getRole,
  getToken,
  setForcePasswordChange,
  setSession,
} from "../api/client";
import type { LoginResponse } from "../api/types";

interface AuthState {
  isAuthenticated: boolean;
  role: "STUDENT" | "ADMIN" | null;
  forcePasswordChange: boolean;
  login: (username: string, password: string) => Promise<LoginResponse>;
  logout: () => void;
  changePassword: (currentPassword: string, newPassword: string) => Promise<void>;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [role, setRole] = useState<"STUDENT" | "ADMIN" | null>(
    (getRole() as "STUDENT" | "ADMIN" | null) ?? null,
  );
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(
    Boolean(getToken()),
  );
  const [forcePasswordChange, setForcePasswordChangeState] = useState<boolean>(
    getForcePasswordChange(),
  );

  const login = async (username: string, password: string) => {
    const response = await api.post<LoginResponse>("/auth/login", {
      username,
      password,
    });
    setSession(response.token, response.role, response.forcePasswordChange);
    setRole(response.role);
    setIsAuthenticated(true);
    setForcePasswordChangeState(response.forcePasswordChange);
    return response;
  };

  const logout = () => {
    clearSession();
    setRole(null);
    setIsAuthenticated(false);
    setForcePasswordChangeState(false);
  };

  const changePassword = async (currentPassword: string, newPassword: string) => {
    await api.post("/auth/change-password", { currentPassword, newPassword });
    setForcePasswordChange(false);
    setForcePasswordChangeState(false);
  };

  const value = useMemo(
    () => ({
      isAuthenticated,
      role,
      forcePasswordChange,
      login,
      logout,
      changePassword,
    }),
    [isAuthenticated, role, forcePasswordChange],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
