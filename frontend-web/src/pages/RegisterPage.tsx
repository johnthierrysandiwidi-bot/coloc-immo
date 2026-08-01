import {
  Alert, Box, Button, Card, CardContent, Checkbox, FormControl, FormControlLabel,
  FormHelperText, MenuItem, Stack, TextField, Typography,
} from '@mui/material';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '@/api/client';
import { ROLES } from '@/types';

const schema = Yup.object({
  // Le rapport (§4.3.1) prescrit le recueil du nom, du prénom, de l'adresse
  // électronique, du téléphone et du mot de passe. Les trois premiers champs
  // manquaient, ainsi que l'acceptation des conditions.
  nom: Yup.string().min(2, 'Au moins 2 caractères').required('Obligatoire'),
  prenom: Yup.string().min(2, 'Au moins 2 caractères').required('Obligatoire'),
  login: Yup.string().min(3, 'Au moins 3 caractères').required('Obligatoire'),
  email: Yup.string().email('Email invalide').required('Obligatoire'),
  telephone: Yup.string()
    // Numéro burkinabè : 8 chiffres, indicatif +226 facultatif.
    .matches(/^(\+?226)?[\s.-]?[0-9]{8}$/, 'Numéro invalide (8 chiffres)')
    .required('Obligatoire'),
  password: Yup.string()
    .min(8, 'Au moins 8 caractères')
    .matches(/[A-Z]/, 'Au moins une majuscule')
    .matches(/[0-9]/, 'Au moins un chiffre')
    .required('Obligatoire'),
  confirmation: Yup.string()
    .oneOf([Yup.ref('password')], 'Les mots de passe diffèrent')
    .required('Obligatoire'),
  role: Yup.string().required('Choisissez un profil'),
  conditions: Yup.boolean().oneOf([true], 'Vous devez accepter les conditions'),
});

/** Le rôle ADMIN n'est jamais proposé : il ne s'auto-attribue pas (EF-01.1). */
const PROFILS = [
  { valeur: ROLES.UTILISATEUR, libelle: 'Je cherche un logement' },
  { valeur: ROLES.PROPRIETAIRE, libelle: 'Je suis propriétaire' },
  { valeur: ROLES.DEMARCHEUR, libelle: 'Je suis démarcheur' },
];

export default function RegisterPage() {
  const navigate = useNavigate();
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState(false);

  const formik = useFormik({
    initialValues: {
      nom: '', prenom: '', login: '', email: '', telephone: '',
      password: '', confirmation: '', role: ROLES.UTILISATEUR as string, conditions: false,
    },
    validationSchema: schema,
    onSubmit: async (v) => {
      setErreur(null);
      try {
        await api.post('/register', {
          login: v.login,
          firstName: v.prenom,
          lastName: v.nom,
          email: v.email,
          telephone: v.telephone.replace(/[\s.-]/g, ''),
          password: v.password,
          langKey: 'fr',
          authorities: [v.role],
        });
        setSucces(true);
        setTimeout(() => navigate('/login'), 1800);
      } catch (e: unknown) {
        // Ne JAMAIS masquer l'erreur du serveur : sans elle, impossible de diagnostiquer.
        const err = e as {
          response?: { status?: number; data?: { detail?: string; title?: string; message?: string } };
          message?: string;
        };
        const corps = err.response?.data;
        const detail = corps?.detail ?? corps?.title ?? corps?.message;
        const statut = err.response?.status;
        setErreur(
          detail
            ? `${detail}${statut ? ` (HTTP ${statut})` : ''}`
            : `Inscription impossible${statut ? ` — HTTP ${statut}` : ''} : ${err.message ?? 'erreur inconnue'}`,
        );
        console.error('Échec de l\'inscription', err.response ?? e);
      }
    },
  });

  const estDemarcheur = formik.values.role === ROLES.DEMARCHEUR;

  return (
    <Box sx={{ display: 'grid', placeItems: 'center', minHeight: '100vh', bgcolor: 'background.default', p: 2 }}>
      <Card sx={{ width: '100%', maxWidth: 480 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" gutterBottom>
            Créer un compte
          </Typography>

          {erreur && <Alert severity="error" sx={{ mb: 2 }}>{erreur}</Alert>}
          {succes && <Alert severity="success" sx={{ mb: 2 }}>Compte créé. Redirection…</Alert>}

          <form onSubmit={formik.handleSubmit}>
            <Stack spacing={2}>
              <TextField
                select name="role" label="Je suis…" fullWidth
                value={formik.values.role} onChange={formik.handleChange}
              >
                {PROFILS.map((p) => (
                  <MenuItem key={p.valeur} value={p.valeur}>{p.libelle}</MenuItem>
                ))}
              </TextField>

              {estDemarcheur && (
                <Alert severity="info">
                  Après inscription, vous devrez déposer une pièce justificative. La publication d'annonces
                  reste bloquée tant qu'un administrateur ne l'a pas validée.
                </Alert>
              )}

              {(['nom', 'prenom', 'login', 'email', 'telephone', 'password', 'confirmation'] as const).map((champ) => (
                <TextField
                  key={champ}
                  name={champ}
                  label={
                    {
                      nom: 'Nom',
                      prenom: 'Prénom',
                      login: 'Identifiant',
                      email: 'Adresse électronique',
                      telephone: 'Numéro de téléphone',
                      password: 'Mot de passe',
                      confirmation: 'Confirmer le mot de passe',
                    }[champ]
                  }
                  type={champ.includes('password') || champ === 'confirmation' ? 'password' : 'text'}
                  placeholder={champ === 'telephone' ? '70 12 34 56' : undefined}
                  fullWidth
                  value={formik.values[champ]}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched[champ] && Boolean(formik.errors[champ])}
                  helperText={formik.touched[champ] && formik.errors[champ]}
                />
              ))}

              <FormControl error={formik.touched.conditions && Boolean(formik.errors.conditions)}>
                <FormControlLabel
                  control={
                    <Checkbox
                      name="conditions"
                      checked={formik.values.conditions}
                      onChange={formik.handleChange}
                    />
                  }
                  label="J'accepte les conditions d'utilisation de la plateforme."
                />
                {formik.touched.conditions && formik.errors.conditions && (
                  <FormHelperText>{formik.errors.conditions}</FormHelperText>
                )}
              </FormControl>

              <Button type="submit" variant="contained" size="large" disabled={formik.isSubmitting}>
                Créer mon compte
              </Button>
              <Typography variant="body2" textAlign="center">
                Déjà inscrit ? <Link to="/login">Se connecter</Link>
              </Typography>
            </Stack>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
