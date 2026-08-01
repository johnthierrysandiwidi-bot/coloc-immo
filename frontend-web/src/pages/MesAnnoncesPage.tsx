import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Divider, Grid, MenuItem, OutlinedInput, Select, Stack, TextField, Typography, Checkbox, ListItemText,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import PublishIcon from '@mui/icons-material/Publish';
import UnpublishedIcon from '@mui/icons-material/Unpublished';
import ArchiveIcon from '@mui/icons-material/Archive';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import DeleteIcon from '@mui/icons-material/Delete';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { annoncesApi, biensApi, colocationApi, referentielApi } from '@/api/services';
import { useAppSelector } from '@/app/hooks';
import type { Annonce, Equipement, Immobilier, StatutAnnonce } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

const COULEUR_STATUT: Record<StatutAnnonce, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
  BROUILLON: 'default',
  PUBLIEE: 'success',
  SUSPENDUE: 'warning',
  EXPIREE: 'error',
  CLOTUREE: 'info',
};

const schema = Yup.object({
  titre: Yup.string().required('Obligatoire').max(150),
  prix: Yup.number().min(0, 'Doit être positif').required('Obligatoire'),
  immobilierId: Yup.number().required('Choisissez un bien'),
  type: Yup.string().required(),
  nombrePlaces: Yup.number().when('type', {
    is: 'COLOCATION',
    then: (s) => s.min(1, 'Au moins une place').required('Obligatoire'),
    otherwise: (s) => s.nullable(),
  }),
  loyer: Yup.number().when('type', {
    is: 'COLOCATION',
    then: (s) => s.min(0).required('Obligatoire'),
    otherwise: (s) => s.nullable(),
  }),
  ageMax: Yup.number().nullable().test('coherent', "L'âge max doit dépasser l'âge min", function (v) {
    const min = this.parent.ageMin;
    return !v || !min || Number(v) >= Number(min);
  }),
});

