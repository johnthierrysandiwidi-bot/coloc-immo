import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Stack, TextField, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import EventBusyIcon from '@mui/icons-material/EventBusy';
import { rendezVousApi } from '@/api/services';
import type { RendezVous } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

const STATUTS = ['DEMANDE', 'ACCEPTE', 'REFUSE', 'REPORTE', 'ANNULE', 'TERMINE'] as const;

const COULEUR_STATUT: Record<string, 'default' | 'success' | 'warning' | 'info' | 'error'> = {
  DEMANDE: 'info',
  ACCEPTE: 'success',
  REFUSE: 'error',
  REPORTE: 'warning',
  ANNULE: 'default',
  TERMINE: 'default',
};

export default function AdminRendezVousPage() {
  const [rdv, setRdv] = useState<RendezVous[]>([]);
  const [chargement, setChargement] = useState(true);
  const [statutFiltre, setStatutFiltre] = useState('');
  const [annulationCible, setAnnulationCible] = useState<RendezVous | null>(null);
  const [motif, setMotif] = useState('');
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  const recharger = () => {
    setChargement(true);
    rendezVousApi
      .tousAdmin({ statut: statutFiltre || undefined })
      .then(setRdv)
      .catch(() => setRetour({ type: 'error', texte: 'Impossible de charger les rendez-vous.' }))
      .finally(() => setChargement(false));
  };

  useEffect(recharger, []); // eslint-disable-line react-hooks/exhaustive-deps

  const confirmerAnnulation = async () => {
    if (!annulationCible) return;
    try {
      await rendezVousApi.annuler(annulationCible.id, motif || undefined);
      setRetour({ type: 'success', texte: 'Rendez-vous annulé.' });
      setAnnulationCible(null);
      setMotif('');
      recharger();
    } catch {
      setRetour({ type: 'error', texte: "Échec de l'annulation." });
    }
  };

  const colonnes: GridColDef<RendezVous>[] = [
    {
      field: 'dateHeure',
      headerName: 'Date / heure',
      width: 170,
      valueFormatter: (v) => (v ? new Date(v as string).toLocaleString('fr-FR') : '—'),
    },
    {
      field: 'annonce',
      headerName: 'Annonce',
      flex: 1,
      minWidth: 160,
      valueGetter: (_v, row) => row.annonce?.titre ?? '—',
    },
    {
      field: 'demandeur',
      headerName: 'Demandeur',
      width: 130,
      valueGetter: (_v, row) => row.demandeur?.login ?? '—',
    },
    {
      // Un rendez-vous met deux personnes en présence. Seul le demandeur était
      // affiché : impossible pour l'administration de savoir qui doit honorer la
      // visite, ni de trancher un litige.
      field: 'auteur',
      headerName: 'Annonceur',
      width: 130,
      valueGetter: (_v, row) => row.annonce?.auteur?.login ?? '—',
    },
    {
      // Le report était invisible : la colonne « Date / heure » montrait toujours
      // la date d'origine, même pour un rendez-vous déplacé.
      field: 'dateReportee',
      headerName: 'Reporté au',
      width: 150,
      valueGetter: (_v, row) => row.dateReportee,
      valueFormatter: (v) => (v ? new Date(v as string).toLocaleString('fr-FR') : '—'),
    },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 130,
      renderCell: (p) => <Chip size="small" label={p.row.statut} color={COULEUR_STATUT[p.row.statut]} />,
    },
    { field: 'motif', headerName: 'Motif', flex: 1, minWidth: 140 },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 130,
      sortable: false,
      renderCell: (p) =>
        !['ANNULE', 'TERMINE', 'REFUSE'].includes(p.row.statut) && (
          <Button size="small" color="error" startIcon={<EventBusyIcon />} onClick={() => setAnnulationCible(p.row)}>
            Annuler
          </Button>
        ),
    },
  ];

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Rendez-vous</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Tous les rendez-vous demandés sur la plateforme.
      </Typography>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour.texte}</Alert>
      )}

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          select size="small" label="Statut" value={statutFiltre}
          onChange={(e) => setStatutFiltre(e.target.value)}
          sx={{ minWidth: 180 }}
        >
          <MenuItem value="">Tous</MenuItem>
          {STATUTS.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
        </TextField>
        <Button variant="contained" onClick={recharger}>Filtrer</Button>
      </Stack>

      <Card>
        <div style={{ height: 560, width: '100%' }}>
          <DataGrid
            rows={rdv} columns={colonnes} loading={chargement} disableRowSelectionOnClick
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            pageSizeOptions={[10, 25, 50]}
          />
        </div>
      </Card>

      <Dialog open={Boolean(annulationCible)} onClose={() => setAnnulationCible(null)} fullWidth maxWidth="sm">
        <DialogTitle>Annuler ce rendez-vous ?</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus fullWidth multiline rows={2} sx={{ mt: 1 }}
            label="Motif (optionnel)" value={motif} onChange={(e) => setMotif(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAnnulationCible(null)}>Retour</Button>
          <Button variant="contained" color="error" onClick={confirmerAnnulation}>Confirmer l'annulation</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
