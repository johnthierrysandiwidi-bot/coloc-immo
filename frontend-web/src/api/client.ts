import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api';

const ACCESS_KEY = 'ci_access_token';
const REFRESH_KEY = 'ci_refresh_token';

export const tokenStore = {
  access: () => localStorage.getItem(ACCESS_KEY),
  refresh: () => localStorage.getItem(REFRESH_KEY),
  set: (access: string, refresh: string) => {
    localStorage.setItem(ACCESS_KEY, access);
    localStorage.setItem(REFRESH_KEY, refresh);
  },
  clear: () => {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
};

export const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStore.access();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

/**
 * Sur 401 : on tente UNE fois de rafraîchir le jeton, puis on rejoue la requête.
 * Les requêtes concurrentes partagent le même rafraîchissement — pas de tempête de refresh.
 */
let refreshEnCours: Promise<string> | null = null;

async function rafraichir(): Promise<string> {
  const refreshToken = tokenStore.refresh();
  if (!refreshToken) throw new Error('Pas de refresh token');
  const { data } = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken });
  tokenStore.set(data.id_token, data.refresh_token);
  return data.id_token;
}

api.interceptors.response.use(
  (r) => r,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && original && !original._retry && tokenStore.refresh()) {
      original._retry = true;
      try {
        if (!refreshEnCours) {
          refreshEnCours = rafraichir().finally(() => {
            refreshEnCours = null;
          });
        }
        const nouveau = await refreshEnCours;
        original.headers.Authorization = `Bearer ${nouveau}`;
        return api(original);
      } catch {
        tokenStore.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);

/** Décode le payload du JWT sans dépendance externe. */
export function decoderJwt(token: string): { sub: string; auth: string; exp: number } | null {
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    return null;
  }
}
