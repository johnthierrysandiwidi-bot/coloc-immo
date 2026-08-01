import { useEffect, useState } from 'react';
import {
  Alert, Box, Card, CardContent, Chip, Grid, List, ListItemButton, ListItemText, Typography,
} from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import ApartmentIcon from '@mui/icons-material/Apartment';
import EventIcon from '@mui/icons-material/Event';
import DescriptionIcon from '@mui/icons-material/Description';
import VisibilityIcon from '@mui/icons-material/Visibility';
import FavoriteIcon from '@mui/icons-material/Favorite';
import PaymentsIcon from '@mui/icons-material/Payments';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import CampaignIcon from '@mui/icons-material/Campaign';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { useNavigate } from 'react-router-dom';
import StatCard from '@/components/StatCard';
import { useAppSelector } from '@/app/hooks';
import { statistiquesApi } from '@/api/services';
import { ROLES } from '@/types';

const fcfa = (n: number) => new Intl.NumberFormat('fr-FR').format(n) + ' F';
const COULEURS = ['#12694f', '#c9743a', '#4bb98d'];

interface Recente { id: number; titre: string; prix: number }

export default function DashboardPage() {
  const { login, roles } = useAppSelector((s) => s.auth);
  const navigate = useNavigate();
  const [stats, setStats] = useState<Record<string, unknown>>({});
  const [erreur, setErreur] = useState<string | null>(null);

  const estAdmin = roles.includes(ROLES.ADMIN);
  const estDemarcheur = roles.includes(ROLES.DEMARCHEUR);
  const estBailleur = roles.includes(ROLES.PROPRIETAIRE) || estDemarcheur;

  useEffect(() => {
    const appel = estAdmin
      ? statistiquesApi.administrateur()
      : estBailleur
        ? statistiquesApi.bailleur()
        : statistiquesApi.utilisateur();

    appel.then(setStats).catch(() => setErreur('Statistiques indisponibles.'));
  }, [estAdmin, estBailleur]);

  const n = (cle: string) => Number(stats[cle] ?? 0);

  const cartes = estAdmin
    ? [
        { libelle: 'Utilisateurs', valeur: n('utilisateurs'), icone: <PeopleIcon /> },
        { libelle: 'Biens', valeur: n('biens'), icone: <ApartmentIcon /> },
        { libelle: 'Annonces', valeur: n('annonces'), icone: <CampaignIcon /> },
        { libelle: 'Rendez-vous', valeur: n('rendezVous'), icone: <EventIcon /> },
        { libelle: 'Démarcheurs en attente', valeur: n('demarcheursEnAttente'), icone: <VerifiedUserIcon />, couleur: 'warning.main' },
        { libelle: 'Documents en attente', valeur: n('documentsEnAttente'), icone: <DescriptionIcon />, couleur: 'secondary.main' },
      ]
    : estBailleur
      ? [
          { libelle: 'Mes biens', valeur: n('biens'), icone: <ApartmentIcon /> },
          { libelle: 'Annonces publiées', valeur: n('annoncesPubliees'), icone: <VisibilityIcon /> },
          { libelle: 'Annonces actives', valeur: n('annoncesActives'), icone: <VisibilityIcon /> },
          { libelle: 'Annonces expirées', valeur: n('annoncesExpirees'), icone: <EventIcon />, couleur: 'warning.main' },
          { libelle: 'Rendez-vous', valeur: n('rendezVous'), icone: <EventIcon /> },
          { libelle: 'Vues cumulées', valeur: n('vuesCumulees'), icone: <VisibilityIcon /> },
          { libelle: 'Revenus estimés', valeur: fcfa(n('revenusEstimes')), icone: <PaymentsIcon />, couleur: 'secondary.main' },
          ...(estDemarcheur
            ? [
                { libelle: 'Documents validés', valeur: n('documentsValides'), icone: <DescriptionIcon />, couleur: 'success.main' },
                { libelle: 'Documents en attente', valeur: n('documentsEnAttente'), icone: <DescriptionIcon />, couleur: 'warning.main' },
                { libelle: 'Documents refusés', valeur: n('documentsRefuses'), icone: <DescriptionIcon />, couleur: 'error.main' },
              ]
            : []),
        ]
      : [
          { libelle: 'Mes favoris', valeur: n('favoris'), icone: <FavoriteIcon /> },
          { libelle: 'Mes alertes', valeur: n('alertes'), icone: <NotificationsActiveIcon /> },
          { libelle: 'Mes rendez-vous', valeur: n('rendezVous'), icone: <EventIcon /> },
        ];

  const parType = (stats.annoncesParType ?? {}) as Record<string, number>;
  const donneesCamembert = Object.entries(parType).map(([nom, valeur]) => ({ nom, valeur: Number(valeur) }));
  const donneesBarres = cartes
    .filter((c) => typeof c.valeur === 'number')
    .map((c) => ({ nom: c.libelle, valeur: Number(c.valeur) }));

  const mensuelles = (stats.mensuelles ?? []) as { mois: string; publications: number }[];
  const recentes = (stats.consulteesRecemment ?? []) as Recente[];
  const statutValidation = stats.statutValidation as string | undefined;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Bonjour {login}</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        {estAdmin ? 'Vue administrateur' : estBailleur ? 'Votre activité' : 'Votre espace'}
      </Typography>

      {erreur && <Alert severity="warning" sx={{ mb: 2 }}>{erreur}</Alert>}

      {/* Le démarcheur doit voir son statut de validation en premier : c'est ce qui conditionne tout. */}
      {estDemarcheur && statutValidation && (
        <Alert
          severity={statutValidation === 'VALIDE' ? 'success' : statutValidation === 'REFUSE' ? 'error' : 'warning'}
          sx={{ mb: 3 }}
          action={
            statutValidation !== 'VALIDE' && (
              <Chip label="Déposer un document" size="small" onClick={() => navigate('/mes-documents')} />
            )
          }
        >
          {statutValidation === 'VALIDE'
            ? 'Vos documents sont validés : vous pouvez publier des annonces.'
            : statutValidation === 'REFUSE'
              ? 'Vos documents ont été refusés. Déposez une nouvelle pièce pour pouvoir publier.'
              : "Validation en attente : la publication d'annonces reste bloquée."}
        </Alert>
      )}

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {cartes.map((c) => (
          <Grid item xs={12} sm={6} md={3} key={c.libelle}>
            <StatCard {...c} />
          </Grid>
        ))}
      </Grid>

      {estBailleur && mensuelles.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>Publications des 6 derniers mois</Typography>
            <Box sx={{ height: 260 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={mensuelles}>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                  <XAxis dataKey="mois" fontSize={11} />
                  <YAxis fontSize={11} allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="publications" fill="#12694f" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </CardContent>
        </Card>
      )}

      <Grid container spacing={2}>
        <Grid item xs={12} md={estAdmin ? 7 : 12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Vue d'ensemble</Typography>
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={donneesBarres}>
                    <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                    <XAxis dataKey="nom" fontSize={11} />
                    <YAxis fontSize={11} allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="valeur" fill="#12694f" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {estAdmin && donneesCamembert.length > 0 && (
          <Grid item xs={12} md={5}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Annonces par type</Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={donneesCamembert} dataKey="valeur" nameKey="nom" outerRadius={95} label>
                        {donneesCamembert.map((_, i) => (
                          <Cell key={i} fill={COULEURS[i % COULEURS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Annonces consultées récemment — exigé par le tableau de bord Utilisateur */}
        {!estAdmin && !estBailleur && recentes.length > 0 && (
          <Grid item xs={12}>
            <Card>
              <CardContent sx={{ pb: 0 }}>
                <Typography variant="h6">Consultées récemment</Typography>
              </CardContent>
              <List>
                {recentes.map((r) => (
                  <ListItemButton key={r.id} onClick={() => navigate(`/annonces/${r.id}`)}>
                    <ListItemText primary={r.titre} secondary={fcfa(r.prix)} />
                  </ListItemButton>
                ))}
              </List>
            </Card>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}
