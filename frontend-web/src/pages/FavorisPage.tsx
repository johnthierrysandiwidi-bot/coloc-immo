import { useEffect, useState } from 'react';
import { Alert, Box, Grid, IconButton, Snackbar, Typography } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AnnonceCard from '@/components/AnnonceCard';
import { favorisApi, messageErreur } from '@/api/services';
import type { Annonce } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

interface Favori { id: number; annonce: Annonce }

export default function FavorisPage() {
  const [favoris, setFavoris] = useState<Favori[]>([]);
  const [erreur, setErreur] = useState<string | null>(null);

  const recharger = () => favorisApi.mesFavoris().then(setFavoris).catch(() => setFavoris([]));
  useEffect(() => { recharger(); }, []);

  const retirer = async (id: number) => {
    try {
      await favorisApi.retirer(id);
      recharger();
    } catch (e) {
      // Le clic sur la corbeille restait sans effet ni explication en cas de refus.
      setErreur(messageErreur(e));
    }
  };

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Mes favoris</Typography>
      {favoris.length === 0 ? (
        <Typography color="text.secondary">Aucun favori pour le moment.</Typography>
      ) : (
        <Grid container spacing={2}>
          {favoris.map((f) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={f.id}>
              <Box sx={{ position: 'relative' }}>
                <AnnonceCard annonce={f.annonce} />
                <IconButton
                  size="small"
                  onClick={() => retirer(f.id)}
                  sx={{ position: 'absolute', top: 8, right: 8, bgcolor: 'background.paper' }}
                >
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Box>
            </Grid>
          ))}
        </Grid>
      )}

      <Snackbar open={erreur !== null} autoHideDuration={5000} onClose={() => setErreur(null)}>
        <Alert severity="error" variant="filled" onClose={() => setErreur(null)}>{erreur}</Alert>
      </Snackbar>
    </Box>
  );
}
