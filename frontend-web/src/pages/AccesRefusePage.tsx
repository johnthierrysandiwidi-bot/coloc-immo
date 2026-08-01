import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import BoutonRetour from '@/components/BoutonRetour';

export default function AccesRefusePage() {
  const navigate = useNavigate();
  return (
    <Box sx={{ textAlign: 'center', py: 10 }}>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Accès refusé</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Votre rôle ne vous donne pas accès à cette page.
      </Typography>
      <Button variant="contained" onClick={() => navigate('/tableau-de-bord')}>Retour au tableau de bord</Button>
    </Box>
  );
}
