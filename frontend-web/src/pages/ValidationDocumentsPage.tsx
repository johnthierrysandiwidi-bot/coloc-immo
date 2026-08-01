import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Link, Stack, TextField, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import {documentsApi, ouvrirFichierProtege, messageErreur } from '@/api/services';
import type { DocumentDemarcheur } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

export default function ValidationDocumentsPage() {
  const [docs, setDocs] = useState<DocumentDemarcheur[]>([]);
  const [chargement, setChargement] = useState(true);
  const [refusCible, setRefusCible] = useState<DocumentDemarcheur | null>(null);
  const [motif, setMotif] = useState('');
  const [retour, setRetour] = useState<string | null>(null);

  const recharger = () => {
    setChargement(true);
    documentsApi.enAttente().then(setDocs).finally(() => setChargement(false));
  };

  useEffect(recharger, []);

  const valider = async (d: DocumentDemarcheur) => {
    try {
      await documentsApi.valider(d.id);
      setRetour(`Document validé. ${d.demarcheur?.login ?? 'Le démarcheur'} peut désormais publier.`);
      recharger();
    } catch (e) {
      // Sans ce filet, un refus du serveur laissait le bouton sans effet visible.
      setRetour(messageErreur(e));
    }
  };

  const confirmerRefus = async () => {
    try {
      if (!refusCible || !motif.trim()) return;
      await documentsApi.refuser(refusCible.id, motif);
      setRetour('Document refusé. Le démarcheur a été notifié du motif.');
      setRefusCible(null);
      setMotif('');
      recharger();
    } catch (e) {
      // Sans ce filet, un refus du serveur laissait le bouton sans effet visible.
      setRetour(messageErreur(e));
    }
  };

  const colonnes: GridColDef<DocumentDemarcheur>[] = [
    { field: 'nom', headerName: 'Document', flex: 1, minWidth: 160 },
    {
      field: 'demarcheur',
      headerName: 'Démarcheur',
      width: 160,
      valueGetter: (_v, row) => row.demarcheur?.login ?? '—',
    },
    {
      field: 'url',
      headerName: 'Fichier',
      width: 110,
      sortable: false,
      renderCell: (p) => (
        <Link
          component="button"
          type="button"
          underline="hover"
          onClick={() =>
            ouvrirFichierProtege(p.row.url).catch(() =>
              setRetour("Fichier introuvable ou accès refusé."),
            )
          }
        >
          Ouvrir
        </Link>
      ),
    },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 130,
      renderCell: (p) => <Chip size="small" label={p.row.statut} color="warning" />,
    },
    {
      field: 'actions',
      headerName: 'Décision',
      width: 200,
      sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={1}>
          <Button size="small" variant="contained" onClick={() => valider(p.row)}>
            Valider
          </Button>
          <Button size="small" color="error" onClick={() => setRefusCible(p.row)}>
            Refuser
          </Button>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Validation des documents</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Tant qu'aucun document n'est validé, le démarcheur ne peut pas publier d'annonce.
      </Typography>

      {retour && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour}</Alert>}

      <div style={{ height: 520, width: '100%' }}>
        <DataGrid
          rows={docs}
          columns={colonnes}
          loading={chargement}
          disableRowSelectionOnClick
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          pageSizeOptions={[10, 25]}
        />
      </div>

      {/* Un refus exige un motif (EF-02.3) */}
      <Dialog open={Boolean(refusCible)} onClose={() => setRefusCible(null)} fullWidth maxWidth="sm">
        <DialogTitle>Refuser le document</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus fullWidth multiline rows={3} sx={{ mt: 1 }}
            label="Motif du refus (obligatoire)"
            value={motif}
            onChange={(e) => setMotif(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRefusCible(null)}>Annuler</Button>
          <Button variant="contained" color="error" disabled={!motif.trim()} onClick={confirmerRefus}>
            Confirmer le refus
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
