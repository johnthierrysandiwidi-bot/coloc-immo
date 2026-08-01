import { useEffect, useMemo, useState } from 'react';
import {
  Alert, Box, Button, Card, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  InputAdornment, MenuItem, Stack, Tab, Tabs, TextField, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import SendIcon from '@mui/icons-material/Send';
import SearchIcon from '@mui/icons-material/Search';
import { adminApi } from '@/api/services';
import { ROLES } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

const ONGLETS = [
  { libelle: 'Tous', role: null },
  { libelle: 'Propriétaires', role: ROLES.PROPRIETAIRE },
  { libelle: 'Démarcheurs', role: ROLES.DEMARCHEUR },
  { libelle: 'Administrateurs', role: ROLES.ADMIN },
] as const;

interface Utilisateur {
  id: number;
  login: string;
  email: string;
  activated: boolean;
  authorities: string[];
}

const ROLES_ATTRIBUABLES = [
  { valeur: ROLES.UTILISATEUR, libelle: 'Utilisateur' },
  { valeur: ROLES.PROPRIETAIRE, libelle: 'Propriétaire' },
  { valeur: ROLES.DEMARCHEUR, libelle: 'Démarcheur' },
  { valeur: ROLES.ADMIN, libelle: 'Administrateur' },
];

export default function AdminUtilisateursPage() {
  const [users, setUsers] = useState<Utilisateur[]>([]);
  const [chargement, setChargement] = useState(true);
  const [cibleRole, setCibleRole] = useState<Utilisateur | null>(null);
  const [role, setRole] = useState<string>(ROLES.DEMARCHEUR);
  const [cibleNotif, setCibleNotif] = useState<Utilisateur | null>(null);
  const [titre, setTitre] = useState('');
  const [message, setMessage] = useState('');
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);
  const [onglet, setOnglet] = useState(0);
  const [recherche, setRecherche] = useState('');

  const usersFiltres = useMemo(() => {
    const roleFiltre = ONGLETS[onglet].role;
    const q = recherche.trim().toLowerCase();
    return users.filter((u) => {
      const okRole = !roleFiltre || (u.authorities ?? []).includes(roleFiltre);
      const okRecherche = !q || u.login.toLowerCase().includes(q) || u.email.toLowerCase().includes(q);
      return okRole && okRecherche;
    });
  }, [users, onglet, recherche]);

  const recharger = () => {
    setChargement(true);
    adminApi.utilisateurs().then(setUsers).finally(() => setChargement(false));
  };

  useEffect(recharger, []);

  const agir = async (action: () => Promise<unknown>, texte: string) => {
    try {
      await action();
      setRetour({ type: 'success', texte });
      recharger();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { detail?: string }; status?: number } };
      setRetour({ type: 'error', texte: err.response?.data?.detail ?? `Échec (HTTP ${err.response?.status ?? '?'})` });
    }
  };

  const colonnes: GridColDef<Utilisateur>[] = [
    { field: 'login', headerName: 'Identifiant', width: 150 },
    { field: 'email', headerName: 'Email', flex: 1, minWidth: 180 },
    {
      field: 'activated',
      headerName: 'État',
      width: 120,
      renderCell: (p) => (
        <Chip size="small" label={p.row.activated ? 'Actif' : 'Suspendu'} color={p.row.activated ? 'success' : 'error'} />
      ),
    },
    {
      field: 'authorities',
      headerName: 'Rôles',
      flex: 1,
      minWidth: 200,
      sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap>
          {(p.row.authorities ?? [])
            .filter((a) => a !== 'ROLE_USER')
            .map((a) => (
              <Chip key={a} size="small" variant="outlined" label={a.replace('ROLE_', '')} />
            ))}
        </Stack>
      ),
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 280,
      sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5}>
          {p.row.activated ? (
            <Button size="small" color="error" startIcon={<BlockIcon />}
              onClick={() => agir(() => adminApi.suspendre(p.row.id), 'Compte suspendu, annonces masquées.')}>
              Suspendre
            </Button>
          ) : (
            <Button size="small" color="success" startIcon={<CheckCircleIcon />}
              onClick={() => agir(() => adminApi.reactiver(p.row.id), 'Compte réactivé.')}>
              Réactiver
            </Button>
          )}
          <Button size="small" onClick={() => setCibleRole(p.row)}>Rôle</Button>
          <Button size="small" startIcon={<SendIcon />} onClick={() => setCibleNotif(p.row)}>Notifier</Button>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Utilisateurs</Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Suspendre un compte masque aussi ses annonces publiées.
      </Typography>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour.texte}</Alert>
      )}

      <Tabs value={onglet} onChange={(_e, v) => setOnglet(v)} sx={{ mb: 2 }}>
        {ONGLETS.map((o) => <Tab key={o.libelle} label={o.libelle} />)}
      </Tabs>

      <TextField
        size="small" placeholder="Rechercher par identifiant ou email..." value={recherche}
        onChange={(e) => setRecherche(e.target.value)}
        InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }}
        sx={{ mb: 2, minWidth: 320 }}
      />

      <Card>
        <div style={{ height: 560, width: '100%' }}>
          <DataGrid rows={usersFiltres} columns={colonnes} loading={chargement} disableRowSelectionOnClick
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }} />
        </div>
      </Card>

      {/* Attribution de rôle — le filet de sécurité si l'inscription n'a pas posé le bon rôle */}
      <Dialog open={Boolean(cibleRole)} onClose={() => setCibleRole(null)} fullWidth maxWidth="xs">
        <DialogTitle>Attribuer un rôle à {cibleRole?.login}</DialogTitle>
        <DialogContent>
          <TextField select fullWidth sx={{ mt: 1 }} label="Rôle" value={role} onChange={(e) => setRole(e.target.value)}>
            {ROLES_ATTRIBUABLES.map((r) => <MenuItem key={r.valeur} value={r.valeur}>{r.libelle}</MenuItem>)}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCibleRole(null)}>Annuler</Button>
          <Button variant="contained" onClick={() => {
            const cible = cibleRole!;
            setCibleRole(null);
            agir(() => adminApi.attribuerRole(cible.id, role), `Rôle attribué à ${cible.login}.`);
          }}>
            Attribuer
          </Button>
        </DialogActions>
      </Dialog>

      {/* Notification manuelle */}
      <Dialog open={Boolean(cibleNotif)} onClose={() => setCibleNotif(null)} fullWidth maxWidth="sm">
        <DialogTitle>Notifier {cibleNotif?.login}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField fullWidth label="Titre" value={titre} onChange={(e) => setTitre(e.target.value)} />
            <TextField fullWidth multiline rows={3} label="Message" value={message} onChange={(e) => setMessage(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCibleNotif(null)}>Annuler</Button>
          <Button variant="contained" disabled={!titre.trim() || !message.trim()} onClick={() => {
            const cible = cibleNotif!;
            setCibleNotif(null);
            agir(() => adminApi.notifier(cible.id, titre, message), 'Notification envoyée.');
            setTitre(''); setMessage('');
          }}>
            Envoyer
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
