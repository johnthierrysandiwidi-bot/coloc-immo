import { useEffect, useState } from 'react';
import {
  Alert, Box, Card, CardContent, Grid, LinearProgress, List, ListItem, ListItemText, Stack,
  Typography,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EventIcon from '@mui/icons-material/Event';
import DonutLargeIcon from '@mui/icons-material/DonutLarge';
import {
  Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import StatCard from '@/components/StatCard';
import { statistiquesApi } from '@/api/services';
import BoutonRetour from '@/components/BoutonRetour';

const COULEURS = ['#12694f', '#c9743a', '#4bb98d', '#8884d8'];

interface TopAnnonce { id: number; titre: string; vues: number; demandes?: number }
interface PerfType { type: string; annonces: number; vues: number; demandes: number }

export default function StatistiquesPage() {
  const [stats, setStats] = useState<Record<string, unknown>>({});
  const [erreur, setErreur] = useState<string | null>(null);
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    statistiquesApi.bailleur()
      .then(setStats)
      .catch(() => setErreur('Statistiques indisponibles.'))
      .finally(() => setChargement(false));
  }, []);

  const n = (cle: string) => Number(stats[cle] ?? 0);

  const top = ((stats.topAnnonces ?? stats.consulteesRecemment ?? []) as TopAnnonce[]).slice(0, 8);

  // Performances par type : accepte soit un tableau typé, soit un objet { TYPE: {...} }.
  let perfs: PerfType[] = [];
  if (Array.isArray(stats.perfParType)) {
    perfs = stats.perfParType as PerfType[];
  } else if (stats.perfParType && typeof stats.perfParType === 'object') {
    perfs = Object.entries(stats.perfParType as Record<string, Partial<PerfType>>).map(([type, v]) => ({
      type,
      annonces: Number(v.annonces ?? 0),
      vues: Number(v.vues ?? 0),
      demandes: Number(v.demandes ?? 0),
    }));
  } else if (stats.annoncesParType && typeof stats.annoncesParType === 'object') {
    perfs = Object.entries(stats.annoncesParType as Record<string, number>).map(([type, annonces]) => ({
      type, annonces: Number(annonces), vues: 0, demandes: 0,
    }));
  }

  const tauxOccupation = Math.max(0, Math.min(100, n('tauxOccupation')));
  const donneesVuesType = perfs.map((p) => ({ nom: p.type, valeur: p.vues }));

  if (chargement) return <LinearProgress />;

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Statistiques</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Performances de vos annonces et de votre parc immobilier.
      </Typography>

      {erreur && <Alert severity="warning" sx={{ mb: 2 }}>{erreur}</Alert>}

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard libelle="Vues cumulées" valeur={n('vuesCumulees')} icone={<VisibilityIcon />} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard libelle="Demandes de visite" valeur={n('demandesVisite')} icone={<EventIcon />} couleur="secondary.main" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard libelle="Biens loués + vendus" valeur={n('biensLoues') + n('biensVendus')} icone={<DonutLargeIcon />} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard libelle="Taux d'occupation" valeur={`${tauxOccupation.toFixed(0)} %`} icone={<DonutLargeIcon />} couleur="success.main" />
        </Grid>
      </Grid>

      <Grid container spacing={2}>
        {/* Taux d'occupation */}
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>Taux d'occupation du parc</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                Part des biens loués ou vendus sur l'ensemble de vos biens.
              </Typography>
              <Stack direction="row" alignItems="center" spacing={2}>
                <Box sx={{ flex: 1 }}>
                  <LinearProgress variant="determinate" value={tauxOccupation} sx={{ height: 12, borderRadius: 6 }} />
                </Box>
                <Typography variant="h5" fontWeight={700}>{tauxOccupation.toFixed(0)}%</Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Répartition des vues par type d'annonce */}
        <Grid item xs={12} md={8}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>Vues par type d'annonce</Typography>
              {donneesVuesType.some((d) => d.valeur > 0) ? (
                <Box sx={{ height: 260 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={donneesVuesType} dataKey="valeur" nameKey="nom" outerRadius={90} label>
                        {donneesVuesType.map((_, i) => <Cell key={i} fill={COULEURS[i % COULEURS.length]} />)}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              ) : (
                <Typography color="text.secondary">Pas encore de données de vues.</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Annonces les plus consultées */}
        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Annonces les plus consultées</Typography>
              {top.length > 0 ? (
                <Box sx={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={top} layout="vertical" margin={{ left: 24 }}>
                      <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                      <XAxis type="number" fontSize={11} allowDecimals={false} />
                      <YAxis type="category" dataKey="titre" width={140} fontSize={11} />
                      <Tooltip />
                      <Bar dataKey="vues" fill="#12694f" radius={[0, 6, 6, 0]} name="Vues" />
                    </BarChart>
                  </ResponsiveContainer>
                </Box>
              ) : (
                <Typography color="text.secondary">Aucune annonce consultée pour le moment.</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Détail vues + demandes par annonce */}
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent sx={{ pb: 0 }}>
              <Typography variant="h6">Détail par annonce</Typography>
            </CardContent>
            <List dense>
              {top.length === 0 && (
                <ListItem><ListItemText secondary="Aucune donnée." /></ListItem>
              )}
              {top.map((a) => (
                <ListItem key={a.id} secondaryAction={
                  <Stack direction="row" spacing={2}>
                    <Typography variant="body2"><VisibilityIcon sx={{ fontSize: 14, verticalAlign: 'middle' }} /> {a.vues}</Typography>
                    <Typography variant="body2" color="text.secondary"><EventIcon sx={{ fontSize: 14, verticalAlign: 'middle' }} /> {a.demandes ?? 0}</Typography>
                  </Stack>
                }>
                  <ListItemText primary={a.titre} primaryTypographyProps={{ noWrap: true, sx: { maxWidth: 180 } }} />
                </ListItem>
              ))}
            </List>
          </Card>
        </Grid>

        {/* Performances par type */}
        {perfs.length > 0 && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Performances par type d'annonce</Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={perfs}>
                      <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                      <XAxis dataKey="type" fontSize={12} />
                      <YAxis fontSize={11} allowDecimals={false} />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="annonces" fill="#12694f" radius={[6, 6, 0, 0]} name="Annonces" />
                      <Bar dataKey="vues" fill="#c9743a" radius={[6, 6, 0, 0]} name="Vues" />
                      <Bar dataKey="demandes" fill="#4bb98d" radius={[6, 6, 0, 0]} name="Demandes" />
                    </BarChart>
                  </ResponsiveContainer>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}
