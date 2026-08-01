import { useState } from 'react';
import {
  Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle,
  Stack, TextField,
} from '@mui/material';
import { api } from '@/api/client';
import { messageErreur } from '@/api/services';
import { useAppSelector } from '@/app/hooks';

/**
 * Dialogue « Contacter l'administrateur ».
 *
 * Accessible depuis l'en-tête public, y compris sans compte : un visiteur bloqué
 * à l'inscription ou à la connexion doit pouvoir joindre l'équipe. Le message est
 * transmis aux administrateurs sous forme de notification interne.
 */
export default function DialogueContact({
  ouvert,
  onFermer,
}: {
  ouvert: boolean;
  onFermer: () => void;
}) {
  const { login } = useAppSelector((s) => s.auth);
  const [sujet, setSujet] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [etat, setEtat] = useState<'saisie' | 'envoye'>('saisie');
  const [erreur, setErreur] = useState<string | null>(null);
  const [envoi, setEnvoi] = useState(false);

  // Un visiteur non connecté doit laisser un moyen de le recontacter.
  const emailValide = login != null || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const pretAEnvoyer = sujet.trim().length > 0 && message.trim().length > 0 && emailValide;

  const envoyer = async () => {
    setEnvoi(true);
    setErreur(null);
    try {
      await api.post('/contact', { sujet, email: login ? undefined : email, message });
      setEtat('envoye');
    } catch (e) {
      setErreur(messageErreur(e, "L'envoi a échoué. Réessayez dans un instant."));
    } finally {
      setEnvoi(false);
    }
  };

  const fermer = () => {
    setEtat('saisie');
    setSujet('');
    setEmail('');
    setMessage('');
    setErreur(null);
    onFermer();
  };

  return (
    <Dialog open={ouvert} onClose={fermer} maxWidth="sm" fullWidth>
      <DialogTitle>Contacter l'administrateur</DialogTitle>

      {etat === 'saisie' ? (
        <>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <Alert severity="info">
                Une question, un signalement ? Écrivez-nous : l'équipe reçoit votre
                message directement.
              </Alert>

              <Alert severity="success" icon={false}>
                Vous préférez appeler ? <strong>+226 54 56 40 01</strong> ou{' '}
                <strong>+226 71 49 05 08</strong>
              </Alert>

              <TextField
                label="Sujet"
                value={sujet}
                onChange={(e) => setSujet(e.target.value)}
                fullWidth
                required
              />

              {!login && (
                <TextField
                  label="Votre adresse électronique"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  fullWidth
                  required
                  error={email.length > 0 && !emailValide}
                  helperText={
                    email.length > 0 && !emailValide
                      ? 'Adresse invalide.'
                      : "Pour que l'équipe puisse vous répondre."
                  }
                />
              )}

              <TextField
                label="Votre message"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                fullWidth
                required
                multiline
                minRows={4}
              />

              {erreur && <Alert severity="error">{erreur}</Alert>}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={fermer}>Annuler</Button>
            <Button variant="contained" onClick={envoyer} disabled={!pretAEnvoyer || envoi}>
              Envoyer
            </Button>
          </DialogActions>
        </>
      ) : (
        <>
          <DialogContent>
            <Alert severity="success">
              Message envoyé. L'équipe vous répondra
              {login ? ' via vos notifications.' : ' à l\'adresse indiquée.'}
            </Alert>
          </DialogContent>
          <DialogActions>
            <Button variant="contained" onClick={fermer}>Fermer</Button>
          </DialogActions>
        </>
      )}
    </Dialog>
  );
}
