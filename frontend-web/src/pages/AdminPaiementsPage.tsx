import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, IconButton, Paper, Stack, Table, TableBody, TableCell, Tooltip,
  TableContainer, TableHead, TableRow, Typography,
} from '@mui/material';
import LockOpenIcon from '@mui/icons-material/LockOpen';
import DownloadIcon from '@mui/icons-material/Download';
import ReplayIcon from '@mui/icons-material/Replay';
import { paiementApi, type Paiement } from '@/api/services';
import { telechargerRecuPaiement } from '@/utils/recuPdf';
import BoutonRetour from '@/components/BoutonRetour';

const COULEUR: Record<Paiement['statut'], 'warning' | 'info' | 'success' | 'error'> = {
  EN_ATTENTE: 'warning',
  EN_SEQUESTRE: 'info',
  LIBERE: 'success',
  REMBOURSE: 'error',
};

const LIBELLE: Record<Paiement['statut'], string> = {
  EN_ATTENTE: 'En attente',
  EN_SEQUESTRE: 'En séquestre',
  LIBERE: 'Libéré au démarcheur',
  REMBOURSE: 'Remboursé au client',
};

const LIBELLE_MOYEN: Record<string, string> = {
  ORANGE_MONEY: 'Orange Money',
  MOOV_MONEY: 'Moov Money',
  CARTE: 'Carte bancaire',
};

const fcfa = (n?: number) => (n == null ? '—' : `${n.toLocaleString('fr-FR')} FCFA`);

export default function AdminPaiementsPage() {
  const [paiements, setPaiements] = useState<Paiement[]>([]);
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  const recharger = () => paiementApi.tous().then(setPaiements).catch(() => setPaiements([]));
  useEffect(() => { recharger(); }, []);

  const agir = async (action: 'liberer' | 'rembourser', id: number) => {
    try {
      await paiementApi[action](id);
      setRetour({
        type: 'success',
        texte: action === 'liberer' ? 'Fonds libérés au démarcheur.' : 'Client remboursé.',
      });
      recharger();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { detail?: string } } };
      setRetour({ type: 'error', texte: err.response?.data?.detail ?? 'Action impossible.' });
    }
  };

  const totalSequestre = paiements
    .filter((p) => p.statut === 'EN_SEQUESTRE')
    .reduce((s, p) => s + (p.montant ?? 0), 0);

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Paiements et séquestres</Typography>
      <Alert severity="info" sx={{ mb: 2 }}>
        Frais de visite conservés en séquestre. Libérez les fonds au démarcheur après une
        visite honorée, ou remboursez le client si la visite n'a pas eu lieu.
        Actuellement en séquestre : <strong>{fcfa(totalSequestre)}</strong>.
      </Alert>

      {retour && (
        <Alert severity={retour.type} sx={{ mb: 2 }} onClose={() => setRetour(null)}>{retour.texte}</Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Référence</TableCell>
              <TableCell>Annonce</TableCell>
              <TableCell>Client</TableCell>
              <TableCell>Montant</TableCell>
              <TableCell>Moyen</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Statut</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paiements.length === 0 && (
              <TableRow><TableCell colSpan={8}>Aucun paiement pour le moment.</TableCell></TableRow>
            )}
            {paiements.map((p) => (
              <TableRow key={p.id} hover>
                <TableCell>{p.reference}</TableCell>
                <TableCell>{p.annonceTitre ?? '—'}</TableCell>
                <TableCell>{p.payeurLogin ?? '—'}</TableCell>
                <TableCell>{fcfa(p.montant)}</TableCell>
                {/* Le moyen et la date manquaient : impossible de rapprocher un
                    paiement d'un relevé d'opérateur, ni de savoir depuis combien
                    de temps une somme dort en séquestre. */}
                <TableCell>{LIBELLE_MOYEN[p.moyen ?? ''] ?? '—'}</TableCell>
                <TableCell>
                  {p.dateSequestre
                    ? new Date(p.dateSequestre).toLocaleString('fr-FR')
                    : p.dateCreation
                      ? new Date(p.dateCreation).toLocaleString('fr-FR')
                      : '—'}
                </TableCell>
                <TableCell><Chip size="small" color={COULEUR[p.statut]} label={LIBELLE[p.statut]} /></TableCell>
                <TableCell align="right">
                  <Stack direction="row" spacing={1} justifyContent="flex-end" alignItems="center">
                    <Tooltip title="Télécharger le reçu">
                      <IconButton size="small" onClick={() => telechargerRecuPaiement(p)}>
                        <DownloadIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    {p.statut === 'EN_SEQUESTRE' ? (
                      <>
                        <Button size="small" variant="contained" startIcon={<LockOpenIcon />}
                          onClick={() => agir('liberer', p.id)}>Libérer</Button>
                        <Button size="small" color="error" startIcon={<ReplayIcon />}
                          onClick={() => agir('rembourser', p.id)}>Rembourser</Button>
                      </>
                    ) : (
                      <Typography variant="caption" color="text.secondary">—</Typography>
                    )}
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
