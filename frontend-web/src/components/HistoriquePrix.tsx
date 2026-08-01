import { useEffect, useState } from 'react';
import { Box, Card, CardContent, Chip, Stack, Typography } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import TrendingFlatIcon from '@mui/icons-material/TrendingFlat';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { biensApi, type PointPrix } from '@/api/services';
import { formaterFCFA } from '@/components/AnnonceCard';

/** Affiche l'évolution du prix d'un bien — transparence des prix. */
export default function HistoriquePrix({ immobilierId }: { immobilierId: number }) {
  const [points, setPoints] = useState<PointPrix[]>([]);

  useEffect(() => {
    biensApi.historiquePrix(immobilierId).then(setPoints).catch(() => setPoints([]));
  }, [immobilierId]);

  // Moins de deux points : pas d'évolution à montrer.
  if (points.length < 2) return null;

  // recharts veut l'ordre chronologique croissant.
  const donnees = [...points]
    .reverse()
    .map((p) => ({
      date: new Date(p.dateEffet).toLocaleDateString('fr-FR', { month: 'short', year: '2-digit' }),
      prix: p.prix,
    }));

  const premier = donnees[0].prix;
  const dernier = donnees[donnees.length - 1].prix;
  const variation = dernier - premier;
  const pct = premier > 0 ? Math.round((variation / premier) * 100) : 0;

  const tendance =
    variation > 0
      ? { icone: <TrendingUpIcon fontSize="small" />, couleur: 'error' as const, texte: `+${pct}%` }
      : variation < 0
        ? { icone: <TrendingDownIcon fontSize="small" />, couleur: 'success' as const, texte: `${pct}%` }
        : { icone: <TrendingFlatIcon fontSize="small" />, couleur: 'default' as const, texte: 'stable' };

  return (
    <Card sx={{ mt: 2 }}>
      <CardContent>
        <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 1 }}>
          <Typography variant="h6">Évolution du prix</Typography>
          <Chip size="small" color={tendance.couleur} icon={tendance.icone} label={tendance.texte} />
        </Stack>
        <Typography variant="caption" color="text.secondary">
          De {formaterFCFA(premier)} à {formaterFCFA(dernier)} sur {donnees.length} relevés
        </Typography>
        <Box sx={{ height: 180, mt: 2 }}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={donnees} margin={{ top: 8, right: 12, left: 0, bottom: 0 }}>
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 11 }} width={70}
                tickFormatter={(v) => `${(v / 1000).toFixed(0)}k`} />
              <Tooltip formatter={(v: number) => formaterFCFA(v)} />
              <Line type="monotone" dataKey="prix" stroke="#0f5c43" strokeWidth={2.5} dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </Box>
      </CardContent>
    </Card>
  );
}
