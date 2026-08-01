import { useEffect, useState } from 'react';
import {
  AppBar, Box, Button, Card, CardContent, Chip, Container, Grid, InputAdornment,
  Stack, TextField, Toolbar, Typography,
} from '@mui/material';
import MapsHomeWorkIcon from '@mui/icons-material/MapsHomeWork';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import SearchIcon from '@mui/icons-material/Search';
import PaymentsIcon from '@mui/icons-material/Payments';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import GroupsIcon from '@mui/icons-material/Groups';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { Link, useNavigate } from 'react-router-dom';
import { annoncesApi } from '@/api/services';
import type { Annonce } from '@/types';
import AnnonceCard from '@/components/AnnonceCard';
import villa from '@/assets/villa-connexion.png';

const ATOUTS = [
  { icone: <VerifiedUserIcon fontSize="large" />, titre: 'Démarcheurs vérifiés',
    texte: "Chaque intermédiaire est validé par notre équipe avant de pouvoir publier. Fini les arnaques." },
  { icone: <SearchIcon fontSize="large" />, titre: 'Recherche précise',
    texte: "Filtrez par quartier, budget, nombre de chambres et type de bien. Trouvez vite ce qui vous convient." },
  { icone: <GroupsIcon fontSize="large" />, titre: 'Colocation simplifiée',
    texte: "Places, loyer, charges, règles de vie : tout est clair avant même la première visite." },
  { icone: <EventAvailableIcon fontSize="large" />, titre: 'Visites organisées',
    texte: "Demandez un rendez-vous en un clic, suivez son statut, et recevez une notification à chaque étape." },
  { icone: <PaymentsIcon fontSize="large" />, titre: 'Frais sécurisés',
    texte: "Les frais de visite sont conservés en séquestre et remboursés si la visite n'a pas lieu." },
  { icone: <NotificationsActiveIcon fontSize="large" />, titre: 'Alertes personnalisées',
    texte: "Définissez vos critères et soyez prévenu dès qu'un bien correspondant est publié." },
];

