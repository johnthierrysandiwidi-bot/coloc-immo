import { useEffect, useMemo, useState } from 'react';
import {
  Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  List, ListItem, ListItemText, Snackbar, Alert, Stack, Tab, Tabs, TextField, Typography,
} from '@mui/material';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import StarIcon from '@mui/icons-material/Star';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import { messageErreur, paiementApi, rendezVousApi } from '@/api/services';
import DialoguePaiement from '@/components/DialoguePaiement';
import { useAppSelector } from '@/app/hooks';
import type { RendezVous, StatutRendezVous } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';
import DialogueAvis from '@/components/DialogueAvis';

const COULEUR: Record<StatutRendezVous, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  DEMANDE: 'warning',
  ACCEPTE: 'success',
  REFUSE: 'error',
  REPORTE: 'info',
  ANNULE: 'default',
  // Une visite menée à son terme est une issue positive : la distinguer
  // visuellement d'une annulation, qui partageait la même teinte neutre.
  TERMINE: 'success',
};

const STATUTS_ACTIFS: StatutRendezVous[] = ['DEMANDE', 'REPORTE', 'ACCEPTE'];
const STATUTS_HISTORIQUE: StatutRendezVous[] = ['REFUSE', 'ANNULE', 'TERMINE'];

const dateRdv = (r: RendezVous) => new Date(r.dateReportee ?? r.dateHeure);

/** Valeur pour un <input type="datetime-local"> à partir d'une date (heure locale). */
function versInputLocal(d: Date): string {
  const p = (x: number) => String(x).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}

