import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Fade, FormControlLabel, Grid, IconButton, InputAdornment, Menu, MenuItem, Stack, Switch,
  TextField, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import PersonIcon from '@mui/icons-material/Person';
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera';
import SearchIcon from '@mui/icons-material/Search';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import StarIcon from '@mui/icons-material/Star';
import StarBorderIcon from '@mui/icons-material/StarBorder';
import SellIcon from '@mui/icons-material/Sell';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {biensApi, referentielApi, uploadApi, messageErreur } from '@/api/services';
import { useAppSelector } from '@/app/hooks';
import BoutonRetour from '@/components/BoutonRetour';
import {
  COULEUR_STATUT_BIEN, LIBELLE_STATUT_BIEN, STATUTS_BIEN_PROPRIETAIRE,
  type Immobilier, type Localite, type Quartier, type StatutImmobilier, type TypeImmobilier,
} from '@/types';

const schema = Yup.object({
  nom: Yup.string().required('Obligatoire').max(150),
  adresse: Yup.string().max(255),
  surface: Yup.number().min(0, 'Doit être positif').nullable(),
  nombreChambres: Yup.number().min(0).integer().nullable(),
  nombreSallesBain: Yup.number().min(0).integer().nullable(),
  nombreSalons: Yup.number().min(0).integer().nullable(),
});

/** Photo en cours d'édition : `id` présent = déjà persistée ; absent = à créer. */
interface PhotoEditable {
  id?: number;
  url: string;
  principale: boolean;
}

const STATUTS = STATUTS_BIEN_PROPRIETAIRE;

