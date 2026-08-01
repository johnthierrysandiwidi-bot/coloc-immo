import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  ImageList, ImageListItem, InputAdornment, MenuItem, Stack, TextField, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import SearchIcon from '@mui/icons-material/Search';
import PhotoLibraryIcon from '@mui/icons-material/PhotoLibrary';
import DeleteIcon from '@mui/icons-material/Delete';
import ArchiveIcon from '@mui/icons-material/Archive';
import UnarchiveIcon from '@mui/icons-material/Unarchive';
import { biensApi } from '@/api/services';
import type { Immobilier } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

const STATUTS = ['BROUILLON', 'DISPONIBLE', 'PARTIELLEMENT_LOUE', 'LOUE', 'ARCHIVE'] as const;

const COULEUR_STATUT: Record<string, 'default' | 'success' | 'warning' | 'info' | 'error'> = {
  BROUILLON: 'default',
  DISPONIBLE: 'success',
  PARTIELLEMENT_LOUE: 'warning',
  LOUE: 'info',
  ARCHIVE: 'error',
};

export default function AdminBiensPage() {
  const [biens, setBiens] = useState<Immobilier[]>([]);
  const [chargement, setChargement] = useState(true);
  const [recherche, setRecherche] = useState('');
  const [statutFiltre, setStatutFiltre] = useState('');
  const [photosCible, setPhotosCible] = useState<Immobilier | null>(null);
  const [suppressionCible, setSuppressionCible] = useState<Immobilier | null>(null);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  const recharger = () => {
    setChargement(true);
    biensApi
      .tousAdmin({ nom: recherche || undefined, statut: statutFiltre || undefined })
      .then(setBiens)
      .catch(() => setRetour({ type: 'error', texte: 'Impossible de charger les biens.' }))
      .finally(() => setChargement(false));
  };

  useEffect(recharger, []); // eslint-disable-line react-hooks/exhaustive-deps

  const changerStatut = async (bien: Immobilier, statut: string) => {
    try {
      await biensApi.changerStatut(bien.id, statut);
      setRetour({ type: 'success', texte: `Statut de « ${bien.nom} » mis à jour.` });
      recharger();
    } catch {
      setRetour({ type: 'error', texte: 'Échec de la mise à jour du statut.' });
    }
  };

  const confirmerSuppression = async () => {
    if (!suppressionCible) return;
    try {
      await biensApi.supprimer(suppressionCible.id);
      setRetour({ type: 'success', texte: `« ${suppressionCible.nom} » supprimé.` });
      setSuppressionCible(null);
      recharger();
    } catch {
      setRetour({ type: 'error', texte: 'Suppression impossible (bien lié à une annonce ?).' });
    }
  };

  const colonnes: GridColDef<Immobilier>[] = [
    { field: 'nom', headerName: 'Bien', flex: 1, minWidth: 160 },
    {
      field: 'typeImmobilier',
      headerName: 'Type',
      width: 130,
      valueGetter: (_v, row) => row.typeImmobilier?.nom ?? '—',
    },
    {
      field: 'localite',
      headerName: 'Localité',
      width: 150,
      valueGetter: (_v, row) => row.localite?.nom ?? '—',
    },
    { field: 'surface', headerName: 'Surface (m²)', width: 110 },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 180,
      renderCell: (p) => (
        <TextField
          select size="small" variant="standard" value={p.row.statut ?? 'BROUILLON'}
          onChange={(e) => changerStatut(p.row, e.target.value)}
          sx={{ minWidth: 150 }}
        >
          {STATUTS.map((s) => (
            <MenuItem key={s} value={s}>
              <Chip size="small" label={s} color={COULEUR_STATUT[s]} />
            </MenuItem>
          ))}
        </TextField>
      ),
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 200,
      sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5}>
          <Button size="small" startIcon={<PhotoLibraryIcon />} onClick={() => setPhotosCible(p.row)}>
            Photos
          </Button>
          {p.row.statut === 'ARCHIVE' ? (
            <Button size="small" color="success" startIcon={<UnarchiveIcon />} onClick={() => changerStatut(p.row, 'DISPONIBLE')}>
              Restaurer
            </Button>
          ) : (
            <Button size="small" color="warning" startIcon={<ArchiveIcon />} onClick={() => changerStatut(p.row, 'ARCHIVE')}>
              Archiver
            </Button>
          )}
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
      <Typography variant="h4" gutterBottom>Biens immobiliers</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Tous les biens publiés par les propriétaires et démarcheurs de la plateforme.
      </Typography>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour.texte}</Alert>
      )}

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          size="small" placeholder="Rechercher un bien..." value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && recharger()}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }}
          sx={{ minWidth: 240 }}
        />
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
            rows={biens} columns={colonnes} loading={chargement} disableRowSelectionOnClick
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            pageSizeOptions={[10, 25, 50]}
          />
        </div>
      </Card>

      <Dialog open={Boolean(photosCible)} onClose={() => setPhotosCible(null)} fullWidth maxWidth="sm">
        <DialogTitle>Photos — {photosCible?.nom}</DialogTitle>
        <DialogContent>
          {photosCible?.images?.length ? (
            <ImageList cols={3} gap={8}>
              {photosCible.images.map((img) => (
                <ImageListItem key={img.id}>
                  <img src={img.url} alt={photosCible.nom} loading="lazy" style={{ borderRadius: 8 }} />
                </ImageListItem>
              ))}
            </ImageList>
          ) : (
            <Typography color="text.secondary">Aucune photo pour ce bien.</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPhotosCible(null)}>Fermer</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(suppressionCible)} onClose={() => setSuppressionCible(null)} fullWidth maxWidth="xs">
        <DialogTitle>Supprimer ce bien ?</DialogTitle>
        <DialogContent>
          <Typography>
            « {suppressionCible?.nom} » sera définitivement supprimé. Cette action est irréversible.
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
