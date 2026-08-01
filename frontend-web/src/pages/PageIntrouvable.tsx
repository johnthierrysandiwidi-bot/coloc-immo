import { Box, Button, Stack, Typography } from '@mui/material';
import SearchOffIcon from '@mui/icons-material/SearchOff';
import { Link, useLocation } from 'react-router-dom';
import BoutonRetour from '@/components/BoutonRetour';
import { useAppSelector } from '@/app/hooks';

/**
 * Page affichée pour une adresse inconnue.
 *
 * Auparavant, toute route non reconnue déclenchait une redirection silencieuse
 * vers l'accueil. Un lien de notification mal formé donnait donc l'impression que
 * « cliquer sur une notification ramène à l'accueil », sans le moindre indice sur
 * la cause. On préfère nommer le problème et proposer une issue.
 */
export default function PageIntrouvable() {
  const { pathname } = useLocation();
  const { login } = useAppSelector((s) => s.auth);

  return (
    <Box
      sx={{
        minHeight: '60vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign: 'center',
        p: 3,
      }}
    >
      <SearchOffIcon sx={{ fontSize: 72, color: 'text.disabled', mb: 2 }} />
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Cette page n'existe pas
      </Typography>
      <Typography color="text.secondary" sx={{ maxWidth: 460, mb: 1 }}>
        L'adresse demandée est introuvable. Le contenu a peut-être été supprimé,
        ou le lien qui vous a amené ici est incorrect.
      </Typography>
      <Typography variant="caption" color="text.disabled" sx={{ mb: 3, wordBreak: 'break-all' }}>
        {pathname}
      </Typography>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
        <BoutonRetour vers={login ? '/tableau-de-bord' : '/'} />
        <Button
          component={Link}
          to={login ? '/tableau-de-bord' : '/'}
          variant="contained"
        >
          {login ? 'Mon tableau de bord' : "Aller à l'accueil"}
        </Button>
      </Stack>
    </Box>
  );
}