export default function MesBiensPage() {
  const { userId } = useAppSelector((s) => s.auth);
  const [biens, setBiens] = useState<Immobilier[]>([]);
  const [localites, setLocalites] = useState<Localite[]>([]);
  const [quartiers, setQuartiers] = useState<Quartier[]>([]);
  const [types, setTypes] = useState<TypeImmobilier[]>([]);
  const [ouvert, setOuvert] = useState(false);
  const [edite, setEdite] = useState<Immobilier | null>(null);
  const [photos, setPhotos] = useState<PhotoEditable[]>([]);
  const [photosSupprimees, setPhotosSupprimees] = useState<number[]>([]);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);
  const inputPhoto = useRef<HTMLInputElement>(null);

  // Recherche / filtre
  const [recherche, setRecherche] = useState('');
  const [filtreStatut, setFiltreStatut] = useState<StatutImmobilier | ''>('');

  // Mandat de démarcheur
  const [bienMandat, setBienMandat] = useState<Immobilier | null>(null);
  const [demarcheurs, setDemarcheurs] = useState<{ id: number; login: string }[]>([]);
  const [choixDemarcheur, setChoixDemarcheur] = useState<number | ''>('');

  // Menu de changement de statut
  const [ancreStatut, setAncreStatut] = useState<{ el: HTMLElement; bien: Immobilier } | null>(null);

  const recharger = () => {
    if (!userId) return;
    biensApi.mesBiens(userId).then(setBiens).catch(() => setBiens([]));
  };

  useEffect(() => {
    referentielApi.localites().then(setLocalites).catch(() => {});
    referentielApi.quartiers().then(setQuartiers).catch(() => {});
    referentielApi.typesImmobilier().then(setTypes).catch(() => {});
  }, []);

  // userId arrive de façon asynchrone après un rechargement de page : on recharge
  // les biens dès qu'il est connu.
  useEffect(() => {
    recharger();
  }, [userId]);

  const biensAffiches = useMemo(() => {
    const q = recherche.trim().toLowerCase();
    return biens.filter((b) => {
      const okTexte = !q
        || b.nom.toLowerCase().includes(q)
        || (b.adresse ?? '').toLowerCase().includes(q)
        || (b.quartier?.nom ?? '').toLowerCase().includes(q)
        || (b.localite?.nom ?? '').toLowerCase().includes(q);
      const okStatut = !filtreStatut || b.statut === filtreStatut;
      return okTexte && okStatut;
    });
  }, [biens, recherche, filtreStatut]);

  const formik = useFormik({
    enableReinitialize: true,
    initialValues: {
      nom: edite?.nom ?? '',
      description: edite?.description ?? '',
      adresse: edite?.adresse ?? '',
      surface: edite?.surface ?? '',
      nombreChambres: edite?.nombreChambres ?? '',
      nombreSallesBain: edite?.nombreSallesBain ?? '',
      nombreSalons: edite?.nombreSalons ?? '',
      garage: edite?.garage ?? false,
      piscine: edite?.piscine ?? false,
      meuble: edite?.meuble ?? false,
      localiteId: edite?.localite?.id ?? '',
      quartierId: edite?.quartier?.id ?? '',
      typeImmobilierId: edite?.typeImmobilier?.id ?? '',
    },
    validationSchema: schema,
    onSubmit: async (v) => {
      // Sans identifiant, le propriétaire partirait à null et Hibernate rejetterait
      // l'enregistrement (TransientPropertyValueException).
      if (!userId) {
        setRetour({ type: 'error', texte: 'Session incomplète. Rechargez la page.' });
        return;
      }
      const corps: Record<string, unknown> = {
        nom: v.nom,
        description: v.description || null,
        adresse: v.adresse || null,
        surface: v.surface === '' ? null : Number(v.surface),
        nombreChambres: v.nombreChambres === '' ? null : Number(v.nombreChambres),
        nombreSallesBain: v.nombreSallesBain === '' ? null : Number(v.nombreSallesBain),
        nombreSalons: v.nombreSalons === '' ? null : Number(v.nombreSalons),
        garage: v.garage,
        piscine: v.piscine,
        meuble: v.meuble,
        // On ne fige le statut à DISPONIBLE qu'à la création ; en modification on garde l'existant.
        statut: edite?.statut ?? 'DISPONIBLE',
        dateCreation: edite ? undefined : new Date().toISOString(),
        proprietaire: { id: userId },
        localite: v.localiteId ? { id: Number(v.localiteId) } : null,
        quartier: v.quartierId ? { id: Number(v.quartierId) } : null,
        typeImmobilier: v.typeImmobilierId ? { id: Number(v.typeImmobilierId) } : null,
      };

      try {
        const bien = edite
          ? await biensApi.modifier(edite.id, { ...corps, id: edite.id })
          : await biensApi.creer(corps);

        // 1) Suppressions de photos déjà persistées.
        for (const id of photosSupprimees) {
          await biensApi.supprimerImage(id);
        }
        // 2) Mise à jour (ordre + principale) des photos existantes, création des nouvelles.
        for (let i = 0; i < photos.length; i++) {
          const p = photos[i];
          if (p.id != null) {
            await biensApi.modifierImage(p.id, { ordre: i, principale: p.principale });
          } else {
            await biensApi.ajouterImage(bien.id, p.url, p.principale, i);
          }
        }

        setRetour({ type: 'success', texte: edite ? 'Bien modifié.' : 'Bien créé.' });
        fermer();
        recharger();
      } catch (e: unknown) {
        const err = e as { response?: { data?: { detail?: string }; status?: number } };
        setRetour({
          type: 'error',
          texte: err.response?.data?.detail ?? `Échec (HTTP ${err.response?.status ?? '?'})`,
        });
      }
    },
  });

  const televerserPhoto = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const fichier = e.target.files?.[0];
    if (!fichier) return;
    try {
      const url = await uploadApi.image(fichier);
      setPhotos((p) => [...p, { url, principale: p.length === 0 }]);
    } catch {
      setRetour({ type: 'error', texte: 'Image refusée : JPG, PNG ou WebP, 5 Mo maximum.' });
    } finally {
      if (inputPhoto.current) inputPhoto.current.value = '';
    }
  };

  const ouvrir = async (bien?: Immobilier) => {
    setEdite(bien ?? null);
    setPhotosSupprimees([]);
    setOuvert(true);
    if (bien) {
      // Charge les photos déjà enregistrées pour permettre suppression / réordonnancement.
      try {
        const imgs = await biensApi.imagesParBien(bien.id);
        setPhotos(imgs.map((im) => ({ id: im.id, url: im.url, principale: Boolean(im.principale) })));
      } catch {
        setPhotos([]);
      }
    } else {
      setPhotos([]);
    }
  };

  const fermer = () => {
    setOuvert(false);
    setEdite(null);
    setPhotos([]);
    setPhotosSupprimees([]);
    formik.resetForm();
  };

  const supprimer = async (id: number) => {
    try {
      await biensApi.supprimer(id);
      recharger();
    } catch (e) {
      // Sans ce filet, un refus du serveur laissait le bouton sans effet visible.
      setRetour({ type: 'error', texte: messageErreur(e) });
    }
  };

  // --- Actions sur les photos dans le formulaire ---
  const retirerPhoto = (index: number) => {
    setPhotos((ps) => {
      const cible = ps[index];
      if (cible.id != null) setPhotosSupprimees((s) => [...s, cible.id!]);
      const reste = ps.filter((_, j) => j !== index);
      // Si on a retiré la principale, la première restante le devient.
      if (cible.principale && reste.length > 0 && !reste.some((p) => p.principale)) {
        reste[0] = { ...reste[0], principale: true };
      }
      return reste;
    });
  };

  const definirPrincipale = (index: number) =>
    setPhotos((ps) => ps.map((p, j) => ({ ...p, principale: j === index })));

  const deplacer = (index: number, sens: -1 | 1) =>
    setPhotos((ps) => {
      const cible = index + sens;
      if (cible < 0 || cible >= ps.length) return ps;
      const copie = [...ps];
      [copie[index], copie[cible]] = [copie[cible], copie[index]];
      return copie;
    });

  // --- Changement de statut depuis la carte ---
  const changerStatut = async (bien: Immobilier, statut: StatutImmobilier) => {
    setAncreStatut(null);
    try {
      await biensApi.changerStatut(bien.id, statut);
      setBiens((prev) => prev.map((b) => (b.id === bien.id ? { ...b, statut } : b)));
      setRetour({ type: 'success', texte: `« ${bien.nom} » : ${LIBELLE_STATUT_BIEN[statut].toLowerCase()}.` });
    } catch (e: unknown) {
      const err = e as { response?: { status?: number } };
      setRetour({ type: 'error', texte: `Changement de statut impossible (HTTP ${err.response?.status ?? '?'}).` });
    }
  };

  const quartiersFiltres = formik.values.localiteId
    ? quartiers.filter((q) => q.localite?.id === Number(formik.values.localiteId))
    : quartiers;

  // Ouvre le dialogue de mandat et charge la liste des démarcheurs vérifiés.
  const ouvrirMandat = async (bien: Immobilier) => {
    setBienMandat(bien);
    setChoixDemarcheur(bien.demarcheur?.id ?? '');
    if (demarcheurs.length === 0) {
      try {
        setDemarcheurs(await biensApi.demarcheursDisponibles());
      } catch {
        setRetour({ type: 'error', texte: 'Impossible de charger les démarcheurs.' });
      }
    }
  };

  const enregistrerMandat = async () => {
    if (!bienMandat) return;
    try {
      const maj = choixDemarcheur === ''
        ? await biensApi.retirerMandat(bienMandat.id)
        : await biensApi.mandater(bienMandat.id, choixDemarcheur);
      setBiens((prev) => prev.map((b) => (b.id === bienMandat.id ? { ...b, demarcheur: maj.demarcheur } : b)));
      setRetour({
        type: 'success',
        texte: choixDemarcheur === '' ? 'Mandat retiré.' : 'Démarcheur mandaté.',
      });
      setBienMandat(null);
    } catch {
      setRetour({ type: 'error', texte: "L'opération a échoué. Le démarcheur doit être vérifié." });
    }
  };

  return (
    <Box>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 3 }}>
        <BoutonRetour />
        <Typography variant="h4">Mes biens</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => ouvrir()}>
          Ajouter un bien
        </Button>
      </Stack>

      {/* Barre de recherche et de filtre */}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 3 }}>
        <TextField
          size="small"
          placeholder="Rechercher (nom, adresse, quartier…)"
          value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
          sx={{ flex: 1 }}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }}
        />
        <TextField
          select size="small" label="Statut" value={filtreStatut}
          onChange={(e) => setFiltreStatut(e.target.value as StatutImmobilier | '')}
          sx={{ minWidth: 180 }}
        >
          <MenuItem value="">Tous les statuts</MenuItem>
          {STATUTS.map((s) => <MenuItem key={s} value={s}>{LIBELLE_STATUT_BIEN[s]}</MenuItem>)}
        </TextField>
      </Stack>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>
          {retour.texte}
        </Alert>
      )}

      {biensAffiches.length === 0 ? (
        <Typography color="text.secondary">
          {biens.length === 0 ? 'Aucun bien enregistré.' : 'Aucun bien ne correspond à la recherche.'}
        </Typography>
      ) : (
        <Grid container spacing={2}>
          {biensAffiches.map((b, i) => {
            const statut = (b.statut ?? 'DISPONIBLE') as StatutImmobilier;
            return (
              <Grid item xs={12} sm={6} md={4} key={b.id}>
                <Fade in timeout={200 + i * 60}>
                  <Card sx={{ transition: 'transform .18s', '&:hover': { transform: 'translateY(-3px)' } }}>
                    <CardContent>
                      <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                        <Box sx={{ minWidth: 0 }}>
                          <Typography variant="subtitle1" fontWeight={600} noWrap>{b.nom}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            {b.quartier?.nom ?? '—'}, {b.localite?.nom ?? '—'}
                          </Typography>
                        </Box>
                        <Tooltip title="Changer le statut">
                          <Chip
                            size="small"
                            label={LIBELLE_STATUT_BIEN[statut]}
                            color={COULEUR_STATUT_BIEN[statut]}
                            onClick={(e) => setAncreStatut({ el: e.currentTarget, bien: b })}
                            onDelete={(e) => setAncreStatut({ el: e.currentTarget as HTMLElement, bien: b })}
                            deleteIcon={<SellIcon />}
                          />
                        </Tooltip>
                      </Stack>

                      <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap sx={{ mt: 1.5 }}>
                        {b.typeImmobilier && <Chip size="small" label={b.typeImmobilier.nom} />}
                        {b.surface && <Chip size="small" variant="outlined" label={`${b.surface} m²`} />}
                        {b.nombreChambres != null && <Chip size="small" variant="outlined" label={`${b.nombreChambres} ch.`} />}
                        {b.garage && <Chip size="small" variant="outlined" label="Garage" />}
                        {b.piscine && <Chip size="small" variant="outlined" label="Piscine" />}
                      </Stack>

                      <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
                        <IconButton size="small" onClick={() => ouvrir(b)}><EditIcon fontSize="small" /></IconButton>
                        <IconButton size="small" color="error" onClick={() => supprimer(b.id)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                        <Tooltip title={b.demarcheur ? `Mandaté à ${b.demarcheur.login}` : 'Confier à un démarcheur'}>
                          <Button size="small" startIcon={<PersonIcon fontSize="small" />}
                            onClick={() => ouvrirMandat(b)} sx={{ ml: 'auto' }}>
                            {b.demarcheur ? b.demarcheur.login : 'Mandater'}
                          </Button>
                        </Tooltip>
                      </Stack>
                    </CardContent>
                  </Card>
                </Fade>
              </Grid>
            );
          })}
        </Grid>
      )}

      {/* Menu de changement de statut (Disponible / Loué / Vendu / Indisponible) */}
      <Menu
        open={Boolean(ancreStatut)}
        anchorEl={ancreStatut?.el ?? null}
        onClose={() => setAncreStatut(null)}
      >
        {STATUTS.map((s) => (
          <MenuItem
            key={s}
            selected={ancreStatut?.bien.statut === s}
            onClick={() => ancreStatut && changerStatut(ancreStatut.bien, s)}
          >
            <Chip size="small" label={LIBELLE_STATUT_BIEN[s]} color={COULEUR_STATUT_BIEN[s]} sx={{ mr: 1 }} />
            Marquer « {LIBELLE_STATUT_BIEN[s].toLowerCase()} »
          </MenuItem>
        ))}
      </Menu>

      <Dialog open={ouvert} onClose={fermer} fullWidth maxWidth="md">
        <DialogTitle>{edite ? 'Modifier le bien' : 'Nouveau bien'}</DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12} md={8}>
              <TextField
                fullWidth name="nom" label="Nom du bien" value={formik.values.nom}
                onChange={formik.handleChange} onBlur={formik.handleBlur}
                error={formik.touched.nom && Boolean(formik.errors.nom)}
                helperText={formik.touched.nom && formik.errors.nom}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                select fullWidth name="typeImmobilierId" label="Type de bien"
                value={formik.values.typeImmobilierId} onChange={formik.handleChange}
              >
                {types.map((t) => <MenuItem key={t.id} value={t.id}>{t.nom}</MenuItem>)}
              </TextField>
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth multiline rows={3} name="description" label="Description"
                value={formik.values.description} onChange={formik.handleChange}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField select fullWidth name="localiteId" label="Ville"
                value={formik.values.localiteId} onChange={formik.handleChange}>
                {localites.map((l) => <MenuItem key={l.id} value={l.id}>{l.nom}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField select fullWidth name="quartierId" label="Quartier"
                value={formik.values.quartierId} onChange={formik.handleChange}>
                {quartiersFiltres.map((q) => <MenuItem key={q.id} value={q.id}>{q.nom}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField fullWidth name="adresse" label="Adresse"
                value={formik.values.adresse} onChange={formik.handleChange} />
            </Grid>

            {(['surface', 'nombreChambres', 'nombreSallesBain', 'nombreSalons'] as const).map((champ) => (
              <Grid item xs={6} md={3} key={champ}>
                <TextField
                  fullWidth type="number" name={champ}
                  label={{ surface: 'Surface (m²)', nombreChambres: 'Chambres', nombreSallesBain: 'Salles de bain', nombreSalons: 'Salons' }[champ]}
                  value={formik.values[champ]} onChange={formik.handleChange}
                  error={formik.touched[champ] && Boolean(formik.errors[champ])}
                  helperText={formik.touched[champ] && formik.errors[champ]}
                />
              </Grid>
            ))}

            <Grid item xs={12}>
              <Stack direction="row" spacing={2}>
                {(['garage', 'piscine', 'meuble'] as const).map((champ) => (
                  <FormControlLabel
                    key={champ}
                    control={<Switch name={champ} checked={Boolean(formik.values[champ])} onChange={formik.handleChange} />}
                    label={{ garage: 'Garage', piscine: 'Piscine', meuble: 'Meublé' }[champ]}
                  />
                ))}
              </Stack>
            </Grid>

            {/* Gestion des photos : ajout, aperçu, principale, réordonnancement, suppression */}
            <Grid item xs={12}>
              <Button startIcon={<PhotoCameraIcon />} onClick={() => inputPhoto.current?.click()}>
                Ajouter une photo
              </Button>
              <input ref={inputPhoto} type="file" hidden accept="image/*" onChange={televerserPhoto} />
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                L'étoile définit la photo principale. Les flèches réordonnent les photos.
              </Typography>

              {photos.length > 0 && (
                <Stack direction="row" spacing={1.5} sx={{ mt: 2, flexWrap: 'wrap' }} useFlexGap>
                  {photos.map((p, i) => (
                    <Box key={p.id ?? p.url} sx={{ position: 'relative', width: 120 }}>
                      <Box
                        component="img" src={p.url} alt=""
                        sx={{
                          width: 120, height: 90, objectFit: 'cover', borderRadius: 1,
                          border: '2px solid', borderColor: p.principale ? 'primary.main' : 'divider',
                        }}
                      />
                      {p.principale && (
                        <Chip size="small" label="Principale" color="primary"
                          sx={{ position: 'absolute', bottom: 22, left: 4, height: 18, fontSize: 10 }} />
                      )}
                      <IconButton
                        size="small"
                        sx={{ position: 'absolute', top: -6, right: -6, bgcolor: 'background.paper' }}
                        onClick={() => retirerPhoto(i)}
                      >
                        <DeleteIcon sx={{ fontSize: 14 }} />
                      </IconButton>
                      <Stack direction="row" justifyContent="center" spacing={0}>
                        <Tooltip title="Définir comme principale">
                          <IconButton size="small" onClick={() => definirPrincipale(i)} color={p.principale ? 'primary' : 'default'}>
                            {p.principale ? <StarIcon sx={{ fontSize: 16 }} /> : <StarBorderIcon sx={{ fontSize: 16 }} />}
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Reculer">
                          <span>
                            <IconButton size="small" onClick={() => deplacer(i, -1)} disabled={i === 0}>
                              <ArrowUpwardIcon sx={{ fontSize: 16 }} />
                            </IconButton>
                          </span>
                        </Tooltip>
                        <Tooltip title="Avancer">
                          <span>
                            <IconButton size="small" onClick={() => deplacer(i, 1)} disabled={i === photos.length - 1}>
                              <ArrowDownwardIcon sx={{ fontSize: 16 }} />
                            </IconButton>
                          </span>
                        </Tooltip>
                      </Stack>
                    </Box>
                  ))}
                </Stack>
              )}
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={fermer}>Annuler</Button>
          <Button variant="contained" onClick={() => formik.handleSubmit()} disabled={formik.isSubmitting}>
            {edite ? 'Enregistrer' : 'Créer'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialogue de mandat : confier le bien à un démarcheur vérifié, ou retirer le mandat.
          Le champ existait au modèle mais n'avait aucune interface — le mandat restait
          théorique. Seuls les démarcheurs à l'identité validée apparaissent ici. */}
      <Dialog open={bienMandat != null} onClose={() => setBienMandat(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Confier « {bienMandat?.nom} »</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Choisissez un démarcheur vérifié pour commercialiser ce bien. Il pourra alors le
            gérer et recevoir les demandes de visite.
          </Typography>
          <TextField
            select
            fullWidth
            label="Démarcheur"
            value={choixDemarcheur}
            onChange={(e) => setChoixDemarcheur(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">Aucun (retirer le mandat)</MenuItem>
            {demarcheurs.map((d) => (
              <MenuItem key={d.id} value={d.id}>{d.login}</MenuItem>
            ))}
          </TextField>
          {demarcheurs.length === 0 && (
            <Alert severity="info" sx={{ mt: 2 }}>
              Aucun démarcheur vérifié n'est disponible pour le moment.
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBienMandat(null)}>Annuler</Button>
          <Button variant="contained" onClick={enregistrerMandat}>Enregistrer</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
