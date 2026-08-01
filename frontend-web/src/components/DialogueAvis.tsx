import { useState } from 'react';
import {
  Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle,
  Rating, Stack, TextField, Typography,
} from '@mui/material';
import { avisApi } from '@/api/services';

/**
 * Dialogue de dépôt d'avis après une visite effectuée.
 *
 * N'a de sens que pour un rendez-vous au statut TERMINE : c'est la garantie que la
 * rencontre a eu lieu. Le serveur revérifie cette règle, le dialogue ne fait que
 * l'exposer proprement.
 */
export default function DialogueAvis({
  rendezVousId,
  ouvert,
  onFermer,
  onEnvoye,
}: {
  rendezVousId: number | null;
  ouvert: boolean;
  onFermer: () => void;
  onEnvoye?: () => void;
}) {
  const [note, setNote] = useState<number | null>(5);
  const [commentaire, setCommentaire] = useState('');
  const [erreur, setErreur] = useState<string | null>(null);
  const [envoi, setEnvoi] = useState(false);

  const envoyer = async () => {
    if (rendezVousId == null || !note) return;
    setEnvoi(true);
    setErreur(null);
    try {
      await avisApi.deposer(rendezVousId, note, commentaire.trim());
      onEnvoye?.();
      fermer();
    } catch {
      setErreur("L'envoi a échoué. Cet avis a peut-être déjà été laissé.");
    } finally {
      setEnvoi(false);
    }
  };

  const fermer = () => {
    setNote(5);
    setCommentaire('');
    setErreur(null);
    onFermer();
  };

  return (
    <Dialog open={ouvert} onClose={fermer} maxWidth="xs" fullWidth>
      <DialogTitle>Noter cette visite</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }} alignItems="flex-start">
          <Typography variant="body2" color="text.secondary">
            Votre retour aide les prochains locataires à choisir en confiance.
          </Typography>
          <Rating
            value={note}
            onChange={(_, v) => setNote(v)}
            size="large"
          />
          <TextField
            label="Commentaire (facultatif)"
            value={commentaire}
            onChange={(e) => setCommentaire(e.target.value)}
            fullWidth
            multiline
            minRows={3}
            inputProps={{ maxLength: 1000 }}
          />
          {erreur && <Alert severity="error" sx={{ width: '100%' }}>{erreur}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={fermer}>Annuler</Button>
        <Button variant="contained" onClick={envoyer} disabled={!note || envoi}>
          Envoyer l'avis
        </Button>
      </DialogActions>
    </Dialog>
  );
}
