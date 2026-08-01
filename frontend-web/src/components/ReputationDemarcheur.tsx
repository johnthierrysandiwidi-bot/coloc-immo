import { useEffect, useState } from 'react';
import { Box, Rating, Skeleton, Tooltip, Typography } from '@mui/material';
import { avisApi, type Reputation } from '@/api/services';

/**
 * Réputation d'un démarcheur : note moyenne et nombre d'avis.
 *
 * Affichée sur la fiche d'annonce, sous le prix. Sans cet affichage, les avis
 * collectés après les visites resteraient invisibles et sans effet ; c'est ici
 * qu'ils nourrissent la confiance, en complément de la vérification d'identité.
 *
 * Discret par choix : un démarcheur sans avis n'affiche rien plutôt qu'un « 0 »
 * dévalorisant — l'absence d'avis n'est pas une mauvaise note.
 */
export default function ReputationDemarcheur({ demarcheurId }: { demarcheurId: number }) {
  const [reputation, setReputation] = useState<Reputation | null>(null);
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    let actif = true;
    avisApi
      .reputation(demarcheurId)
      .then((r) => actif && setReputation(r))
      .catch(() => actif && setReputation(null))
      .finally(() => actif && setChargement(false));
    return () => {
      actif = false;
    };
  }, [demarcheurId]);

  if (chargement) return <Skeleton variant="text" width={160} height={28} />;

  // Aucun avis : on n'affiche rien, pour ne pas pénaliser un démarcheur récent.
  if (!reputation || reputation.nombreAvis === 0) return null;

  const moyenne = reputation.moyenne ?? 0;
  return (
    <Tooltip title={`${reputation.nombreAvis} avis après visite`}>
      <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.75, mb: 1 }}>
        <Rating value={moyenne} precision={0.5} readOnly size="small" />
        <Typography variant="body2" color="text.secondary">
          {moyenne.toFixed(1)} ({reputation.nombreAvis})
        </Typography>
      </Box>
    </Tooltip>
  );
}
