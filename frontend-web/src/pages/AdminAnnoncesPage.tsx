import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  InputAdornment, MenuItem, Stack, TextField, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import SearchIcon from '@mui/icons-material/Search';
import PublishIcon from '@mui/icons-material/Publish';
import UnpublishedIcon from '@mui/icons-material/Unpublished';
import ArchiveIcon from '@mui/icons-material/Archive';
import RefreshIcon from '@mui/icons-material/Refresh';
import DeleteIcon from '@mui/icons-material/Delete';
import {annoncesApi, messageErreur } from '@/api/services';
import type { Annonce } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

const TYPES = ['VENTE', 'LOCATION', 'COLOCATION'] as const;
const STATUTS = ['BROUILLON', 'PUBLIEE', 'SUSPENDUE', 'EXPIREE', 'CLOTUREE'] as const;

const COULEUR_STATUT: Record<string, 'default' | 'success' | 'warning' | 'info' | 'error'> = {
  BROUILLON: 'default',
  PUBLIEE: 'success',
  SUSPENDUE: 'warning',
  EXPIREE: 'error',
  CLOTUREE: 'info',
};

const fcfa = (n: number) => new Intl.NumberFormat('fr-FR').format(n) + ' F';

export default function AdminAnnoncesPage() {
  const [annonces, setAnnonces] = useState<Annonce[]>([]);
  const [chargement, setChargement] = useState(true);
  const [titre, setTitre] = useState('');
  const [type, setType] = useState('');
  const [statut, setStatut] = useState('');
  const [suppressionCible, setSuppressionCible] = useState<Annonce | null>(null);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  const recharger = () => {
    setChargement(true);
    annoncesApi
      .toutesAdmin({ titre: titre || undefined, type: type || undefined, statut: statut || undefined })
      .then(setAnnonces)
      .catch(() => setRetour({ type: 'error', texte: 'Impossible de charger les annonces.' }))
      .finally(() => setChargement(false));
  };

  useEffect(recharger, []); // eslint-disable-line react-hooks/exhaustive-deps

  const agir = async (action: () => Promise<unknown>, texte: string) => {
    try {
      await action();
      setRetour({ type: 'success', texte });
      recharger();
    } catch {
      setRetour({ type: 'error', texte: "Échec de l'opération." });
    }
  };

  const confirmerSuppression = async () => {
    try {
      if (!suppressionCible) return;
      await agir(() => annoncesApi.supprimer(suppressionCible.id), `« ${suppressionCible.titre} » supprimée.`);
      setSuppressionCible(null);
    } catch (e) {
      // Sans ce filet, un refus du serveur laissait le bouton sans effet visible.
      setRetour({ type: 'error', texte: messageErreur(e) });
    }
  };

  const colonnes: GridColDef<Annonce>[] = [
    { field: 'titre', headerName: 'Annonce', flex: 1, minWidth: 180 },
    { field: 'type', headerName: 'Type', width: 110 },
    { field: 'prix', headerName: 'Prix', width: 120, valueFormatter: (v) => fcfa(Number(v)) },
    { field: 'nombreVues', headerName: 'Vues', width: 80 },
    {
      field: 'auteur',
      headerName: 'Auteur',
      width: 140,
      valueGetter: (_v, row) => row.auteur?.login ?? '—',
    },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 120,
      renderCell: (p) => <Chip size="small" label={p.row.statut} color={COULEUR_STATUT[p.row.statut]} />,
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 260,
      sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5}>
          {p.row.statut === 'PUBLIEE' ? (
            <Button size="small" startIcon={<UnpublishedIcon />} onClick={() => agir(() => annoncesApi.depublier(p.row.id), 'Annonce dépubliée.')}>
              Dépublier
            </Button>
          ) : p.row.statut !== 'CLOTUREE' && (
            <Button size="small" startIcon={<PublishIcon />} onClick={() => agir(() => annoncesApi.publier(p.row.id), 'Annonce publiée.')}>
              Publier
            </Button>
          )}
          {p.row.statut === 'EXPIREE' && (
            <Button size="small" color="success" startIcon={<RefreshIcon />} onClick={() => agir(() => annoncesApi.renouveler(p.row.id), 'Annonce renouvelée.')}>
              Renouveler
            </Button>
          )}
          <Button size="small" color="warning" startIcon={<ArchiveIcon />} onClick={() => agir(() => annoncesApi.archiver(p.row.id), 'Annonce archivée.')}>
            Archiver
          </Button>
          <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => setSuppressionCible(p.row)}>
            Suppr.
          </Button>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Annonces</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Toutes les annonces de la plateforme, quel que soit leur statut.
      </Typography>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour.texte}</Alert>
      )}

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          size="small" placeholder="Rechercher une annonce..." value={titre}
          onChange={(e) => setTitre(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && recharger()}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }}
          sx={{ minWidth: 240 }}
        />
        <TextField select size="small" label="Type" value={type} onChange={(e) => setType(e.target.value)} sx={{ minWidth: 150 }}>
          <MenuItem value="">Tous</MenuItem>
          {TYPES.map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
        </TextField>
        <TextField select size="small" label="Statut" value={statut} onChange={(e) => setStatut(e.target.value)} sx={{ minWidth: 160 }}>
          <MenuItem value="">Tous</MenuItem>
          {STATUTS.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
        </TextField>
        <Button variant="contained" onClick={recharger}>Filtrer</Button>
      </Stack>

      <Card>
        <div style={{ height: 560, width: '100%' }}>
          <DataGrid
            rows={annonces} columns={colonnes} loading={chargement} disableRowSelectionOnClick
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            pageSizeOptions={[10, 25, 50]}
          />
        </div>
      </Card>

      <Dialog open={Boolean(suppressionCible)} onClose={() => setSuppressionCible(null)} fullWidth maxWidth="xs">
        <DialogTitle>Supprimer cette annonce ?</DialogTitle>
        <DialogContent>
          <Typography>
            « {suppressionCible?.titre} » sera définitivement supprimée. Cette action est irréversible.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSuppressionCible(null)}>Annuler</Button>
          <Button variant="contained" color="error" onClick={confirmerSuppression}>Supprimer</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
