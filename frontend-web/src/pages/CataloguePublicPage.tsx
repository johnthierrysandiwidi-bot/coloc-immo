import { useEffect, useMemo, useState } from 'react';
import {
  Badge, Box, Button, Card, CardContent, Chip, CircularProgress, Collapse, Fade,
  Grid, MenuItem, Pagination, Stack, TextField, Typography,
} from '@mui/material';
import TuneIcon from '@mui/icons-material/Tune';
import { useSearchParams } from 'react-router-dom';
import AnnonceCard from '@/components/AnnonceCard';
import { annoncesApi, referentielApi } from '@/api/services';
import BarrePublique from '@/components/layout/BarrePublique';
import type { Annonce, FiltresRecherche, Localite, Quartier, TypeImmobilier } from '@/types';

const TAILLE = 12;

/** Recherche instantanée : on attend que la frappe se calme avant d'appeler l'API. */
function useDebounce<T>(valeur: T, delai = 400): T {
  const [differee, setDifferee] = useState(valeur);
  useEffect(() => {
    const t = setTimeout(() => setDifferee(valeur), delai);
    return () => clearTimeout(t);
  }, [valeur, delai]);
  return differee;
}

interface FiltresLocaux extends FiltresRecherche {
  nombreChambresMin?: number;
  surfaceMin?: number;
  localiteId?: number;
  quartierId?: number;
  typeImmobilierId?: number;
}

