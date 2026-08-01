import { AppBar, Avatar, Button, Chip, Stack, Toolbar, Typography } from '@mui/material';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import { useState } from 'react';
import DialogueContact from '@/components/DialogueContact';
import MapsHomeWorkIcon from '@mui/icons-material/MapsHomeWork';
import { Link } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';

/**
 * En-tête des pages publiques (accueil, catalogue, fiche d'annonce).
 *
 * Elle s'affiche désormais aussi pour un utilisateur connecté. Auparavant elle
 * était masquée dès qu'une session existait, alors que les routes publiques se
 * situent hors de l'espace authentifié : la page se retrouvait donc sans aucune
 * barre de navigation. Rien n'indiquait qu'on était connecté, au point de croire
 * qu'un visiteur anonyme pouvait envoyer une demande de visite — alors que le
 * formulaire n'apparaît que pour une session valide.
 */
export default function BarrePublique() {
  const { login } = useAppSelector((s) => s.auth);
  const [contactOuvert, setContactOuvert] = useState(false);

  return (
    <AppBar
      position="sticky"
      elevation={0}
      color="default"
      sx={{ bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider' }}
    >
      <Toolbar>
        <Stack
          component={Link}
          to="/"
          direction="row"
          alignItems="center"
          spacing={1}
          sx={{ flexGrow: 1, textDecoration: 'none', color: 'inherit' }}
        >
          <MapsHomeWorkIcon color="primary" />
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Coloc<span style={{ opacity: 0.5 }}>Immo</span>
          </Typography>
        </Stack>

        <Button component={Link} to="/annonces-publiques">Parcourir</Button>
        <Button startIcon={<MailOutlineIcon />} onClick={() => setContactOuvert(true)} sx={{ mx: 0.5 }}>
          Contact
        </Button>

        {login ? (
          // Session ouverte : on nomme explicitement le compte utilisé et on offre
          // le retour vers l'espace personnel.
          <Stack direction="row" spacing={1.5} alignItems="center" sx={{ ml: 1 }}>
            <Chip
              size="small"
              color="success"
              variant="outlined"
              avatar={<Avatar sx={{ width: 22, height: 22 }}>{login.slice(0, 1).toUpperCase()}</Avatar>}
              label={`Connecté : ${login}`}
            />
            <Button component={Link} to="/tableau-de-bord" variant="contained">
              Mon espace
            </Button>
          </Stack>
        ) : (
          <>
            <Button component={Link} to="/login" sx={{ mx: 1 }}>Se connecter</Button>
            <Button component={Link} to="/inscription" variant="contained">Créer un compte</Button>
          </>
        )}
      </Toolbar>
      <DialogueContact ouvert={contactOuvert} onFermer={() => setContactOuvert(false)} />
    </AppBar>
  );
}
