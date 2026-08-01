import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Grid, IconButton, MenuItem, Stack, Switch, TextField, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { alertesApi, referentielApi } from '@/api/services';
import { useAppSelector } from '@/app/hooks';
import type { Localite, Quartier, TypeImmobilier } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

interface AlerteItem {
  id: number;
  titre: string;
  typeAnnonce?: string;
  prixMin?: number;
  prixMax?: number;
  nombreChambresMin?: number;
  surfaceMin?: number;
  active: boolean;
  localite?: Localite;
  quartier?: Quartier;
  typeImmobilier?: TypeImmobilier;
}

const schema = Yup.object({
  titre: Yup.string().required('Donnez un nom à votre alerte'),
  prixMax: Yup.number().nullable().test('ordre', 'Le prix max doit dépasser le prix min', function (v) {
    const min = this.parent.prixMin;
    return !v || !min || Number(v) >= Number(min);
  }),
});

export default function MesAlertesPage() {
  const { userId } = useAppSelector((s) => s.auth);
  const [alertes, setAlertes] = useState<AlerteItem[]>([]);
  const [localites, setLocalites] = useState<Localite[]>([]);
  const [quartiers, setQuartiers] = useState<Quartier[]>([]);
  const [types, setTypes] = useState<TypeImmobilier[]>([]);
  const [ouvert, setOuvert] = useState(false);
  const [retour, setRetour] = useState<string | null>(null);

  const recharger = () => alertesApi.mesAlertes().then(setAlertes).catch(() => setAlertes([]));

  useEffect(() => {
    recharger();
    referentielApi.localites().then(setLocalites).catch(() => {});
    referentielApi.quartiers().then(setQuartiers).catch(() => {});
    referentielApi.typesImmobilier().then(setTypes).catch(() => {});
  }, []);

  const formik = useFormik({
    initialValues: {
      titre: '', typeAnnonce: '', prixMin: '', prixMax: '',
      localiteId: '', quartierId: '', typeImmobilierId: '',
      nombreChambresMin: '', surfaceMin: '', meubleUniquement: false,
    },
    validationSchema: schema,
    onSubmit: async (v) => {
      await alertesApi.creer({
        titre: v.titre,
        typeAnnonce: v.typeAnnonce || null,
        prixMin: v.prixMin ? Number(v.prixMin) : null,
        prixMax: v.prixMax ? Number(v.prixMax) : null,
        nombreChambresMin: v.nombreChambresMin ? Number(v.nombreChambresMin) : null,
        surfaceMin: v.surfaceMin ? Number(v.surfaceMin) : null,
        meubleUniquement: v.meubleUniquement,
        active: true,
        frequence: 'IMMEDIATE',
        dateCreation: new Date().toISOString(),
        titulaire: { id: userId },
        localite: v.localiteId ? { id: Number(v.localiteId) } : null,
        quartier: v.quartierId ? { id: Number(v.quartierId) } : null,
        typeImmobilier: v.typeImmobilierId ? { id: Number(v.typeImmobilierId) } : null,
      });
      setRetour('Alerte créée. Vous serez notifié dès qu\u2019une annonce correspondra.');
      setOuvert(false);
      formik.resetForm();
      recharger();
    },
  });

  const quartiersFiltres = formik.values.localiteId
    ? quartiers.filter((q) => q.localite?.id === Number(formik.values.localiteId))
    : quartiers;

  const format = (n?: number) => (n != null ? new Intl.NumberFormat('fr-FR').format(n) + ' F' : null);

  return (
    <Box>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 3 }}>
        <BoutonRetour />
        <Typography variant="h4">Mes alertes</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOuvert(true)}>
          Créer une alerte
        </Button>
      </Stack>

      {retour && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour}</Alert>}

      {alertes.length === 0 ? (
        <Typography color="text.secondary">
          Aucune alerte. Créez-en une pour être prévenu dès qu'un bien correspond à vos critères.
        </Typography>
      ) : (
        <Grid container spacing={2}>
          {alertes.map((a) => (
            <Grid item xs={12} md={6} key={a.id}>
              <Card>
                <CardContent>
                  <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                    <Box>
                      <Typography variant="subtitle1" fontWeight={600}>{a.titre}</Typography>
                      <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
                        {a.typeAnnonce && <Chip size="small" label={a.typeAnnonce} color="primary" />}
                        {a.localite && <Chip size="small" variant="outlined" label={a.localite.nom} />}
                        {a.quartier && <Chip size="small" variant="outlined" label={a.quartier.nom} />}
                        {a.typeImmobilier && <Chip size="small" variant="outlined" label={a.typeImmobilier.nom} />}
                        {(a.prixMin || a.prixMax) && (
                          <Chip size="small" variant="outlined"
                            label={`${format(a.prixMin) ?? '0'} – ${format(a.prixMax) ?? '∞'}`} />
                        )}
                        {a.nombreChambresMin && <Chip size="small" variant="outlined" label={`${a.nombreChambresMin}+ ch.`} />}
                      </Stack>
                    </Box>
                    <Stack direction="row" alignItems="center">
                      <Switch checked={a.active} size="small" disabled />
                      <IconButton size="small" color="error" onClick={() => alertesApi.supprimer(a.id).then(recharger)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Stack>
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Dialog open={ouvert} onClose={() => setOuvert(false)} fullWidth maxWidth="sm">
        <DialogTitle>Nouvelle alerte</DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12}>
              <TextField fullWidth name="titre" label="Nom de l'alerte" placeholder="Ex : Studio à Karpala"
                value={formik.values.titre} onChange={formik.handleChange} onBlur={formik.handleBlur}
                error={formik.touched.titre && Boolean(formik.errors.titre)}
                helperText={formik.touched.titre && formik.errors.titre} />
            </Grid>

            <Grid item xs={6}>
              <TextField select fullWidth name="typeAnnonce" label="Type d'annonce"
                value={formik.values.typeAnnonce} onChange={formik.handleChange}>
                <MenuItem value="">Tous</MenuItem>
                <MenuItem value="LOCATION">Location</MenuItem>
                <MenuItem value="VENTE">Vente</MenuItem>
                <MenuItem value="COLOCATION">Colocation</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField select fullWidth name="typeImmobilierId" label="Type de bien"
                value={formik.values.typeImmobilierId} onChange={formik.handleChange}>
                <MenuItem value="">Tous</MenuItem>
                {types.map((t) => <MenuItem key={t.id} value={t.id}>{t.nom}</MenuItem>)}
              </TextField>
            </Grid>

            <Grid item xs={6}>
              <TextField select fullWidth name="localiteId" label="Ville"
                value={formik.values.localiteId} onChange={formik.handleChange}>
                <MenuItem value="">Toutes</MenuItem>
                {localites.map((l) => <MenuItem key={l.id} value={l.id}>{l.nom}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField select fullWidth name="quartierId" label="Quartier"
                value={formik.values.quartierId} onChange={formik.handleChange}>
                <MenuItem value="">Tous</MenuItem>
                {quartiersFiltres.map((q) => <MenuItem key={q.id} value={q.id}>{q.nom}</MenuItem>)}
              </TextField>
            </Grid>

            <Grid item xs={6}>
              <TextField fullWidth type="number" name="prixMin" label="Prix minimum"
                value={formik.values.prixMin} onChange={formik.handleChange} />
            </Grid>
            <Grid item xs={6}>
              <TextField fullWidth type="number" name="prixMax" label="Prix maximum"
                value={formik.values.prixMax} onChange={formik.handleChange} onBlur={formik.handleBlur}
                error={formik.touched.prixMax && Boolean(formik.errors.prixMax)}
                helperText={formik.touched.prixMax && formik.errors.prixMax} />
            </Grid>

            <Grid item xs={6}>
              <TextField fullWidth type="number" name="nombreChambresMin" label="Chambres minimum"
                value={formik.values.nombreChambresMin} onChange={formik.handleChange} />
            </Grid>
            <Grid item xs={6}>
              <TextField fullWidth type="number" name="surfaceMin" label="Surface minimum (m²)"
                value={formik.values.surfaceMin} onChange={formik.handleChange} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOuvert(false)}>Annuler</Button>
          <Button variant="contained" onClick={() => formik.handleSubmit()}>Créer l'alerte</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
