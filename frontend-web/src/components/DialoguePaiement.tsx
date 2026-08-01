import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle,
  Divider, InputAdornment, MenuItem, Stack, TextField, Typography,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import DownloadIcon from '@mui/icons-material/Download';
import LockIcon from '@mui/icons-material/Lock';
import PhoneIphoneIcon from '@mui/icons-material/PhoneIphone';
import { messageErreur, paiementApi, type Paiement } from '@/api/services';
import { telechargerRecuPaiement } from '@/utils/recuPdf';

const MOYENS = [
  { valeur: 'ORANGE_MONEY', libelle: 'Orange Money', mobile: true, prefixes: ['5', '6', '7'] },
  { valeur: 'MOOV_MONEY', libelle: 'Moov Money', mobile: true, prefixes: ['0', '1', '5', '6'] },
  { valeur: 'CARTE', libelle: 'Carte bancaire', mobile: false, prefixes: [] },
];

const formaterMontant = (m: number) => `${new Intl.NumberFormat('fr-FR').format(m)} FCFA`;

interface Props {
  rendezVousId: number | null;
  ouvert: boolean;
  onFermer: () => void;
  onPaye: () => void;
}

/**
 * Règlement des frais de visite — passerelle SIMULÉE.
 *
 * Le parcours se fait en trois temps (saisie, traitement, reçu) au lieu d'un seul
 * clic : l'ancienne version fermait la fenêtre dès la réponse du serveur, sans
 * jamais montrer la référence du paiement, seule preuve dont dispose l'utilisateur
 * en cas de litige sur la visite.
 */
