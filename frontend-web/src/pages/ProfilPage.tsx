import { useEffect, useState } from 'react';
import {
  Alert, Avatar, Box, Button, Card, CardContent, Dialog, DialogActions, DialogContent,
  DialogContentText, DialogTitle, Divider, Grid, Stack, TextField, Typography,
} from '@mui/material';
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { useNavigate } from 'react-router-dom';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { compteApi, uploadApi } from '@/api/services';
import { useAppDispatch } from '@/app/hooks';
import { deconnexion } from '@/features/auth/authSlice';
import BoutonRetour from '@/components/BoutonRetour';

const schemaProfil = Yup.object({
  firstName: Yup.string().max(50, '50 caractères maximum'),
  lastName: Yup.string().max(50, '50 caractères maximum'),
  email: Yup.string().email('Adresse invalide').required('Obligatoire'),
});

const schemaMotDePasse = Yup.object({
  currentPassword: Yup.string().required('Obligatoire'),
  newPassword: Yup.string()
    .min(8, '8 caractères minimum')
    .matches(/[A-Z]/, 'Au moins une majuscule')
    .matches(/[0-9]/, 'Au moins un chiffre')
    .required('Obligatoire'),
  confirmation: Yup.string()
    .oneOf([Yup.ref('newPassword')], 'Les mots de passe ne correspondent pas')
    .required('Obligatoire'),
});

