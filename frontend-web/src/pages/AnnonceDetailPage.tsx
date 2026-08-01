import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Alert, Box, Button, Card, CardContent, Chip, CircularProgress, Divider,
  Link as MuiLink,
  Grid, Stack, TextField, Typography,
} from '@mui/material';
import ChatIcon from '@mui/icons-material/ChatBubbleOutline';
import { annoncesApi, favorisApi, messagerieApi, rendezVousApi, vueApi } from '@/api/services';
import { formaterFCFA } from '@/components/AnnonceCard';
import { useAppSelector } from '@/app/hooks';
import { Link } from 'react-router-dom';
import BarrePublique from '@/components/layout/BarrePublique';
import HistoriquePrix from '@/components/HistoriquePrix';
import CarteLocalisation from '@/components/CarteLocalisation';
import ReputationDemarcheur from '@/components/ReputationDemarcheur';
import BlocContact from '@/components/BlocContact';
import type { Annonce } from '@/types';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BoutonRetour from '@/components/BoutonRetour';
import { imageFiable, serieIllustrations } from '@/utils/illustrations';

export default function AnnonceDetailPage() {
  const [indexPhoto, setIndexPhoto] = useState(0);
  const { id } = useParams();
  const navigate = useNavigate();
  const { login, userId } = useAppSelector((s) => s.auth);
  const [annonce, setAnnonce] = useState<Annonce | null>(null);
  const [date, setDate] = useState('');
  const [message, setMessage] = useState('');
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  useEffect(() => {
    if (!id) return;
    annoncesApi.parId(+id).then(setAnnonce);
    // Compteur de vues : le backend n'incrémente qu'une fois par 24 h et par personne (EF-04.4)
    vueApi.enregistrer(+id);
  }, [id]);

  if (!annonce) {
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  const bien = annonce.immobilier;
  const coloc = annonce.detailColocation;

  /** Bornes de saisie : de maintenant à un an, au format attendu par datetime-local. */
  const pourChampLocal = (d: Date) =>
    new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  const dateMin = pourChampLocal(new Date());
  const dateMax = pourChampLocal(new Date(Date.now() + 365 * 24 * 3600 * 1000));

  const demanderVisite = async () => {
    // Troisième verrou, après l'affichage conditionnel du formulaire et le contrôle
    // du serveur. Il couvre le cas où le jeton expire pendant la navigation : l'état
    // d'authentification n'est calculé qu'au chargement de la page, le formulaire
    // resterait donc visible alors que la session n'est plus valide.
    if (!login || !userId) {
      setRetour({
        type: 'error',
        texte: 'Votre session a expiré. Reconnectez-vous pour envoyer la demande.',
      });
      return;
    }

    // Garde-fou : une saisie comme « 22/07/62026 » donne une date invalide et
    // ferait échouer toISOString(). On prévient l'utilisateur au lieu d'échouer sourdement.
    const souhaitee = new Date(date);
    if (Number.isNaN(souhaitee.getTime())) {
      setRetour({ type: 'error', texte: 'Date invalide. Vérifiez le jour, le mois et l’année.' });
      return;
    }
    if (souhaitee.getTime() < Date.now()) {
      setRetour({ type: 'error', texte: 'La date souhaitée doit être dans le futur.' });
      return;
    }
    if (souhaitee.getTime() > Date.now() + 365 * 24 * 3600 * 1000) {
      setRetour({ type: 'error', texte: 'La date ne peut pas dépasser un an.' });
      return;
    }

    try {
      await rendezVousApi.demander(annonce.id, souhaitee.toISOString(), message);
      setRetour({ type: 'success', texte: 'Demande envoyée. Le propriétaire a été notifié.' });
    } catch (e: unknown) {
      const err = e as { response?: { status?: number; data?: { detail?: string; title?: string } } };
      const msg =
        err?.response?.data?.detail ??
        err?.response?.data?.title ??
        (err?.response?.status
          ? `La demande a été refusée par le serveur (erreur ${err.response.status}).`
          : "Impossible de contacter le serveur. Vérifiez votre connexion.");
      setRetour({ type: 'error', texte: msg });
    }
  };

  const contacter = async () => {
    if (!userId) {
      setRetour({ type: 'error', texte: 'Connectez-vous pour contacter l\'auteur.' });
      return;
    }
    try {
      const conv = await messagerieApi.ouvrirPourAnnonce(annonce!.id);
      navigate(`/messages/${conv.id}`);
    } catch {
      setRetour({ type: 'error', texte: "Impossible d'ouvrir la conversation." });
    }
  };

  const ajouterFavori = async () => {
    // Auparavant, un visiteur non connecté cliquait et il ne se passait rien :
    // la fonction sortait en silence. On l'oriente désormais vers la connexion.
    if (!userId) {
      setRetour({
        type: 'error',
        texte: 'Connectez-vous pour enregistrer cette annonce dans vos favoris.',
      });
      return;
    }
    try {
      await favorisApi.ajouter(annonce.id, userId);
      setRetour({ type: 'success', texte: 'Ajouté à vos favoris.' });
    } catch (e: unknown) {
      // Sans ce filet, un refus du serveur (favori déjà présent, par exemple)
      // ne produisait aucun message : le clic semblait ignoré.
      const err = e as { response?: { status?: number; data?: { detail?: string } } };
      const msg =
        err?.response?.status === 400 || err?.response?.status === 409
          ? 'Cette annonce est déjà dans vos favoris.'
          : err?.response?.data?.detail ?? "Impossible d'ajouter aux favoris pour le moment.";
      setRetour({ type: 'error', texte: msg });
    }
  };

  // Sans photo, on présente l'illustration du type de bien : une galerie vide
  // laissait la fiche sans aucun visuel.
  const photosReelles = (annonce.photos ?? (annonce.photoUrl ? [annonce.photoUrl] : []))
    .filter(imageFiable);
  const photos = photosReelles.length > 0 ? photosReelles : serieIllustrations(annonce);

  return (
    <>
      {/* La barre s'affiche même connecté : cette route est publique et vit hors
          de l'espace authentifié, elle n'hérite donc d'aucune navigation. */}
      <BarrePublique />
      <Box sx={{ px: { xs: 2, md: 3 }, pt: 2, maxWidth: 1200, mx: 'auto' }}>
        <BoutonRetour vers={login ? '/rechercher' : '/annonces-publiques'} libelle="Retour aux annonces" />
      </Box>
      <Grid container spacing={3} sx={{ p: { xs: 2, md: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <Grid item xs={12} md={8}>
        {photos.length > 0 && (
          <Card sx={{ mb: 2, overflow: 'hidden' }}>
            <Box
              component="img"
              src={photos[indexPhoto]}
              alt={annonce.titre}
              sx={{ width: '100%', height: { xs: 220, sm: 380 }, objectFit: 'cover', display: 'block' }}
            />
            {photos.length > 1 && (
              <Stack direction="row" spacing={1} sx={{ p: 1, overflowX: 'auto' }}>
                {photos.map((url, i) => (
                  <Box
                    key={url}
                    component="img"
                    src={url}
                    alt={`Photo ${i + 1}`}
                    onClick={() => setIndexPhoto(i)}
                    sx={{
                      width: 84,
                      height: 60,
                      objectFit: 'cover',
                      borderRadius: 1,
                      cursor: 'pointer',
                      flexShrink: 0,
                      outline: i === indexPhoto ? '2px solid' : 'none',
                      outlineColor: 'primary.main',
                      opacity: i === indexPhoto ? 1 : 0.65,
                      transition: 'opacity .2s',
                      '&:hover': { opacity: 1 },
                    }}
                  />
                ))}
              </Stack>
            )}
          </Card>
        )}

        <Stack direction="row" spacing={1} sx={{ mb: 1 }}>
          <Chip label={annonce.type} color="primary" size="small" />
          <Chip label={annonce.statut} variant="outlined" size="small" />
        </Stack>

        <Typography variant="h4">{annonce.titre}</Typography>
        <Typography variant="h5" color="primary" sx={{ my: 1 }}>
          {formaterFCFA(annonce.prix)}
        </Typography>
        {annonce.auteur?.id != null && <ReputationDemarcheur demarcheurId={annonce.auteur.id} />}
        <Typography color="text.secondary" sx={{ mb: 2 }}>
          {bien?.adresse} — {bien?.quartier?.nom}, {bien?.localite?.nom}
        </Typography>

        <Typography sx={{ whiteSpace: 'pre-line', mb: 3 }}>{annonce.contenu}</Typography>

        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>Caractéristiques</Typography>
            <Grid container spacing={1}>
              {[
                ['Type de bien', bien?.typeImmobilier?.nom],
                ['Surface', bien?.surface ? `${bien.surface} m²` : null],
                ['Chambres', bien?.nombreChambres],
                ['Salles de bain', bien?.nombreSallesBain],
                ['Salons', bien?.nombreSalons],
                ['Garage', bien?.garage ? 'Oui' : 'Non'],
                ['Piscine', bien?.piscine ? 'Oui' : 'Non'],
                ['Meublé', bien?.meuble ? 'Oui' : 'Non'],
              ]
                .filter(([, v]) => v != null && v !== '')
                .map(([k, v]) => (
                  <Grid item xs={6} sm={3} key={String(k)}>
                    <Typography variant="caption" color="text.secondary">{k}</Typography>
                    <Typography variant="body2" fontWeight={600}>{String(v)}</Typography>
                  </Grid>
                ))}
            </Grid>
          </CardContent>
        </Card>

        {bien?.id && <HistoriquePrix immobilierId={bien.id} />}

        <CarteLocalisation
          latitude={bien?.latitude}
          longitude={bien?.longitude}
          libelle={annonce.titre}
          quartier={bien?.quartier?.nom}
          ville={bien?.localite?.nom}
        />

        <Box sx={{ mt: 2 }}>
          <BlocContact />
        </Box>

        {/* Bloc colocation (EF-05) */}
        {annonce.type === 'COLOCATION' && coloc && (
          <Card sx={{ mt: 2 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>La colocation</Typography>
              <Grid container spacing={1.5}>
                {[
                  ['Places', `${coloc.placesRestantes} restante(s) sur ${coloc.nombrePlaces}`],
                  ['Profil recherché', coloc.sexeRecherche],
                  ['Âge', coloc.ageMin && coloc.ageMax ? `${coloc.ageMin} – ${coloc.ageMax} ans` : 'Indifférent'],
                  ['Loyer', formaterFCFA(coloc.loyer)],
                  ['Caution', formaterFCFA(coloc.caution)],
                  ['Charges', formaterFCFA(coloc.charges)],
                ].map(([k, v]) => (
                  <Grid item xs={6} sm={4} key={String(k)}>
                    <Typography variant="caption" color="text.secondary">{k}</Typography>
                    <Typography variant="body2" fontWeight={600}>{String(v)}</Typography>
                  </Grid>
                ))}
              </Grid>

              {coloc.equipements && coloc.equipements.length > 0 && (
                <>
                  <Divider sx={{ my: 2 }} />
                  <Typography variant="subtitle2" gutterBottom>Équipements</Typography>
                  <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                    {coloc.equipements.map((e) => (
                      <Chip key={e.id} label={e.nom} size="small" variant="outlined" />
                    ))}
                  </Stack>
                </>
              )}

              {coloc.reglesDeVie && (
                <>
                  <Divider sx={{ my: 2 }} />
                  <Typography variant="subtitle2" gutterBottom>Règles de vie</Typography>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>{coloc.reglesDeVie}</Typography>
                </>
              )}
            </CardContent>
          </Card>
        )}
      </Grid>

      {/* Colonne d'action */}
      <Grid item xs={12} md={4}>
        <Card sx={{ position: 'sticky', top: 88 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>Demander une visite</Typography>

            {retour && <Alert severity={retour.type} sx={{ mb: 2 }}>{retour.texte}</Alert>}

            {!login ? (
              // Un visiteur doit savoir, avant d'essayer, que réserver suppose un
              // compte. On ne lui présente donc pas un formulaire qu'il ne pourrait
              // pas valider : on lui montre ce que le compte débloque, et l'action
              // principale est la création de compte, puisqu'il n'en a pas encore.
              // « Se connecter » reste accessible en lien, sans dupliquer le bouton
              // déjà présent dans l'en-tête.
              <Stack spacing={1.5}>
                <Alert severity="info">
                  Créez un compte gratuit pour réserver une visite, enregistrer ce bien
                  en favori et échanger avec l'annonceur.
                </Alert>
                <Stack spacing={0.5} sx={{ pl: 0.5 }}>
                  {[
                    'Réserver une visite à la date de votre choix',
                    'Enregistrer vos biens favoris',
                    'Suivre vos rendez-vous et vos paiements',
                  ].map((avantage) => (
                    <Stack key={avantage} direction="row" spacing={1} alignItems="center">
                      <CheckCircleIcon color="success" sx={{ fontSize: 16 }} />
                      <Typography variant="body2" color="text.secondary">{avantage}</Typography>
                    </Stack>
                  ))}
                </Stack>
                <Button
                  component={Link}
                  to={`/inscription?retour=/annonces-publiques/${annonce.id}`}
                  variant="contained"
                  fullWidth
                  size="large"
                >
                  Créer un compte gratuit
                </Button>
                <Typography variant="body2" align="center" color="text.secondary">
                  Déjà inscrit ?{' '}
                  <MuiLink component={Link} to={`/login?retour=/annonces-publiques/${annonce.id}`}>
                    Se connecter
                  </MuiLink>
                </Typography>
              </Stack>
            ) : (
              <Stack spacing={2}>
                <TextField
                  type="datetime-local" label="Date souhaitée" size="small"
                  InputLabelProps={{ shrink: true }}
                  inputProps={{ min: dateMin, max: dateMax }}
                  helperText="Entre aujourd'hui et un an."
                  value={date} onChange={(e) => setDate(e.target.value)}
                />
                <TextField
                  label="Message" size="small" multiline rows={3}
                  value={message} onChange={(e) => setMessage(e.target.value)}
                />
                <Button variant="contained" disabled={!date} onClick={demanderVisite}>
                  Envoyer la demande
                </Button>
                <Button variant="outlined" onClick={ajouterFavori}>
                  Ajouter aux favoris
                </Button>
                {userId != null && annonce.auteur?.id !== userId && (
                  <Button variant="text" startIcon={<ChatIcon />} onClick={contacter}>
                    Contacter
                  </Button>
                )}
              </Stack>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
    </>
  );
}