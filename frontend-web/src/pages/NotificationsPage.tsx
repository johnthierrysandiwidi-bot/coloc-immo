import { useEffect, useMemo, useState } from 'react';
import {
  Box, Button, Card, Chip, IconButton, List, ListItem, ListItemButton, ListItemText, Stack,
  Tooltip, Typography,
} from '@mui/material';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import { useNavigate } from 'react-router-dom';
import { notificationsApi } from '@/api/services';
import type { NotificationItem } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

export default function NotificationsPage() {
  const [notifs, setNotifs] = useState<NotificationItem[]>([]);
  const navigate = useNavigate();

  const charger = () =>
    notificationsApi.mesNotifications().then(setNotifs).catch(() => setNotifs([]));
  useEffect(() => { charger(); }, []);

  const nonLues = useMemo(() => notifs.filter((n) => !n.lue).length, [notifs]);

  const ouvrir = async (n: NotificationItem) => {
    if (!n.lue) {
      await notificationsApi.marquerLue(n);
      setNotifs((prev) => prev.map((x) => (x.id === n.id ? { ...x, lue: true } : x)));
    }
    if (n.lien) navigate(n.lien);
  };

  const toutMarquer = async () => {
    try {
      await notificationsApi.marquerToutesLues();
    } catch {
      // Repli si l'endpoint groupé n'est pas déployé : on marque une par une.
      await Promise.all(notifs.filter((n) => !n.lue).map((n) => notificationsApi.marquerLue(n)));
    }
    setNotifs((prev) => prev.map((x) => ({ ...x, lue: true })));
  };

  const supprimer = async (id: number) => {
    await notificationsApi.supprimer(id);
    setNotifs((prev) => prev.filter((n) => n.id !== id));
  };

  return (
    <Box>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <BoutonRetour />
        <Typography variant="h4">Notifications</Typography>
        <Button
          startIcon={<DoneAllIcon />}
          onClick={toutMarquer}
          disabled={nonLues === 0}
        >
          Tout marquer comme lu{nonLues > 0 ? ` (${nonLues})` : ''}
        </Button>
      </Stack>

      <Card>
        <List disablePadding>
          {notifs.length === 0 && (
            <ListItem>
              <ListItemText secondary="Aucune notification." />
            </ListItem>
          )}
          {notifs.map((n) => (
            <ListItem
              key={n.id}
              disablePadding
              secondaryAction={
                <Tooltip title="Supprimer">
                  <IconButton edge="end" size="small" color="error" onClick={() => supprimer(n.id)}>
                    <DeleteOutlineIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              }
            >
              <ListItemButton onClick={() => ouvrir(n)} sx={{ opacity: n.lue ? 0.6 : 1, pr: 8 }}>
                <ListItemText
                  primary={n.titre}
                  secondary={n.message}
                  primaryTypographyProps={{ fontWeight: n.lue ? 400 : 600 }}
                />
                {!n.lue && <Chip size="small" label="Nouveau" color="primary" sx={{ ml: 1 }} />}
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Card>
    </Box>
  );
}
