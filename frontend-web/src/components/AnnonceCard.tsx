import { Card, CardActionArea, CardContent, CardMedia, Chip, Stack, Typography } from '@mui/material';
import PlaceIcon from '@mui/icons-material/Place';
import { useNavigate } from 'react-router-dom';
import type { Annonce } from '@/types';
import { visuelAnnonce } from '@/utils/illustrations';

const COULEUR_TYPE: Record<string, 'success' | 'info' | 'warning'> = {
  VENTE: 'warning',
  LOCATION: 'info',
  COLOCATION: 'success',
};

export function formaterFCFA(montant?: number) {
  if (montant == null) return '—';
  return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
}

export default function AnnonceCard({ annonce, basePath = '/annonces' }: { annonce: Annonce; basePath?: string }) {
  const navigate = useNavigate();
  // photoUrl vient du backend. On garde l'ancien chemin en secours.
  const photo =
    annonce.photoUrl ??
    (annonce.immobilier?.images?.find((i) => i.principale) ?? annonce.immobilier?.images?.[0])?.url;
  // Seules les photos téléversées dans l'application sont affichées ; toute autre
  // adresse (jeu de démonstration) est remplacée par l'illustration du type de bien.
  const visuel = visuelAnnonce(annonce, photo);

  return (
    <Card sx={{ height: '100%' }}>
      <CardActionArea onClick={() => navigate(`${basePath}/${annonce.id}`)} sx={{ height: '100%' }}>
        <CardMedia
          component="div"
          sx={{
            height: 160,
            bgcolor: 'action.hover',
            backgroundImage: `url(${visuel})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />
        <CardContent>
          <Stack direction="row" spacing={1} sx={{ mb: 1 }}>
            <Chip size="small" label={annonce.type} color={COULEUR_TYPE[annonce.type]} />
            {annonce.type === 'COLOCATION' && annonce.detailColocation && (
              <Chip
                size="small"
                variant="outlined"
                label={`${annonce.detailColocation.placesRestantes} place(s)`}
              />
            )}
          </Stack>

          <Typography variant="subtitle1" fontWeight={600} noWrap>
            {annonce.titre}
          </Typography>

          <Stack direction="row" alignItems="center" spacing={0.5} sx={{ color: 'text.secondary', my: 0.5 }}>
            <PlaceIcon sx={{ fontSize: 15 }} />
            <Typography variant="caption" noWrap>
              {annonce.immobilier?.quartier?.nom ?? '—'}, {annonce.immobilier?.localite?.nom ?? '—'}
            </Typography>
          </Stack>

          <Typography variant="h6" color="primary" sx={{ mt: 1 }}>
            {formaterFCFA(annonce.prix)}
          </Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