export default function AccueilPage() {
  const navigate = useNavigate();
  const [recherche, setRecherche] = useState('');
  const [annonces, setAnnonces] = useState<Annonce[]>([]);

  useEffect(() => {
    annoncesApi
      .rechercher({ statut: 'PUBLIEE', size: 6, sort: 'datePublication,desc' } as never)
      .then((p) => setAnnonces(p.contenu))
      .catch(() => setAnnonces([]));
  }, []);

  return (
    <Box>
      {/* Barre publique */}
      <AppBar position="sticky" elevation={0} color="default"
        sx={{ bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar>
          <Stack direction="row" alignItems="center" spacing={1} sx={{ flexGrow: 1 }}>
            <MapsHomeWorkIcon color="primary" />
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Coloc<span style={{ opacity: 0.5 }}>Immo</span>
            </Typography>
          </Stack>
          <Button component={Link} to="/login" sx={{ mr: 1 }}>Se connecter</Button>
          <Button component={Link} to="/inscription" variant="contained">Créer un compte</Button>
        </Toolbar>
      </AppBar>

      {/* Hero */}
      <Box
        sx={{
          position: 'relative',
          color: '#fff',
          py: { xs: 8, md: 14 },
          px: 3,
          textAlign: 'center',
          backgroundImage: `linear-gradient(180deg, rgba(9,30,25,0.55), rgba(9,30,25,0.82)), url(${villa})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
        }}
      >
        <Container maxWidth="md">
          <Typography variant="h2" sx={{ fontWeight: 800, mb: 2, fontSize: { xs: '2.2rem', md: '3.4rem' } }}>
            Trouvez votre futur chez-vous
          </Typography>
          <Typography variant="h6" sx={{ opacity: 0.92, mb: 4, fontWeight: 400 }}>
            Louez, achetez ou partagez un logement au Burkina Faso, en toute confiance.
            Des annonces vérifiées et des intermédiaires dont l'identité est contrôlée.
          </Typography>
          <Stack
            component="form"
            onSubmit={(e: React.FormEvent) => {
              e.preventDefault();
              const q = recherche.trim();
              navigate(q ? `/annonces-publiques?q=${encodeURIComponent(q)}` : '/annonces-publiques');
            }}
            direction={{ xs: 'column', sm: 'row' }}
            spacing={1.5}
            sx={{ maxWidth: 620, mx: 'auto', mb: 3 }}
          >
            <TextField
              fullWidth
              placeholder="Quartier, type de bien, mot-clé…"
              value={recherche}
              onChange={(e) => setRecherche(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                sx: { bgcolor: '#fff', borderRadius: 2 },
              }}
            />
            <Button type="submit" variant="contained" size="large" sx={{ px: 4, whiteSpace: 'nowrap' }}>
              Rechercher
            </Button>
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="center">
            <Button component={Link} to="/inscription" variant="outlined" size="large"
              endIcon={<ArrowForwardIcon />}
              sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.6)' }}>Créer un compte gratuit</Button>
            <Button component={Link} to="/annonces-publiques" variant="text" size="large"
              sx={{ color: '#fff' }}>Voir toutes les annonces</Button>
          </Stack>
        </Container>
      </Box>


      {/* Pourquoi nous */}
      <Box sx={{ bgcolor: 'action.hover', py: { xs: 6, md: 8 } }}>
        <Container maxWidth="lg">
          <Typography variant="h4" textAlign="center" sx={{ fontWeight: 700, mb: 1 }}>
            Pourquoi choisir ColocImmo ?
          </Typography>
          <Typography textAlign="center" color="text.secondary" sx={{ mb: 5 }}>
            Une plateforme pensée pour la confiance et la transparence.
          </Typography>
          <Grid container spacing={3}>
            {ATOUTS.map((a) => (
              <Grid item xs={12} sm={6} md={4} key={a.titre}>
                <Card sx={{ height: '100%' }} variant="outlined">
                  <CardContent>
                    <Box sx={{ color: 'primary.main', mb: 1 }}>{a.icone}</Box>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>{a.titre}</Typography>
                    <Typography variant="body2" color="text.secondary">{a.texte}</Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>

      {/* Annonces récentes */}
      <Container maxWidth="lg" sx={{ py: { xs: 6, md: 8 } }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 3 }}>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>Annonces récentes</Typography>
          <Button component={Link} to="/annonces-publiques" endIcon={<ArrowForwardIcon />}>Voir tout</Button>
        </Stack>

        {annonces.length === 0 ? (
          <Typography color="text.secondary">Les annonces s'afficheront ici une fois la plateforme alimentée.</Typography>
        ) : (
          <Grid container spacing={3}>
            {annonces.map((a) => (
              <Grid item xs={12} sm={6} md={4} key={a.id}>
                <AnnonceCard annonce={a} basePath="/annonces-publiques" />
              </Grid>
            ))}
          </Grid>
        )}
      </Container>

      {/* Appel final */}
      <Box sx={{ bgcolor: 'primary.main', color: '#fff', py: { xs: 6, md: 8 }, textAlign: 'center' }}>
        <Container maxWidth="sm">
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 2 }}>Prêt à trouver votre logement ?</Typography>
          <Typography sx={{ opacity: 0.9, mb: 4 }}>
            Créez votre compte en une minute et accédez à toutes les annonces.
          </Typography>
          <Button component={Link} to="/inscription" variant="contained" size="large"
            sx={{ bgcolor: '#fff', color: 'primary.main', '&:hover': { bgcolor: '#f0f0f0' } }}>
            Commencer maintenant
          </Button>
        </Container>
      </Box>

      {/* Pied de page */}
      <Box sx={{ py: 3, textAlign: 'center', bgcolor: 'background.paper', borderTop: 1, borderColor: 'divider' }}>
        <Typography variant="caption" color="text.secondary">
          © 2026 ColocImmo — Plateforme de colocation et de gestion immobilière · Ouagadougou
        </Typography>
      </Box>
    </Box>
  );
}
