import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Stack, Tab, Tabs, TextField, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { referentielApi } from '@/api/services';
import type { Localite, Quartier } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

export default function AdminVillesQuartiersPage() {
  const [onglet, setOnglet] = useState(0);
  const [localites, setLocalites] = useState<Localite[]>([]);
  const [quartiers, setQuartiers] = useState<Quartier[]>([]);
  const [chargement, setChargement] = useState(true);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  // Dialogue Ville
  const [dialogVille, setDialogVille] = useState<{ mode: 'creer' | 'modifier'; item?: Localite } | null>(null);
  const [nomVille, setNomVille] = useState('');

  // Dialogue Quartier
  const [dialogQuartier, setDialogQuartier] = useState<{ mode: 'creer' | 'modifier'; item?: Quartier } | null>(null);
  const [nomQuartier, setNomQuartier] = useState('');
  const [localiteQuartier, setLocaliteQuartier] = useState<number | ''>('');

  const [suppression, setSuppression] = useState<{ type: 'ville' | 'quartier'; id: number; nom: string } | null>(null);

  const recharger = () => {
    setChargement(true);
    // On efface d'abord tout message précédent : le bandeau d'erreur restait
    // affiché même après un rechargement réussi.
    setRetour(null);
    // Les deux appels sont indépendants : un échec de l'un ne doit pas masquer
    // le succès de l'autre (auparavant, un seul échec vidait tout l'écran).
    Promise.allSettled([referentielApi.localites(), referentielApi.quartiers()])
      .then(([resL, resQ]) => {
        if (resL.status === 'fulfilled') setLocalites(resL.value);
        if (resQ.status === 'fulfilled') setQuartiers(resQ.value);
        if (resL.status === 'rejected' || resQ.status === 'rejected') {
          setRetour({ type: 'error', texte: 'Certaines données n\'ont pas pu être chargées. Réessayez.' });
        }
      })
      .finally(() => setChargement(false));
  };

  useEffect(recharger, []);

  const ouvrirCreationVille = () => { setNomVille(''); setDialogVille({ mode: 'creer' }); };
  const ouvrirEditionVille = (l: Localite) => { setNomVille(l.nom); setDialogVille({ mode: 'modifier', item: l }); };

  const enregistrerVille = async () => {
    if (!nomVille.trim() || !dialogVille) return;
    try {
      if (dialogVille.mode === 'creer') await referentielApi.creerLocalite(nomVille.trim());
      else await referentielApi.modifierLocalite(dialogVille.item!.id, nomVille.trim());
      setRetour({ type: 'success', texte: 'Ville enregistrée.' });
      setDialogVille(null);
      recharger();
    } catch {
      setRetour({ type: 'error', texte: "Échec de l'enregistrement de la ville." });
    }
  };

  const ouvrirCreationQuartier = () => {
    setNomQuartier(''); setLocaliteQuartier(localites[0]?.id ?? '');
    setDialogQuartier({ mode: 'creer' });
  };
  const ouvrirEditionQuartier = (q: Quartier) => {
    setNomQuartier(q.nom); setLocaliteQuartier(q.localite?.id ?? '');
    setDialogQuartier({ mode: 'modifier', item: q });
  };

  const enregistrerQuartier = async () => {
    if (!nomQuartier.trim() || !localiteQuartier || !dialogQuartier) return;
    try {
      if (dialogQuartier.mode === 'creer') await referentielApi.creerQuartier(nomQuartier.trim(), Number(localiteQuartier));
      else await referentielApi.modifierQuartier(dialogQuartier.item!.id, nomQuartier.trim(), Number(localiteQuartier));
      setRetour({ type: 'success', texte: 'Quartier enregistré.' });
      setDialogQuartier(null);
      recharger();
    } catch {
      setRetour({ type: 'error', texte: "Échec de l'enregistrement du quartier." });
    }
  };

  const confirmerSuppression = async () => {
    if (!suppression) return;
    try {
      if (suppression.type === 'ville') await referentielApi.supprimerLocalite(suppression.id);
      else await referentielApi.supprimerQuartier(suppression.id);
      setRetour({ type: 'success', texte: 'Suppression effectuée.' });
      setSuppression(null);
      recharger();
    } catch {
      setRetour({ type: 'error', texte: 'Suppression impossible (élément probablement utilisé par un bien).' });
    }
  };

  const colonnesVilles: GridColDef<Localite>[] = [
    { field: 'nom', headerName: 'Ville', flex: 1, minWidth: 200 },
    {
      field: 'actions', headerName: 'Actions', width: 160, sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5}>
          <Button size="small" startIcon={<EditIcon />} onClick={() => ouvrirEditionVille(p.row)}>Modifier</Button>
          <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => setSuppression({ type: 'ville', id: p.row.id, nom: p.row.nom })} />
        </Stack>
      ),
    },
  ];

  const colonnesQuartiers: GridColDef<Quartier>[] = [
    { field: 'nom', headerName: 'Quartier', flex: 1, minWidth: 180 },
    { field: 'localite', headerName: 'Ville', width: 160, valueGetter: (_v, row) => row.localite?.nom ?? '—' },
    {
      field: 'actions', headerName: 'Actions', width: 160, sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5}>
          <Button size="small" startIcon={<EditIcon />} onClick={() => ouvrirEditionQuartier(p.row)}>Modifier</Button>
          <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => setSuppression({ type: 'quartier', id: p.row.id, nom: p.row.nom })} />
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Villes &amp; quartiers</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Référentiel géographique utilisé pour la localisation des biens et la recherche.
      </Typography>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour.texte}</Alert>
      )}

      <Tabs value={onglet} onChange={(_e, v) => setOnglet(v)} sx={{ mb: 2 }}>
        <Tab label={`Villes (${localites.length})`} />
        <Tab label={`Quartiers (${quartiers.length})`} />
      </Tabs>

      {onglet === 0 && (
        <Card>
          <Box sx={{ p: 1.5, display: 'flex', justifyContent: 'flex-end' }}>
            <Button variant="contained" startIcon={<AddIcon />} onClick={ouvrirCreationVille}>Ajouter une ville</Button>
          </Box>
          <div style={{ height: 480, width: '100%' }}>
            <DataGrid
              rows={localites} columns={colonnesVilles} loading={chargement} disableRowSelectionOnClick
              initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            />
          </div>
        </Card>
      )}

      {onglet === 1 && (
        <Card>
          <Box sx={{ p: 1.5, display: 'flex', justifyContent: 'flex-end' }}>
            <Button variant="contained" startIcon={<AddIcon />} onClick={ouvrirCreationQuartier} disabled={!localites.length}>
              Ajouter un quartier
            </Button>
          </Box>
          <div style={{ height: 480, width: '100%' }}>
            <DataGrid
              rows={quartiers} columns={colonnesQuartiers} loading={chargement} disableRowSelectionOnClick
              initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            />
          </div>
        </Card>
      )}

      {/* Ville */}
      <Dialog open={Boolean(dialogVille)} onClose={() => setDialogVille(null)} fullWidth maxWidth="xs">
        <DialogTitle>{dialogVille?.mode === 'creer' ? 'Ajouter une ville' : 'Modifier la ville'}</DialogTitle>
        <DialogContent>
          <TextField autoFocus fullWidth sx={{ mt: 1 }} label="Nom de la ville" value={nomVille} onChange={(e) => setNomVille(e.target.value)} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogVille(null)}>Annuler</Button>
          <Button variant="contained" disabled={!nomVille.trim()} onClick={enregistrerVille}>Enregistrer</Button>
        </DialogActions>
      </Dialog>

      {/* Quartier */}
      <Dialog open={Boolean(dialogQuartier)} onClose={() => setDialogQuartier(null)} fullWidth maxWidth="xs">
        <DialogTitle>{dialogQuartier?.mode === 'creer' ? 'Ajouter un quartier' : 'Modifier le quartier'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField autoFocus fullWidth label="Nom du quartier" value={nomQuartier} onChange={(e) => setNomQuartier(e.target.value)} />
            <TextField select fullWidth label="Ville" value={localiteQuartier} onChange={(e) => setLocaliteQuartier(Number(e.target.value))}>
              {localites.map((l) => <MenuItem key={l.id} value={l.id}>{l.nom}</MenuItem>)}
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogQuartier(null)}>Annuler</Button>
          <Button variant="contained" disabled={!nomQuartier.trim() || !localiteQuartier} onClick={enregistrerQuartier}>Enregistrer</Button>
        </DialogActions>
      </Dialog>

      {/* Suppression */}
      <Dialog open={Boolean(suppression)} onClose={() => setSuppression(null)} fullWidth maxWidth="xs">
        <DialogTitle>Confirmer la suppression</DialogTitle>
        <DialogContent>
          <Typography>« {suppression?.nom} » sera définitivement supprimé(e).</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSuppression(null)}>Annuler</Button>
          <Button variant="contained" color="error" onClick={confirmerSuppression}>Supprimer</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