export default function RendezVousPage() {
  const { userId } = useAppSelector((s) => s.auth);
  const [rdvAPayer, setRdvAPayer] = useState<number | null>(null);
  const [rdvANoter, setRdvANoter] = useState<number | null>(null);
  /** Retour utilisateur : sans lui, un refus du serveur passait totalement inaperçu. */
  const [retour, setRetour] = useState<{ type: 'success' | 'error'; texte: string } | null>(null);

  // Bornes du champ de report, au format attendu par datetime-local.
  const pourChampLocal = (d: Date) =>
    new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  const dateMin = pourChampLocal(new Date());
  const dateMax = pourChampLocal(new Date(Date.now() + 365 * 24 * 3600 * 1000));
  const [rdv, setRdv] = useState<RendezVous[]>([]);
  /** Statut du paiement des frais, par identifiant de rendez-vous. */
  const [paiements, setPaiements] = useState<Record<number, string>>({});
  const [chargement, setChargement] = useState(true);
  const [onglet, setOnglet] = useState(0);

  // Dialogue de report
  const [aReporter, setAReporter] = useState<RendezVous | null>(null);
  const [nouvelleDate, setNouvelleDate] = useState('');

  const recharger = () => {
    setChargement(true);
    rendezVousApi
      .lister()
      .then(async (p) => {
        setRdv(p.contenu);
        // Frais de visite : visiteur ET auteur de l'annonce sont autorisés à connaître
        // l'état du règlement ; le serveur refuse (403) pour les tiers, d'où le catch.
        const miens = p.contenu.filter((r) => ['DEMANDE', 'REPORTE', 'ACCEPTE'].includes(r.statut));
        const etats = await Promise.all(
          miens.map(async (r) => [r.id, (await paiementApi.pourRendezVous(r.id))?.statut] as const),
        );
        setPaiements(Object.fromEntries(etats.filter(([, st]) => Boolean(st)) as [number, string][]));
      })
      .finally(() => setChargement(false));
  };
  useEffect(recharger, [userId]);

  const agir = async (action: 'accepter' | 'refuser' | 'annuler' | 'terminer', id: number) => {
    try {
      if (action === 'accepter') await rendezVousApi.accepter(id);
      if (action === 'refuser') await rendezVousApi.refuser(id, 'Créneau indisponible');
      if (action === 'annuler') await rendezVousApi.annuler(id);
      if (action === 'terminer') await rendezVousApi.terminer(id);
      const libelle =
        action === 'accepter' ? 'Rendez-vous accepté.'
        : action === 'terminer' ? 'Visite confirmée. Les frais sont débloqués.'
        : action === 'refuser' ? 'Rendez-vous refusé.'
        : 'Rendez-vous annulé.';
      setRetour({ type: 'success', texte: libelle });
      recharger();
    } catch (e) {
      // Le clic restait sans effet visible quand le serveur refusait l'action.
      setRetour({ type: 'error', texte: messageErreur(e) });
    }
  };

  const ouvrirReport = (r: RendezVous) => {
    setAReporter(r);
    const base = dateRdv(r);
    setNouvelleDate(versInputLocal(base));
  };

  const confirmerReport = async () => {
    if (!aReporter || !nouvelleDate) return;
    // Même garde que sur la demande de visite : une saisie comme « 22/07/62026 »
    // produit une date invalide et faisait échouer toISOString() sans message.
    const date = new Date(nouvelleDate);
    if (Number.isNaN(date.getTime())) {
      setRetour({ type: 'error', texte: 'Date invalide. Vérifiez le jour, le mois et l’année.' });
      return;
    }
    if (date.getTime() < Date.now()) {
      setRetour({ type: 'error', texte: 'La nouvelle date doit être dans le futur.' });
      return;
    }
    try {
      await rendezVousApi.reporter(aReporter.id, date.toISOString());
      setAReporter(null);
      setNouvelleDate('');
      setRetour({ type: 'success', texte: 'Rendez-vous reporté. Le demandeur a été notifié.' });
      recharger();
    } catch (e) {
      setRetour({ type: 'error', texte: messageErreur(e) });
    }
  };

  const actifs = useMemo(() => rdv.filter((r) => STATUTS_ACTIFS.includes(r.statut)), [rdv]);
  const historique = useMemo(() => rdv.filter((r) => STATUTS_HISTORIQUE.includes(r.statut)), [rdv]);

  // Calendrier : rendez-vous à venir (acceptés ou reportés), groupés par jour.
  const parJour = useMemo(() => {
    const maintenant = Date.now();
    const aVenir = rdv
      .filter((r) => (r.statut === 'ACCEPTE' || r.statut === 'REPORTE') && dateRdv(r).getTime() >= maintenant)
      .sort((a, b) => dateRdv(a).getTime() - dateRdv(b).getTime());
    const groupes = new Map<string, RendezVous[]>();
    for (const r of aVenir) {
      const cle = dateRdv(r).toLocaleDateString('fr-FR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
      groupes.set(cle, [...(groupes.get(cle) ?? []), r]);
    }
    return [...groupes.entries()];
  }, [rdv]);

  const colonnes = (avecActions: boolean): GridColDef<RendezVous>[] => [
    { field: 'annonce', headerName: 'Annonce', flex: 1, minWidth: 180, valueGetter: (_v, r) => r.annonce?.titre ?? '—' },
    { field: 'demandeur', headerName: 'Demandeur', width: 140, valueGetter: (_v, r) => r.demandeur?.login ?? '—' },
    {
      field: 'dateHeure',
      headerName: 'Date',
      width: 180,
      valueGetter: (_v, r) => dateRdv(r).toLocaleString('fr-FR'),
    },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 120,
      renderCell: (p) => <Chip size="small" label={p.row.statut} color={COULEUR[p.row.statut]} />,
    },
    {
      field: 'frais',
      headerName: 'Frais de visite',
      width: 150,
      sortable: false,
      renderCell: (p) => {
        const etat = paiements[p.row.id];
        if (!etat) return <Chip size="small" variant="outlined" label="Non réglés" />;
        const libelle =
          etat === 'EN_ATTENTE' ? 'À finaliser'
          : etat === 'EN_SEQUESTRE' ? 'En séquestre'
          : etat === 'LIBERE' ? 'Versés'
          : 'Remboursés';
        return <Chip size="small" color={etat === 'EN_ATTENTE' ? 'warning' : 'success'} label={libelle} />;
      },
    },
    ...(avecActions
      ? [{
          field: 'actions',
          headerName: 'Actions',
          width: 330,
          sortable: false,
          renderCell: (p: { row: RendezVous }) => {
            const enAttente = p.row.statut === 'DEMANDE' || p.row.statut === 'REPORTE';
            return (
              <Stack direction="row" spacing={0.5}>
                {enAttente && (
                  <>
                    <Button size="small" variant="contained" onClick={() => agir('accepter', p.row.id)}>Accepter</Button>
                    <Button size="small" color="error" onClick={() => agir('refuser', p.row.id)}>Refuser</Button>
                  </>
                )}
                {/* Frais de visite : le demandeur règle tant que la visite est à venir.
                    Auparavant limité au statut DEMANDE, le bouton disparaissait dès que le
                    propriétaire acceptait — le visiteur ne pouvait alors plus jamais payer. */}
                {p.row.demandeur?.id === userId &&
                  ['DEMANDE', 'REPORTE', 'ACCEPTE'].includes(p.row.statut) &&
                  (paiements[p.row.id] ? (
                    <Chip size="small" color="success" variant="outlined"
                      label={paiements[p.row.id] === 'EN_ATTENTE' ? 'Paiement à finaliser' : 'Frais réglés'} />
                  ) : (
                    <Button size="small" variant="contained" color="success"
                      onClick={() => setRdvAPayer(p.row.id)}>Payer les frais</Button>
                  ))}
                {/* Clôture : le statut TERMINE n'était jamais posé, si bien que les frais
                    étaient débloqués sans trace de la visite. Les deux parties peuvent
                    déclarer la visite faite ; le serveur distingue qui parle. */}
                {p.row.statut === 'ACCEPTE' && (
                  <Button size="small" variant="outlined" color="success"
                    onClick={() => agir('terminer', p.row.id)}>Visite effectuée</Button>
                )}
                {(enAttente || p.row.statut === 'ACCEPTE') && (
                  <>
                    <Button size="small" color="info" onClick={() => ouvrirReport(p.row)}>Reporter</Button>
                    <Button size="small" onClick={() => agir('annuler', p.row.id)}>Annuler</Button>
                  </>
                )}
                {/* Avis : seul le visiteur, après une visite effectuée, peut noter. */}
                {p.row.statut === 'TERMINE' && p.row.demandeur?.id === userId && (
                  <Button size="small" variant="outlined" startIcon={<StarIcon fontSize="small" />}
                    onClick={() => setRdvANoter(p.row.id)}>Noter</Button>
                )}
              </Stack>
            );
          },
        } as GridColDef<RendezVous>]
      : []),
  ];

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Mes rendez-vous</Typography>

      <Tabs value={onglet} onChange={(_e, v) => setOnglet(v)} sx={{ mb: 2 }}>
        <Tab label={`En cours (${actifs.length})`} />
        <Tab label="Calendrier des visites" />
        <Tab label={`Historique (${historique.length})`} />
      </Tabs>

      {onglet === 0 && (
        <Card>
          <div style={{ height: 520, width: '100%' }}>
            <DataGrid
              rows={actifs}
              columns={colonnes(true)}
              loading={chargement}
              disableRowSelectionOnClick
              initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            />
          </div>
        </Card>
      )}

      {onglet === 1 && (
        <Card>
          <CardContent>
            {parJour.length === 0 ? (
              <Typography color="text.secondary">Aucune visite planifiée à venir.</Typography>
            ) : (
              parJour.map(([jour, liste]) => (
                <Box key={jour} sx={{ mb: 2 }}>
                  <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
                    <EventAvailableIcon fontSize="small" color="primary" />
                    <Typography variant="subtitle1" fontWeight={600} textTransform="capitalize">{jour}</Typography>
                  </Stack>
                  <List dense disablePadding sx={{ pl: 3 }}>
                    {liste.map((r) => (
                      <ListItem key={r.id} disableGutters
                        secondaryAction={<Chip size="small" label={r.statut} color={COULEUR[r.statut]} />}>
                        <ListItemText
                          primary={`${dateRdv(r).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })} — ${r.annonce?.titre ?? 'Annonce'}`}
                          secondary={`Demandeur : ${r.demandeur?.login ?? '—'}`}
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              ))
            )}
          </CardContent>
        </Card>
      )}

      {onglet === 2 && (
        <Card>
          <div style={{ height: 520, width: '100%' }}>
            <DataGrid
              rows={historique}
              columns={colonnes(false)}
              loading={chargement}
              disableRowSelectionOnClick
              initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            />
          </div>
        </Card>
      )}

      <Dialog open={Boolean(aReporter)} onClose={() => setAReporter(null)} fullWidth maxWidth="xs">
        <DialogTitle>Reporter le rendez-vous</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {aReporter?.annonce?.titre ?? 'Annonce'} — demandé par {aReporter?.demandeur?.login ?? '—'}
          </Typography>
          <TextField
            fullWidth
            type="datetime-local"
            label="Nouvelle date et heure"
            InputLabelProps={{ shrink: true }}
            inputProps={{ min: dateMin, max: dateMax }}
            helperText="Entre aujourd'hui et un an." 
            value={nouvelleDate}
            onChange={(e) => setNouvelleDate(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAReporter(null)}>Annuler</Button>
          <Button variant="contained" onClick={confirmerReport} disabled={!nouvelleDate}>
            Confirmer le report
          </Button>
        </DialogActions>
      </Dialog>
      <Snackbar
        open={retour !== null}
        autoHideDuration={5000}
        onClose={() => setRetour(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={retour?.type ?? 'info'} onClose={() => setRetour(null)} variant="filled">
          {retour?.texte}
        </Alert>
      </Snackbar>

      <DialoguePaiement
        rendezVousId={rdvAPayer}
        ouvert={rdvAPayer !== null}
        onFermer={() => setRdvAPayer(null)}
        onPaye={recharger}
      />
    </Box>
  );
}