export default function DialoguePaiement({ rendezVousId, ouvert, onFermer, onPaye }: Props) {
  const [frais, setFrais] = useState<number | null>(null);
  const [moyen, setMoyen] = useState('ORANGE_MONEY');
  const [telephone, setTelephone] = useState('');
  const [etape, setEtape] = useState<'saisie' | 'traitement' | 'recu'>('saisie');
  const [recu, setRecu] = useState<Paiement | null>(null);
  const [erreur, setErreur] = useState<string | null>(null);

  const moyenChoisi = MOYENS.find((m) => m.valeur === moyen)!;

  useEffect(() => {
    if (!ouvert) return;
    setEtape('saisie');
    setErreur(null);
    setRecu(null);
    setTelephone('');
    // Le montant vient du serveur (RG23) ; on n'en code aucun en dur, sans quoi
    // l'écran pourrait afficher un tarif obsolète.
    paiementApi
      .fraisDeVisite()
      .then(setFrais)
      .catch(() => setErreur('Impossible de récupérer le montant des frais.'));
  }, [ouvert]);

  /** Numéro burkinabè : 8 chiffres, préfixe cohérent avec l'opérateur choisi. */
  const telephoneValide = (() => {
    if (!moyenChoisi.mobile) return true;
    const chiffres = telephone.replace(/\s/g, '');
    return /^[0-9]{8}$/.test(chiffres) && moyenChoisi.prefixes.includes(chiffres[0]);
  })();

  const payer = async () => {
    if (!rendezVousId || !telephoneValide) return;
    setEtape('traitement');
    setErreur(null);
    try {
      const paiement = await paiementApi.initier(rendezVousId);
      const regle = await paiementApi.simuler(paiement.id, moyen);
      setRecu(regle);
      setEtape('recu');
      onPaye();
    } catch (e) {
      setErreur(messageErreur(e, 'Le paiement a échoué.'));
      setEtape('saisie');
    }
  };

  const fermerDepuisRecu = () => {
    onFermer();
  };

  return (
    <Dialog open={ouvert} onClose={etape === 'traitement' ? undefined : onFermer} maxWidth="xs" fullWidth>
      <DialogTitle>
        {etape === 'recu' ? 'Paiement confirmé' : 'Régler les frais de visite'}
      </DialogTitle>

      <DialogContent>
        {/* ---------- Étape 1 : saisie ---------- */}
        {etape === 'saisie' && (
          <Stack spacing={2}>
            <Alert severity="info" icon={<LockIcon fontSize="small" />}>
              Ces frais sont conservés en séquestre. Ils sont versés au démarcheur après
              la visite, ou intégralement remboursés si elle n'a pas lieu.
            </Alert>

            <Box textAlign="center" py={1}>
              {frais === null ? (
                <CircularProgress size={28} />
              ) : (
                <Typography variant="h4" color="primary" fontWeight={800}>
                  {formaterMontant(frais)}
                </Typography>
              )}
            </Box>

            <TextField
              select
              label="Moyen de paiement"
              value={moyen}
              onChange={(e) => {
                setMoyen(e.target.value);
                setTelephone('');
              }}
              fullWidth
            >
              {MOYENS.map((m) => (
                <MenuItem key={m.valeur} value={m.valeur}>{m.libelle}</MenuItem>
              ))}
            </TextField>

            {moyenChoisi.mobile && (
              <TextField
                label={`Numéro ${moyenChoisi.libelle}`}
                value={telephone}
                onChange={(e) => setTelephone(e.target.value.replace(/[^0-9\s]/g, ''))}
                fullWidth
                placeholder="70 12 34 56"
                error={telephone.length > 0 && !telephoneValide}
                helperText={
                  telephone.length > 0 && !telephoneValide
                    ? '8 chiffres, commençant par ' + moyenChoisi.prefixes.join(', ')
                    : 'Le numéro qui recevra la demande de confirmation.'
                }
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <PhoneIphoneIcon fontSize="small" />
                      <Typography variant="body2" sx={{ ml: 0.5 }}>+226</Typography>
                    </InputAdornment>
                  ),
                }}
              />
            )}

            {erreur && <Alert severity="error">{erreur}</Alert>}

            <Typography variant="caption" color="text.secondary">
              Démonstration : aucun montant réel n'est débité et le numéro saisi n'est pas
              transmis à l'opérateur.
            </Typography>
          </Stack>
        )}

        {/* ---------- Étape 2 : traitement ---------- */}
        {etape === 'traitement' && (
          <Stack spacing={2} alignItems="center" py={3}>
            <CircularProgress />
            <Typography align="center">
              {moyenChoisi.mobile
                ? `Demande envoyée au ${moyenChoisi.libelle}…`
                : 'Autorisation de la carte en cours…'}
            </Typography>
            <Typography variant="body2" color="text.secondary" align="center">
              En conditions réelles, vous confirmeriez l'opération sur votre téléphone.
            </Typography>
          </Stack>
        )}

        {/* ---------- Étape 3 : reçu ---------- */}
        {etape === 'recu' && recu && (
          <Stack spacing={2}>
            <Box textAlign="center" py={1}>
              <CheckCircleIcon color="success" sx={{ fontSize: 56 }} />
              <Typography variant="h5" fontWeight={700} mt={1}>
                {formaterMontant(recu.montant)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Somme placée en séquestre
              </Typography>
            </Box>

            <Divider />

            <Stack spacing={1}>
              <Ligne libelle="Référence" valeur={recu.reference} />
              <Ligne
                libelle="Moyen"
                valeur={MOYENS.find((m) => m.valeur === recu.moyen)?.libelle ?? recu.moyen ?? '—'}
              />
              {recu.annonceTitre && <Ligne libelle="Annonce" valeur={recu.annonceTitre} />}
            </Stack>

            <Alert severity="success">
              Conservez la référence : elle sert de preuve en cas de contestation sur la visite.
            </Alert>

            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={() => telechargerRecuPaiement(recu)}
              fullWidth
            >
              Télécharger le reçu (PDF)
            </Button>
          </Stack>
        )}
      </DialogContent>

      <DialogActions>
        {etape === 'saisie' && (
          <>
            <Button onClick={onFermer}>Annuler</Button>
            <Button
              variant="contained"
              onClick={payer}
              disabled={frais === null || !telephoneValide}
            >
              Payer {frais !== null ? formaterMontant(frais) : ''}
            </Button>
          </>
        )}
        {etape === 'recu' && (
          <Button variant="contained" onClick={fermerDepuisRecu}>Terminer</Button>
        )}
      </DialogActions>
    </Dialog>
  );
}

function Ligne({ libelle, valeur }: { libelle: string; valeur: string }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2}>
      <Typography variant="body2" color="text.secondary">{libelle}</Typography>
      <Typography variant="body2" fontWeight={600} sx={{ textAlign: 'right' }}>{valeur}</Typography>
    </Stack>
  );
}