export default function ProfilPage() {
  const [compte, setCompte] = useState<Record<string, unknown> | null>(null);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);
  const [photo, setPhoto] = useState<string>('');
  const [confirmerDesactivation, setConfirmerDesactivation] = useState(false);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const desactiverCompte = async () => {
    try {
      await compteApi.desactiverMonCompte();
      setConfirmerDesactivation(false);
      // Le compte est désactivé côté serveur : on ferme la session et on renvoie vers la connexion.
      await dispatch(deconnexion());
      navigate('/login', { replace: true });
    } catch (e: unknown) {
      const err = e as { response?: { status?: number } };
      setConfirmerDesactivation(false);
      setRetour({ type: 'error', texte: `Désactivation impossible (HTTP ${err.response?.status ?? '?'}).` });
    }
  };

  useEffect(() => {
    compteApi.monCompte().then((c) => {
      setCompte(c);
      setPhoto((c.imageUrl as string) ?? '');
    });
  }, []);

  const formProfil = useFormik({
    enableReinitialize: true,
    initialValues: {
      firstName: (compte?.firstName as string) ?? '',
      lastName: (compte?.lastName as string) ?? '',
      email: (compte?.email as string) ?? '',
    },
    validationSchema: schemaProfil,
    onSubmit: async (v) => {
      try {
        await compteApi.enregistrer({ ...compte, ...v, imageUrl: photo });
        setRetour({ type: 'success', texte: 'Profil enregistré.' });
      } catch (e: unknown) {
        const err = e as { response?: { data?: { detail?: string }; status?: number } };
        setRetour({ type: 'error', texte: err.response?.data?.detail ?? `Échec (HTTP ${err.response?.status ?? '?'})` });
      }
    },
  });

  const formMotDePasse = useFormik({
    initialValues: { currentPassword: '', newPassword: '', confirmation: '' },
    validationSchema: schemaMotDePasse,
    onSubmit: async (v, { resetForm }) => {
      try {
        await compteApi.changerMotDePasse(v.currentPassword, v.newPassword);
        setRetour({ type: 'success', texte: 'Mot de passe modifié.' });
        resetForm();
      } catch (e: unknown) {
        const err = e as { response?: { status?: number } };
        setRetour({
          type: 'error',
          texte:
            err.response?.status === 400
              ? 'Le mot de passe actuel est incorrect.'
              : `Échec (HTTP ${err.response?.status ?? '?'})`,
        });
      }
    },
  });

  const changerPhoto = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const fichier = e.target.files?.[0];
    if (!fichier) return;
    try {
      const url = await uploadApi.image(fichier);
      setPhoto(url);
      await compteApi.enregistrer({ ...compte, ...formProfil.values, imageUrl: url });
      setRetour({ type: 'success', texte: 'Photo de profil mise à jour.' });
    } catch {
      setRetour({ type: 'error', texte: "L'envoi de la photo a échoué." });
    }
  };

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Mon profil</Typography>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 3 }} onClose={() => setRetour(null)}>
          {retour.texte}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 3 }}>
                <Avatar src={photo || undefined} sx={{ width: 64, height: 64 }}>
                  {(compte?.login as string)?.slice(0, 2).toUpperCase()}
                </Avatar>
                <Button component="label" size="small" startIcon={<PhotoCameraIcon />}>
                  Changer la photo
                  <input type="file" hidden accept="image/*" onChange={changerPhoto} />
                </Button>
              </Stack>

              <Divider sx={{ mb: 3 }} />

              <Typography variant="h6" gutterBottom>Informations</Typography>
              <Stack spacing={2} component="form" onSubmit={formProfil.handleSubmit}>
                <TextField
                  label="Identifiant" size="small" disabled
                  value={(compte?.login as string) ?? ''}
                  helperText="L'identifiant n'est pas modifiable."
                />
                <TextField
                  label="Prénom" size="small" name="firstName"
                  value={formProfil.values.firstName}
                  onChange={formProfil.handleChange}
                  error={Boolean(formProfil.touched.firstName && formProfil.errors.firstName)}
                  helperText={formProfil.touched.firstName && formProfil.errors.firstName}
                />
                <TextField
                  label="Nom" size="small" name="lastName"
                  value={formProfil.values.lastName}
                  onChange={formProfil.handleChange}
                  error={Boolean(formProfil.touched.lastName && formProfil.errors.lastName)}
                  helperText={formProfil.touched.lastName && formProfil.errors.lastName}
                />
                <TextField
                  label="Email" size="small" name="email"
                  value={formProfil.values.email}
                  onChange={formProfil.handleChange}
                  error={Boolean(formProfil.touched.email && formProfil.errors.email)}
                  helperText={formProfil.touched.email && formProfil.errors.email}
                />
                <Button type="submit" variant="contained" disabled={formProfil.isSubmitting}>
                  Enregistrer
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Changer le mot de passe</Typography>
              <Stack spacing={2} component="form" onSubmit={formMotDePasse.handleSubmit}>
                <TextField
                  label="Mot de passe actuel" type="password" size="small" name="currentPassword"
                  value={formMotDePasse.values.currentPassword}
                  onChange={formMotDePasse.handleChange}
                  error={Boolean(formMotDePasse.touched.currentPassword && formMotDePasse.errors.currentPassword)}
                  helperText={formMotDePasse.touched.currentPassword && formMotDePasse.errors.currentPassword}
                />
                <TextField
                  label="Nouveau mot de passe" type="password" size="small" name="newPassword"
                  value={formMotDePasse.values.newPassword}
                  onChange={formMotDePasse.handleChange}
                  error={Boolean(formMotDePasse.touched.newPassword && formMotDePasse.errors.newPassword)}
                  helperText={formMotDePasse.touched.newPassword && formMotDePasse.errors.newPassword}
                />
                <TextField
                  label="Confirmer" type="password" size="small" name="confirmation"
                  value={formMotDePasse.values.confirmation}
                  onChange={formMotDePasse.handleChange}
                  error={Boolean(formMotDePasse.touched.confirmation && formMotDePasse.errors.confirmation)}
                  helperText={formMotDePasse.touched.confirmation && formMotDePasse.errors.confirmation}
                />
                <Button type="submit" variant="contained" disabled={formMotDePasse.isSubmitting}>
                  Modifier le mot de passe
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card sx={{ borderColor: 'error.light', borderWidth: 1, borderStyle: 'solid' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <WarningAmberIcon color="error" />
                <Typography variant="h6" color="error">Désactiver mon compte</Typography>
              </Stack>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Votre compte sera désactivé et vous serez déconnecté. Vos annonces ne seront plus
                visibles. Contactez un administrateur pour une réactivation ultérieure.
              </Typography>
              <Button color="error" variant="outlined" onClick={() => setConfirmerDesactivation(true)}>
                Désactiver mon compte
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Dialog open={confirmerDesactivation} onClose={() => setConfirmerDesactivation(false)}>
        <DialogTitle>Confirmer la désactivation</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Êtes-vous sûr de vouloir désactiver votre compte ? Cette action vous déconnectera
            immédiatement et masquera votre profil et vos annonces.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmerDesactivation(false)}>Annuler</Button>
          <Button color="error" variant="contained" onClick={desactiverCompte}>
            Oui, désactiver
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
