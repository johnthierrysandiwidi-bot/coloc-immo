import { useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, MenuItem, Stack, TextField, Typography,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import { adminApi } from '@/api/services';
import BoutonRetour from '@/components/BoutonRetour';

const CIBLES = [
  { valeur: 'TOUS' as const, libelle: 'Tous les utilisateurs' },
  { valeur: 'ROLE_PROPRIETAIRE' as const, libelle: 'Tous les propriétaires' },
  { valeur: 'ROLE_DEMARCHEUR' as const, libelle: 'Tous les démarcheurs' },
];

export default function AdminNotificationsPage() {
  const [cible, setCible] = useState<(typeof CIBLES)[number]['valeur']>('TOUS');
  const [titre, setTitre] = useState('');
  const [message, setMessage] = useState('');
  const [envoiEnCours, setEnvoiEnCours] = useState(false);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  const envoyer = async () => {
    if (!titre.trim() || !message.trim()) return;
    setEnvoiEnCours(true);
    setRetour(null);
    try {
      const nb = await adminApi.diffuser(cible, titre.trim(), message.trim());
      setRetour({ type: 'success', texte: `Notification envoyée à ${nb} utilisateur(s).` });
      setTitre('');
      setMessage('');
    } catch {
      setRetour({ type: 'error', texte: "Échec de l'envoi de la diffusion." });
    } finally {
      setEnvoiEnCours(false);
    }
  };

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Diffuser une notification</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Envoie une notification à un groupe entier d'utilisateurs actifs. Chaque destinataire la reçoit
        individuellement dans son espace « Notifications ».
      </Typography>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour.texte}</Alert>
      )}

      <Card sx={{ maxWidth: 560 }}>
        <CardContent>
          <Stack spacing={2}>
            <TextField
              select fullWidth label="Destinataires" value={cible}
              onChange={(e) => setCible(e.target.value as (typeof CIBLES)[number]['valeur'])}
            >
              {CIBLES.map((c) => <MenuItem key={c.valeur} value={c.valeur}>{c.libelle}</MenuItem>)}
            </TextField>
            <TextField fullWidth label="Titre" value={titre} onChange={(e) => setTitre(e.target.value)} />
            <TextField fullWidth multiline rows={4} label="Message" value={message} onChange={(e) => setMessage(e.target.value)} />
            <Button
              variant="contained" startIcon={<SendIcon />} disabled={!titre.trim() || !message.trim() || envoiEnCours}
              onClick={envoyer}
            >
              {envoiEnCours ? 'Envoi en cours...' : 'Envoyer la diffusion'}
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