export default function CataloguePublicPage() {
  // Recherche transmise depuis la page d'accueil : /annonces-publiques?q=…
  const [parametresUrl] = useSearchParams();
  const [texte, setTexte] = useState(parametresUrl.get('q') ?? '');
  const texteDiffere = useDebounce(texte);

  const [filtres, setFiltres] = useState<FiltresLocaux>({});
  const [page, setPage] = useState(0);
  const [tri, setTri] = useState('datePublication,desc');
  const [avances, setAvances] = useState(false);

  const [annonces, setAnnonces] = useState<Annonce[]>([]);
  const [total, setTotal] = useState(0);
  const [chargement, setChargement] = useState(true);

  const [localites, setLocalites] = useState<Localite[]>([]);
  const [quartiers, setQuartiers] = useState<Quartier[]>([]);
  const [types, setTypes] = useState<TypeImmobilier[]>([]);

  useEffect(() => {
    referentielApi.localites().then(setLocalites).catch(() => {});
    referentielApi.quartiers().then(setQuartiers).catch(() => {});
    referentielApi.typesImmobilier().then(setTypes).catch(() => {});
  }, []);

  useEffect(() => {
    setChargement(true);
    annoncesApi
      .rechercher({ ...filtres, titre: texteDiffere || undefined, statut: 'PUBLIEE', page, size: TAILLE, sort: tri } as never)
      .then((p) => {
        // Les critères portant sur le BIEN ne sont pas exprimables en Criteria JHipster
        // sur la relation. On les applique côté client sur la page reçue.
        let liste = p.contenu;
        if (filtres.localiteId) liste = liste.filter((a) => a.immobilier?.localite?.id === filtres.localiteId);
        if (filtres.quartierId) liste = liste.filter((a) => a.immobilier?.quartier?.id === filtres.quartierId);
        if (filtres.typeImmobilierId) liste = liste.filter((a) => a.immobilier?.typeImmobilier?.id === filtres.typeImmobilierId);
        if (filtres.nombreChambresMin != null)
          liste = liste.filter((a) => (a.immobilier?.nombreChambres ?? 0) >= filtres.nombreChambresMin!);
        if (filtres.surfaceMin != null)
          liste = liste.filter((a) => (a.immobilier?.surface ?? 0) >= filtres.surfaceMin!);

        setAnnonces(liste);
        setTotal(p.total);
      })
      .finally(() => setChargement(false));
  }, [filtres, texteDiffere, page, tri]);

  const majFiltre = (cle: keyof FiltresLocaux, valeur: unknown) => {
    setPage(0);
    setFiltres((f) => ({ ...f, [cle]: valeur === '' || valeur == null ? undefined : valeur }));
  };

  const quartiersFiltres = useMemo(
    () => (filtres.localiteId ? quartiers.filter((q) => q.localite?.id === filtres.localiteId) : quartiers),
    [filtres.localiteId, quartiers],
  );

  const nbFiltresActifs = Object.values(filtres).filter((v) => v !== undefined).length;

  return (
    <Box>
      <BarrePublique />
      <Box sx={{ maxWidth: 1200, mx: 'auto', p: { xs: 2, md: 3 } }}>
      <Typography variant="h4" gutterBottom>Parcourir les annonces</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={5}>
              <TextField
                fullWidth size="small" label="Mot-clé"
                placeholder="Studio, villa, colocation…"
                value={texte}
                onChange={(e) => { setTexte(e.target.value); setPage(0); }}
              />
            </Grid>
            <Grid item xs={6} md={2}>
              <TextField select fullWidth size="small" label="Type" value={filtres.type ?? ''}
                onChange={(e) => majFiltre('type', e.target.value)}>
                <MenuItem value="">Tous</MenuItem>
                <MenuItem value="LOCATION">Location</MenuItem>
                <MenuItem value="VENTE">Vente</MenuItem>
                <MenuItem value="COLOCATION">Colocation</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={6} md={3}>
              <TextField select fullWidth size="small" label="Trier par" value={tri}
                onChange={(e) => setTri(e.target.value)}>
                <MenuItem value="datePublication,desc">Plus récentes</MenuItem>
                <MenuItem value="prix,asc">Prix croissant</MenuItem>
                <MenuItem value="prix,desc">Prix décroissant</MenuItem>
                <MenuItem value="nombreVues,desc">Les plus vues</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} md={2}>
              <Badge badgeContent={nbFiltresActifs} color="primary">
                <Button fullWidth variant="outlined" startIcon={<TuneIcon />} onClick={() => setAvances((v) => !v)}>
                  Filtres
                </Button>
              </Badge>
            </Grid>
          </Grid>

          <Collapse in={avances}>
            <Grid container spacing={2} sx={{ mt: 0.5 }}>
              <Grid item xs={6} md={3}>
                <TextField select fullWidth size="small" label="Ville" value={filtres.localiteId ?? ''}
                  onChange={(e) => { majFiltre('localiteId', e.target.value ? Number(e.target.value) : ''); majFiltre('quartierId', ''); }}>
                  <MenuItem value="">Toutes</MenuItem>
                  {localites.map((l) => <MenuItem key={l.id} value={l.id}>{l.nom}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={6} md={3}>
                <TextField select fullWidth size="small" label="Quartier" value={filtres.quartierId ?? ''}
                  onChange={(e) => majFiltre('quartierId', e.target.value ? Number(e.target.value) : '')}>
                  <MenuItem value="">Tous</MenuItem>
                  {quartiersFiltres.map((q) => <MenuItem key={q.id} value={q.id}>{q.nom}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={6} md={3}>
                <TextField select fullWidth size="small" label="Type de bien" value={filtres.typeImmobilierId ?? ''}
                  onChange={(e) => majFiltre('typeImmobilierId', e.target.value ? Number(e.target.value) : '')}>
                  <MenuItem value="">Tous</MenuItem>
                  {types.map((t) => <MenuItem key={t.id} value={t.id}>{t.nom}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={6} md={3}>
                <TextField fullWidth size="small" type="number" label="Chambres min."
                  value={filtres.nombreChambresMin ?? ''}
                  onChange={(e) => majFiltre('nombreChambresMin', e.target.value ? Number(e.target.value) : '')} />
              </Grid>
              <Grid item xs={6} md={3}>
                <TextField fullWidth size="small" type="number" label="Prix minimum"
                  value={filtres.prixMin ?? ''}
                  onChange={(e) => majFiltre('prixMin', e.target.value ? Number(e.target.value) : '')} />
              </Grid>
              <Grid item xs={6} md={3}>
                <TextField fullWidth size="small" type="number" label="Prix maximum"
                  value={filtres.prixMax ?? ''}
                  onChange={(e) => majFiltre('prixMax', e.target.value ? Number(e.target.value) : '')} />
              </Grid>
              <Grid item xs={6} md={3}>
                <TextField fullWidth size="small" type="number" label="Surface min. (m²)"
                  value={filtres.surfaceMin ?? ''}
                  onChange={(e) => majFiltre('surfaceMin', e.target.value ? Number(e.target.value) : '')} />
              </Grid>
              <Grid item xs={6} md={3}>
                <Button fullWidth sx={{ height: 40 }} onClick={() => { setFiltres({}); setTexte(''); setPage(0); }}>
                  Réinitialiser
                </Button>
              </Grid>
            </Grid>
          </Collapse>
        </CardContent>
      </Card>

      {chargement ? (
        <Box sx={{ display: 'grid', placeItems: 'center', py: 8 }}>
          <CircularProgress />
        </Box>
      ) : annonces.length === 0 ? (
        <Stack alignItems="center" sx={{ py: 8 }} spacing={1}>
          <Typography color="text.secondary">Aucune annonce ne correspond à ces critères.</Typography>
          <Chip label="Élargir la recherche" onClick={() => { setFiltres({}); setTexte(''); }} />
        </Stack>
      ) : (
        <>
          <Grid container spacing={2}>
            {annonces.map((a, i) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={a.id}>
                <Fade in timeout={180 + i * 40}>
                  <Box sx={{ height: '100%', transition: 'transform .18s', '&:hover': { transform: 'translateY(-4px)' } }}>
                    <AnnonceCard annonce={a} basePath="/annonces-publiques" />
                  </Box>
                </Fade>
              </Grid>
            ))}
          </Grid>

          <Stack alignItems="center" sx={{ mt: 4 }}>
            <Pagination
              count={Math.max(1, Math.ceil(total / TAILLE))}
              page={page + 1}
              onChange={(_, p) => setPage(p - 1)}
              color="primary"
            />
          </Stack>
        </>
      )}
    </Box>
    </Box>
  );
}
