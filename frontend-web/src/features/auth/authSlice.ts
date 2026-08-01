import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import axios from 'axios';
import { api, decoderJwt, tokenStore } from '@/api/client';
import type { Role } from '@/types';

interface AuthState {
  login: string | null;
  userId: number | null;
  roles: Role[];
  statut: 'idle' | 'chargement' | 'authentifie' | 'erreur';
  erreur: string | null;
}

function depuisJeton(token: string | null): Pick<AuthState, 'login' | 'roles'> {
  if (!token) return { login: null, roles: [] };
  const payload = decoderJwt(token);
  if (!payload || payload.exp * 1000 < Date.now()) return { login: null, roles: [] };
  return { login: payload.sub, roles: (payload.auth ?? '').split(' ').filter(Boolean) as Role[] };
}

const initial = depuisJeton(tokenStore.access());

const initialState: AuthState = {
  login: initial.login,
  userId: null,
  roles: initial.roles,
  statut: initial.login ? 'authentifie' : 'idle',
  erreur: null,
};

export const connexion = createAsyncThunk(
  'auth/connexion',
  async (creds: { username: string; password: string; rememberMe?: boolean }, { rejectWithValue }) => {
    try {
      const { data } = await api.post('/authenticate', creds);
      tokenStore.set(data.id_token, data.refresh_token);
      const compte = await api.get('/account');
      return { token: data.id_token as string, userId: compte.data.id as number };
    } catch (e) {
      if (axios.isAxiosError(e)) {
        const detail = e.response?.data?.detail ?? e.response?.data?.title;
        if (e.response?.status === 401) {
          // Distinguer « mauvais mot de passe » de « compte non activé » : ce n'est pas le même problème.
          return rejectWithValue(detail ?? 'Identifiants incorrects, ou compte non activé.');
        }
        return rejectWithValue(detail ?? `Connexion impossible (HTTP ${e.response?.status ?? '?'})`);
      }
      return rejectWithValue('Connexion impossible');
    }
  },
);

/**
 * Au rechargement de la page (F5), le jeton est relu du stockage local et fournit
 * le login et les rôles — mais PAS l'identifiant numérique, qui n'est renvoyé que
 * par /account. Sans cette restauration, userId reste null et toute création
 * référençant l'utilisateur (bien, annonce) part avec un id vide : Hibernate la
 * rejette (TransientPropertyValueException).
 */
export const restaurerSession = createAsyncThunk(
  'auth/restaurer',
  async (_: void, { rejectWithValue }) => {
    if (!tokenStore.access()) return rejectWithValue('aucun jeton');
    try {
      const compte = await api.get('/account');
      return { userId: compte.data.id as number };
    } catch {
      return rejectWithValue('session expirée');
    }
  }
);

export const deconnexion = createAsyncThunk('auth/deconnexion', async () => {
  const refreshToken = tokenStore.refresh();
  if (refreshToken) {
    try {
      await api.post('/auth/logout', { refreshToken });
    } catch {
      /* on se déconnecte quand même côté client */
    }
  }
  tokenStore.clear();
});

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(restaurerSession.fulfilled, (s, a) => {
        s.userId = a.payload.userId;
      })
      .addCase(connexion.pending, (s) => {
        s.statut = 'chargement';
        s.erreur = null;
      })
      .addCase(connexion.fulfilled, (s, a) => {
        const info = depuisJeton(a.payload.token);
        s.login = info.login;
        s.roles = info.roles;
        s.userId = a.payload.userId;
        s.statut = 'authentifie';
      })
      .addCase(connexion.rejected, (s, a) => {
        s.statut = 'erreur';
        s.erreur = (a.payload as string) ?? 'Erreur';
      })
      .addCase(deconnexion.fulfilled, (s) => {
        s.login = null;
        s.userId = null;
        s.roles = [];
        s.statut = 'idle';
      });
  },
});

export default authSlice.reducer;
