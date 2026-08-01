import { useState } from 'react';
import {
  Alert, Box, Button, Checkbox, FormControlLabel, IconButton, InputAdornment,
  Stack, TextField, Typography,
} from '@mui/material';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import SellOutlinedIcon from '@mui/icons-material/SellOutlined';
import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined';
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined';
import ShieldOutlinedIcon from '@mui/icons-material/ShieldOutlined';
import MapsHomeWorkIcon from '@mui/icons-material/MapsHomeWork';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { connexion } from '@/features/auth/authSlice';
import villa from '@/assets/villa-connexion.png';

const schema = Yup.object({
  username: Yup.string().required('Identifiant obligatoire'),
  password: Yup.string().min(4, 'Au moins 4 caractères').required('Mot de passe obligatoire'),
});

const ATOUTS = [
  { icone: <SellOutlinedIcon />, libelle: 'Vente' },
  { icone: <CalendarMonthOutlinedIcon />, libelle: 'Location' },
  { icone: <GroupsOutlinedIcon />, libelle: 'Colocation' },
  { icone: <ShieldOutlinedIcon />, libelle: 'Sécurité' },
];

export default function LoginPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { login, statut, erreur } = useAppSelector((s) => s.auth);
  const [voirMdp, setVoirMdp] = useState(false);

  const formik = useFormik({
    initialValues: { username: '', password: '' },
    validationSchema: schema,
    onSubmit: async (values) => {
      const res = await dispatch(connexion(values));
      if (connexion.fulfilled.match(res)) {
        const from = (location.state as { from?: Location })?.from?.pathname ?? '/tableau-de-bord';
        navigate(from, { replace: true });
      }
    },
  });

  if (login) return <Navigate to="/tableau-de-bord" replace />;

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
        bgcolor: 'background.default',
      }}
    >
      {/* Colonne gauche : visuel + accroche (masquée sur mobile) */}
      <Box
        sx={{
          display: { xs: 'none', md: 'flex' },
          position: 'relative',
          flexDirection: 'column',
          justifyContent: 'flex-end',
          p: 6,
          color: '#fff',
          backgroundImage: `linear-gradient(180deg, rgba(9,30,25,0.15) 0%, rgba(9,30,25,0.55) 55%, rgba(9,30,25,0.88) 100%), url(${villa})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
        }}
      >
        <Typography variant="h3" sx={{ fontWeight: 800, lineHeight: 1.05, mb: 2 }}>
          Trouvez votre futur chez-vous
        </Typography>
        <Typography sx={{ opacity: 0.9, maxWidth: 460, mb: 4 }}>
          La plateforme de référence pour la gestion locative et la colocation intelligente.
        </Typography>

        <Stack direction="row" spacing={4}>
          {ATOUTS.map((a) => (
            <Stack key={a.libelle} alignItems="center" spacing={0.5}>
              <Box sx={{ opacity: 0.95 }}>{a.icone}</Box>
              <Typography variant="caption">{a.libelle}</Typography>
            </Stack>
          ))}
        </Stack>
      </Box>

      {/* Colonne droite : formulaire fonctionnel */}
      <Box sx={{ display: 'grid', placeItems: 'center', p: { xs: 3, sm: 6 } }}>
        <Box sx={{ width: '100%', maxWidth: 420 }}>
          <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 4 }}>
            <MapsHomeWorkIcon color="primary" />
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Coloc<span style={{ opacity: 0.5 }}>Immo</span>
            </Typography>
          </Stack>

          <Typography variant="h4" sx={{ fontWeight: 700 }} gutterBottom>
            Connexion
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Connectez-vous pour accéder à votre espace.
          </Typography>

          {erreur && <Alert severity="error" sx={{ mb: 2 }}>{erreur}</Alert>}

          <form onSubmit={formik.handleSubmit}>
            <Stack spacing={2}>
              <TextField
                name="username"
                label="Identifiant"
                fullWidth
                value={formik.values.username}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={formik.touched.username && Boolean(formik.errors.username)}
                helperText={formik.touched.username && formik.errors.username}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start"><EmailOutlinedIcon fontSize="small" /></InputAdornment>
                  ),
                }}
              />
              <TextField
                name="password"
                label="Mot de passe"
                type={voirMdp ? 'text' : 'password'}
                fullWidth
                value={formik.values.password}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
                error={formik.touched.password && Boolean(formik.errors.password)}
                helperText={formik.touched.password && formik.errors.password}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start"><LockOutlinedIcon fontSize="small" /></InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setVoirMdp((v) => !v)} edge="end" size="small">
                        {voirMdp ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />

              <FormControlLabel
                control={<Checkbox size="small" />}
                label={<Typography variant="body2">Se souvenir de moi</Typography>}
              />

              <Button type="submit" variant="contained" size="large" disabled={statut === 'chargement'}>
                {statut === 'chargement' ? 'Connexion…' : 'Se connecter'}
              </Button>

              <Button component={Link} to="/inscription" variant="outlined" size="large">
                Créer un compte
              </Button>
            </Stack>
          </form>

          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', textAlign: 'center', mt: 4 }}>
            © 2026 ColocImmo — Tous droits réservés
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}