export default function MesAnnoncesPage() {
  const { userId } = useAppSelector((s) => s.auth);
  const [annonces, setAnnonces] = useState<Annonce[]>([]);
  const [biens, setBiens] = useState<Immobilier[]>([]);
  const [equipements, setEquipements] = useState<Equipement[]>([]);
  const [ouvert, setOuvert] = useState(false);
  const [chargement, setChargement] = useState(true);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  const recharger = () => {
    if (!userId) return;
    setChargement(true);
    annoncesApi
      .mesAnnonces(userId)
      .then((p) => setAnnonces(p.contenu))
      .finally(() => setChargement(false));
  };

  useEffect(() => {
    if (!userId) return;
    recharger();
    biensApi.mesBiens(userId).then(setBiens).catch(() => {});
    referentielApi.equipements().then(setEquipements).catch(() => {});
  }, [userId]);

  const formik = useFormik({
    initialValues: {
      titre: '', contenu: '', type: 'LOCATION', prix: '', immobilierId: '',
      nombrePlaces: '', placesRestantes: '', sexeRecherche: 'INDIFFERENT',
      ageMin: '', ageMax: '', loyer: '', caution: '', charges: '', reglesDeVie: '',
      equipementIds: [] as number[],
    },
    validationSchema: schema,
    onSubmit: async (v) => {
      try {
        const annonce = await annoncesApi.creer({
          titre: v.titre,
          contenu: v.contenu || undefined,
          type: v.type as never,
          prix: Number(v.prix),
          statut: 'BROUILLON',
          nombreVues: 0,
          immobilier: { id: Number(v.immobilierId) } as never,
          auteur: { id: userId } as never,
        });

        // Le détail de colocation n'existe QUE pour ce type (EF-05)
        if (v.type === 'COLOCATION') {
          await colocationApi.creerDetail({
            nombrePlaces: Number(v.nombrePlaces),
            placesRestantes: Number(v.placesRestantes || v.nombrePlaces),
            sexeRecherche: v.sexeRecherche,
            ageMin: v.ageMin ? Number(v.ageMin) : null,
            ageMax: v.ageMax ? Number(v.ageMax) : null,
            loyer: Number(v.loyer),
            caution: v.caution ? Number(v.caution) : 0,
            charges: v.charges ? Number(v.charges) : 0,
            reglesDeVie: v.reglesDeVie || null,
            annonce: { id: annonce.id },
            equipements: v.equipementIds.map((id) => ({ id })),
          });
        }

        setRetour({ type: 'success', texte: 'Annonce créée en brouillon. Publiez-la quand vous êtes prêt.' });
        setOuvert(false);
        formik.resetForm();
        recharger();
      } catch (e: unknown) {
        const err = e as { response?: { data?: { detail?: string }; status?: number } };
        setRetour({ type: 'error', texte: err.response?.data?.detail ?? `Échec (HTTP ${err.response?.status ?? '?'})` });
      }
    },
  });

  /** C'est ici que le verrou du démarcheur se manifeste : un 403 remonte du backend. */
  const publier = async (id: number) => {
    try {
      await annoncesApi.publier(id);
      setRetour({ type: 'success', texte: 'Annonce publiée. Les alertes correspondantes ont été notifiées.' });
      recharger();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { detail?: string }; status?: number } };
      setRetour({
        type: 'error',
        texte: err.response?.data?.detail ?? `Publication refusée (HTTP ${err.response?.status ?? '?'})`,
      });
    }
  };

  /** Transition générique : dépublier, archiver, renouveler. */
  const transition = async (id: number, action: 'depublier' | 'archiver' | 'renouveler', succes: string) => {
    try {
      await annoncesApi[action](id);
      setRetour({ type: 'success', texte: succes });
      recharger();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { detail?: string }; status?: number } };
      setRetour({
        type: 'error',
        texte: err.response?.data?.detail ?? `Action refusée (HTTP ${err.response?.status ?? '?'})`,
      });
    }
  };

  const colocation = formik.values.type === 'COLOCATION';

  const colonnes: GridColDef<Annonce>[] = [
    { field: 'titre', headerName: 'Titre', flex: 1, minWidth: 180 },
    { field: 'type', headerName: 'Type', width: 120 },
    {
      field: 'prix',
      headerName: 'Prix',
      width: 130,
      valueGetter: (_v, r) => (r.prix != null ? new Intl.NumberFormat('fr-FR').format(r.prix) + ' F' : '—'),
    },
    { field: 'nombreVues', headerName: 'Vues', width: 80 },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 130,
      renderCell: (p) => <Chip size="small" label={p.row.statut} color={COULEUR_STATUT[p.row.statut]} />,
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 320,
      sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5}>
          {(p.row.statut === 'BROUILLON' || p.row.statut === 'SUSPENDUE') && (
            <Button size="small" variant="contained" startIcon={<PublishIcon />} onClick={() => publier(p.row.id)}>
              Publier
            </Button>
          )}

          {p.row.statut === 'PUBLIEE' && (
            <Button size="small" startIcon={<UnpublishedIcon />}
              onClick={() => transition(p.row.id, 'depublier', 'Annonce dépubliée : elle repasse en brouillon.')}>
              Dépublier
            </Button>
          )}

          {p.row.statut === 'EXPIREE' && (
            <Button size="small" variant="contained" color="warning" startIcon={<AutorenewIcon />}
              onClick={() => transition(p.row.id, 'renouveler', 'Annonce renouvelée pour 60 jours.')}>
              Renouveler
            </Button>
          )}

          {p.row.statut !== 'CLOTUREE' && (
            <Button size="small" startIcon={<ArchiveIcon />}
              onClick={() => transition(p.row.id, 'archiver', 'Annonce archivée.')}>
              Archiver
            </Button>
          )}

          <Button size="small" color="error" startIcon={<DeleteIcon />}
            onClick={() => annoncesApi.supprimer(p.row.id).then(recharger)}>
            Suppr.
          </Button>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 3 }}>
        <BoutonRetour />
        <Typography variant="h4">Mes annonces</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOuvert(true)} disabled={biens.length === 0}>
          Nouvelle annonce
        </Button>
      </Stack>

      {biens.length === 0 && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Créez d'abord un bien : une annonce s'adosse toujours à un bien.
        </Alert>
      )}

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>
          {retour.texte}
        </Alert>
      )}

      <Card>
        <div style={{ height: 520, width: '100%' }}>
          <DataGrid rows={annonces} columns={colonnes} loading={chargement} disableRowSelectionOnClick
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }} />
        </div>
      </Card>

      <Dialog open={ouvert} onClose={() => setOuvert(false)} fullWidth maxWidth="md">
        <DialogTitle>Nouvelle annonce</DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12} md={8}>
              <TextField fullWidth name="titre" label="Titre" value={formik.values.titre}
                onChange={formik.handleChange} onBlur={formik.handleBlur}
                error={formik.touched.titre && Boolean(formik.errors.titre)}
                helperText={formik.touched.titre && formik.errors.titre} />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField select fullWidth name="type" label="Type d'annonce"
                value={formik.values.type} onChange={formik.handleChange}>
                <MenuItem value="LOCATION">Location</MenuItem>
                <MenuItem value="VENTE">Vente</MenuItem>
                <MenuItem value="COLOCATION">Colocation</MenuItem>
              </TextField>
            </Grid>

            <Grid item xs={12} md={8}>
              <TextField select fullWidth name="immobilierId" label="Bien concerné"
                value={formik.values.immobilierId} onChange={formik.handleChange}
                error={formik.touched.immobilierId && Boolean(formik.errors.immobilierId)}
                helperText={formik.touched.immobilierId && formik.errors.immobilierId}>
                {biens.map((b) => <MenuItem key={b.id} value={b.id}>{b.nom}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField fullWidth type="number" name="prix" label="Prix (FCFA)"
                value={formik.values.prix} onChange={formik.handleChange} onBlur={formik.handleBlur}
                error={formik.touched.prix && Boolean(formik.errors.prix)}
                helperText={formik.touched.prix && formik.errors.prix} />
            </Grid>

            <Grid item xs={12}>
              <TextField fullWidth multiline rows={3} name="contenu" label="Description"
                value={formik.values.contenu} onChange={formik.handleChange} />
            </Grid>

            {/* Bloc colocation — n'apparaît que si le type l'exige (EF-05) */}
            {colocation && (
              <>
                <Grid item xs={12}>
                  <Divider sx={{ my: 1 }}>
                    <Chip label="Détails de la colocation" size="small" color="success" />
                  </Divider>
                </Grid>

                {(['nombrePlaces', 'placesRestantes', 'loyer', 'caution', 'charges'] as const).map((champ) => (
                  <Grid item xs={6} md={4} key={champ}>
                    <TextField
                      fullWidth type="number" name={champ}
                      label={{
                        nombrePlaces: 'Nombre de places', placesRestantes: 'Places restantes',
                        loyer: 'Loyer (FCFA)', caution: 'Caution (FCFA)', charges: 'Charges (FCFA)',
                      }[champ]}
                      value={formik.values[champ]} onChange={formik.handleChange} onBlur={formik.handleBlur}
                      error={formik.touched[champ] && Boolean(formik.errors[champ])}
                      helperText={formik.touched[champ] && formik.errors[champ]}
                    />
                  </Grid>
                ))}

                <Grid item xs={6} md={4}>
                  <TextField select fullWidth name="sexeRecherche" label="Profil recherché"
                    value={formik.values.sexeRecherche} onChange={formik.handleChange}>
                    <MenuItem value="INDIFFERENT">Indifférent</MenuItem>
                    <MenuItem value="HOMME">Homme</MenuItem>
                    <MenuItem value="FEMME">Femme</MenuItem>
                  </TextField>
                </Grid>
                <Grid item xs={6} md={4}>
                  <TextField fullWidth type="number" name="ageMin" label="Âge minimum"
                    value={formik.values.ageMin} onChange={formik.handleChange} />
                </Grid>
                <Grid item xs={6} md={4}>
                  <TextField fullWidth type="number" name="ageMax" label="Âge maximum"
                    value={formik.values.ageMax} onChange={formik.handleChange} onBlur={formik.handleBlur}
                    error={formik.touched.ageMax && Boolean(formik.errors.ageMax)}
                    helperText={formik.touched.ageMax && formik.errors.ageMax} />
                </Grid>

                <Grid item xs={12}>
                  <Typography variant="caption" color="text.secondary">Équipements disponibles</Typography>
                  <Select
                    multiple fullWidth size="small" sx={{ mt: 0.5 }}
                    value={formik.values.equipementIds}
                    onChange={(e) => formik.setFieldValue('equipementIds', e.target.value)}
                    input={<OutlinedInput />}
                    renderValue={(ids) => (
                      <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap>
                        {(ids as number[]).map((id) => (
                          <Chip key={id} size="small" label={equipements.find((eq) => eq.id === id)?.nom ?? id} />
                        ))}
                      </Stack>
                    )}
                  >
                    {equipements.map((eq) => (
                      <MenuItem key={eq.id} value={eq.id}>
                        <Checkbox checked={formik.values.equipementIds.includes(eq.id)} />
                        <ListItemText primary={eq.nom} />
                      </MenuItem>
                    ))}
                  </Select>
                </Grid>

                <Grid item xs={12}>
                  <TextField fullWidth multiline rows={3} name="reglesDeVie" label="Règles de vie"
                    value={formik.values.reglesDeVie} onChange={formik.handleChange} />
                </Grid>
              </>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOuvert(false)}>Annuler</Button>
          <Button variant="contained" onClick={() => formik.handleSubmit()} disabled={formik.isSubmitting}>
            Créer en brouillon
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
