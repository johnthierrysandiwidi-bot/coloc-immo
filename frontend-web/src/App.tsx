import { useEffect, useMemo, useState } from 'react';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { themeClair, themeSombre } from '@/theme';
import AppLayout from '@/components/layout/AppLayout';
import RoleGuard from '@/components/RoleGuard';
import PageIntrouvable from '@/pages/PageIntrouvable';
import AccueilPage from '@/pages/AccueilPage';
import CataloguePublicPage from '@/pages/CataloguePublicPage';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import DashboardPage from '@/pages/DashboardPage';
import AnnoncesPage from '@/pages/AnnoncesPage';
import AnnonceDetailPage from '@/pages/AnnonceDetailPage';
import FavorisPage from '@/pages/FavorisPage';
import RendezVousPage from '@/pages/RendezVousPage';
import MessageriePage from '@/pages/MessageriePage';
import NotificationsPage from '@/pages/NotificationsPage';
import MesDocumentsPage from '@/pages/MesDocumentsPage';
import MesBiensPage from '@/pages/MesBiensPage';
import MesAnnoncesPage from '@/pages/MesAnnoncesPage';
import MesAlertesPage from '@/pages/MesAlertesPage';
import StatistiquesPage from '@/pages/StatistiquesPage';
import ProfilPage from '@/pages/ProfilPage';
import ValidationDocumentsPage from '@/pages/ValidationDocumentsPage';
import AdminUtilisateursPage from '@/pages/AdminUtilisateursPage';
import AdminPaiementsPage from '@/pages/AdminPaiementsPage';
import AdminBiensPage from '@/pages/AdminBiensPage';
import AdminAnnoncesPage from '@/pages/AdminAnnoncesPage';
import AdminRendezVousPage from '@/pages/AdminRendezVousPage';
import AdminVillesQuartiersPage from '@/pages/AdminVillesQuartiersPage';
import AdminNotificationsPage from '@/pages/AdminNotificationsPage';
import AccesRefusePage from '@/pages/AccesRefusePage';
import { ROLES } from '@/types';
import { useAppDispatch } from '@/app/hooks';
import { restaurerSession } from '@/features/auth/authSlice';

const CLE_THEME = 'ci_theme_sombre';

export default function App() {
  const dispatch = useAppDispatch();
  const [modeSombre, setModeSombre] = useState(localStorage.getItem(CLE_THEME) === '1');

  // Au rechargement de la page, le jeton survit mais l'identifiant numérique, non :
  // on le récupère auprès de /account avant que les écrans n'en aient besoin.
  useEffect(() => {
    dispatch(restaurerSession());
  }, [dispatch]);
  const theme = useMemo(() => (modeSombre ? themeSombre : themeClair), [modeSombre]);

  const basculerTheme = () => {
    setModeSombre((v) => {
      localStorage.setItem(CLE_THEME, v ? '0' : '1');
      return !v;
    });
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/" element={<AccueilPage />} />
          <Route path="/annonces-publiques" element={<CataloguePublicPage />} />
          <Route path="/annonces-publiques/:id" element={<AnnonceDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/inscription" element={<RegisterPage />} />

          {/* Authentifié */}
          <Route element={<RoleGuard />}>
            <Route element={<AppLayout modeSombre={modeSombre} basculerTheme={basculerTheme} />}>
              <Route path="tableau-de-bord" element={<DashboardPage />} />
              <Route path="annonces" element={<AnnoncesPage />} />
              <Route path="annonces/:id" element={<AnnonceDetailPage />} />
              <Route path="favoris" element={<FavorisPage />} />
              <Route path="rendez-vous" element={<RendezVousPage />} />
              {/* Les notifications de visite pointent vers /rendez-vous/{id}. Sans
                  cette route, l'adresse ne correspondait à rien et l'utilisateur
                  était renvoyé à l'accueil par la règle « * » ci-dessous. */}
              <Route path="rendez-vous/:id" element={<RendezVousPage />} />
              <Route path="messages" element={<MessageriePage />} />
              <Route path="messages/:id" element={<MessageriePage />} />
              <Route path="notifications" element={<NotificationsPage />} />
              <Route path="alertes" element={<MesAlertesPage />} />
              <Route path="profil" element={<ProfilPage />} />
              <Route path="acces-refuse" element={<AccesRefusePage />} />

              {/* Propriétaire, démarcheur, admin : gestion du parc */}
              <Route
                element={<RoleGuard roles={[ROLES.PROPRIETAIRE, ROLES.DEMARCHEUR, ROLES.ADMIN]} />}
              >
                <Route path="mes-biens" element={<MesBiensPage />} />
                <Route path="mes-annonces" element={<MesAnnoncesPage />} />
              {/* Idem pour les notifications liées à une annonce précise. */}
              <Route path="mes-annonces/:id" element={<MesAnnoncesPage />} />
                <Route path="statistiques" element={<StatistiquesPage />} />
              </Route>

              {/* Démarcheur uniquement */}
              <Route element={<RoleGuard roles={[ROLES.DEMARCHEUR]} />}>
                <Route path="mes-documents" element={<MesDocumentsPage />} />
              </Route>

              {/* Administrateur uniquement */}
              <Route element={<RoleGuard roles={[ROLES.ADMIN]} />}>
                <Route path="admin/documents" element={<ValidationDocumentsPage />} />
                <Route path="admin/utilisateurs" element={<AdminUtilisateursPage />} />
                <Route path="admin/paiements" element={<AdminPaiementsPage />} />
                <Route path="admin/biens" element={<AdminBiensPage />} />
                <Route path="admin/annonces" element={<AdminAnnoncesPage />} />
                <Route path="admin/rendez-vous" element={<AdminRendezVousPage />} />
                <Route path="admin/villes-quartiers" element={<AdminVillesQuartiersPage />} />
                <Route path="admin/notifications" element={<AdminNotificationsPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<PageIntrouvable />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}
