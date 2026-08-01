import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Alert, Avatar, Box, CircularProgress, Divider, IconButton, List, ListItemButton,
  ListItemText, Paper, Stack, TextField, Typography,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import {
  messagerieApi, type ConversationItem, type MessageItem,
} from '@/api/services';
import { useAppSelector } from '@/app/hooks';
import BoutonRetour from '@/components/BoutonRetour';

/**
 * Messagerie interne : conversations à gauche, fil de discussion à droite.
 *
 * Le fil est rafraîchi par un sondage léger (toutes les dix secondes) plutôt qu'en
 * temps réel : suffisant pour l'usage, sans l'infrastructure WebSocket qu'imposerait
 * le temps réel.
 */
export default function MessageriePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { userId } = useAppSelector((s) => s.auth);

  const [conversations, setConversations] = useState<ConversationItem[]>([]);
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [saisie, setSaisie] = useState('');
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState<string | null>(null);
  const finRef = useRef<HTMLDivElement>(null);

  const conversationActive = id ? Number(id) : null;

  useEffect(() => {
    messagerieApi
      .conversations()
      .then(setConversations)
      .catch(() => setErreur('Impossible de charger vos conversations.'))
      .finally(() => setChargement(false));
  }, []);

  useEffect(() => {
    if (conversationActive == null) return;
    let actif = true;
    const charger = () =>
      messagerieApi
        .messages(conversationActive)
        .then((m) => actif && setMessages(m))
        .catch(() => actif && setErreur('Impossible de charger les messages.'));
    charger();
    const t = setInterval(charger, 10000);
    return () => {
      actif = false;
      clearInterval(t);
    };
  }, [conversationActive]);

  useEffect(() => {
    finRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const envoyer = async () => {
    if (!saisie.trim() || conversationActive == null) return;
    const texte = saisie.trim();
    setSaisie('');
    try {
      const m = await messagerieApi.envoyer(conversationActive, texte);
      setMessages((prev) => [...prev, m]);
    } catch {
      setErreur("Le message n'a pas pu être envoyé.");
      setSaisie(texte);
    }
  };

  if (chargement) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 2 }}>
      <BoutonRetour />
      <Typography variant="h4" sx={{ mb: 2 }}>Messages</Typography>
      {erreur && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setErreur(null)}>{erreur}</Alert>}

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ height: '70vh' }}>
        <Paper sx={{ width: { xs: '100%', md: 320 }, overflow: 'auto' }}>
          {conversations.length === 0 ? (
            <Typography color="text.secondary" sx={{ p: 2 }}>
              Aucune conversation pour le moment.
            </Typography>
          ) : (
            <List disablePadding>
              {conversations.map((c) => (
                <ListItemButton
                  key={c.id}
                  selected={c.id === conversationActive}
                  onClick={() => navigate(`/messages/${c.id}`)}
                >
                  <Avatar sx={{ mr: 1.5, width: 36, height: 36 }}>
                    {(c.interlocuteurLogin ?? '?').charAt(0).toUpperCase()}
                  </Avatar>
                  <ListItemText
                    primary={c.interlocuteurLogin ?? 'Interlocuteur'}
                    secondary={c.annonceTitre}
                    primaryTypographyProps={{ noWrap: true }}
                    secondaryTypographyProps={{ noWrap: true }}
                  />
                </ListItemButton>
              ))}
            </List>
          )}
        </Paper>

        <Paper sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          {conversationActive == null ? (
            <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Typography color="text.secondary">Choisissez une conversation.</Typography>
            </Box>
          ) : (
            <>
              <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
                {messages.map((m) => {
                  const deMoi = m.expediteurId === userId;
                  return (
                    <Box
                      key={m.id}
                      sx={{ display: 'flex', justifyContent: deMoi ? 'flex-end' : 'flex-start', mb: 1 }}
                    >
                      <Box
                        sx={{
                          maxWidth: '72%',
                          px: 1.5,
                          py: 1,
                          borderRadius: 2,
                          bgcolor: deMoi ? 'primary.main' : 'grey.100',
                          color: deMoi ? 'primary.contrastText' : 'text.primary',
                        }}
                      >
                        <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>{m.contenu}</Typography>
                        <Typography
                          variant="caption"
                          sx={{ opacity: 0.7, display: 'block', textAlign: 'right', mt: 0.25 }}
                        >
                          {new Date(m.dateEnvoi).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                        </Typography>
                      </Box>
                    </Box>
                  );
                })}
                <div ref={finRef} />
              </Box>
              <Divider />
              <Stack direction="row" spacing={1} sx={{ p: 1.5 }}>
                <TextField
                  fullWidth
                  size="small"
                  placeholder="Votre message…"
                  value={saisie}
                  onChange={(e) => setSaisie(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      envoyer();
                    }
                  }}
                  multiline
                  maxRows={4}
                />
                <IconButton color="primary" onClick={envoyer} disabled={!saisie.trim()}>
                  <SendIcon />
                </IconButton>
              </Stack>
            </>
          )}
        </Paper>
      </Stack>
    </Box>
  );
}
