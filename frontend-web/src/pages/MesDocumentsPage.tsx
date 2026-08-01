import { useEffect, useRef, useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Chip, IconButton, List, ListItem, ListItemText,
  MenuItem, Stack, TextField, Tooltip, Typography,
} from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteIcon from '@mui/icons-material/Delete';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import {documentsApi, uploadApi, ouvrirFichierProtege } from '@/api/services';
import { useAppSelector } from '@/app/hooks';
import type { DocumentDemarcheur, StatutValidation } from '@/types';
import BoutonRetour from '@/components/BoutonRetour';

const COULEUR: Record<StatutValidation, 'warning' | 'success' | 'error'> = {
  EN_ATTENTE: 'warning',
  VALIDE: 'success',
  REFUSE: 'error',
};

export default function MesDocumentsPage() {
  const { userId } = useAppSelector((s) => s.auth);
  const [docs, setDocs] = useState<DocumentDemarcheur[]>([]);
  const [types, setTypes] = useState<{ id: number; nom: string }[]>([]);
  const [typeChoisi, setTypeChoisi] = useState<number | ''>('');
  const [erreur, setErreur] = useState<string | null>(null);
  const [envoi, setEnvoi] = useState(false);
  const [idARemplacer, setIdARemplacer] = useState<number | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const inputRemplacementRef = useRef<HTMLInputElement>(null);

  const recharger = () => {
    if (!userId) return Promise.resolve();
    return documentsApi.mesDocuments(userId).then(setDocs).catch(() => setDocs([]));
  };

  useEffect(() => {
    if (!userId) return;
    recharger();
    documentsApi.typesDocument().then((t) => {
      setTypes(t);
      if (t.length > 0) setTypeChoisi(t[0].id);
    });
  }, [userId]);

  const televerser = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const fichier = e.target.files?.[0];
    if (!fichier || !userId || typeChoisi === '') return;
    setErreur(null);
    setEnvoi(true);
    try {
      await documentsApi.televerser(fichier, fichier.name, typeChoisi, userId);
      await recharger();
    } catch {
      setErreur('Fichier refusé. Formats acceptés : PDF, JPG, PNG — 5 Mo maximum.');
    } finally {
      setEnvoi(false);
      if (inputRef.current) inputRef.current.value = '';
    }
  };

  const remplacer = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const fichier = e.target.files?.[0];
    if (!fichier || idARemplacer === null) return;
    setErreur(null);
    setEnvoi(true);
    try {
      const url = await uploadApi.image(fichier);
      await documentsApi.remplacer(idARemplacer, url, fichier.name);
      await recharger();
    } catch {
      setErreur('Le remplacement a échoué : un document déjà validé ne peut plus être remplacé.');
    } finally {
      setEnvoi(false);
      setIdARemplacer(null);
      if (inputRemplacementRef.current) inputRemplacementRef.current.value = '';
    }
  };

  const demanderRemplacement = (id: number) => {
    setIdARemplacer(id);
    inputRemplacementRef.current?.click();
  };

  const retirer = async (id: number) => {
    setErreur(null);
    try {
      await documentsApi.retirer(id);
      await recharger();
    } catch {
      setErreur('Suppression impossible : un document validé ne peut plus être retiré.');
    }
  };

  const telecharger = async (id: number) => {
    try {
      const url = await documentsApi.telecharger(id);
      // window.open() sur cette URL ouvrait un onglet sans jeton d'authentification :
      // le serveur refusait, l'adresse ne correspondait à aucune route du site, et
      // l'application retombait sur l'accueil. On récupère donc le fichier via le
      // client authentifié avant de l'ouvrir.
      await ouvrirFichierProtege(url);
    } catch {
      setErreur('Téléchargement impossible : fichier introuvable ou accès refusé.');
    }
  };

  const valide = docs.some((d) => d.statut === 'VALIDE');

  return (
    <Box>
      <BoutonRetour />
      <Typography variant="h4" gutterBottom>Mes documents</Typography>

      <Alert severity={valide ? 'success' : 'info'} sx={{ mb: 3 }}>
        {valide
          ? 'Vos documents sont validés. Vous pouvez publier des annonces.'
          : "Publication bloquée : un administrateur doit d'abord valider une pièce justificative."}
      </Alert>

      {erreur && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setErreur(null)}>{erreur}</Alert>}

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
            <TextField
              select size="small" label="Type de pièce" sx={{ minWidth: 220 }}
              value={typeChoisi}
              onChange={(e) => setTypeChoisi(Number(e.target.value))}
            >
              {types.map((t) => (
                <MenuItem key={t.id} value={t.id}>{t.nom}</MenuItem>
              ))}
            </TextField>

            <Button
              variant="contained"
              startIcon={<UploadFileIcon />}
              disabled={typeChoisi === '' || envoi}
              onClick={() => inputRef.current?.click()}
            >
              {envoi ? 'Envoi…' : 'Déposer le document'}
            </Button>

            <input ref={inputRef} type="file" hidden accept=".pdf,.jpg,.jpeg,.png" onChange={televerser} />
            <input ref={inputRemplacementRef} type="file" hidden accept=".pdf,.jpg,.jpeg,.png" onChange={remplacer} />
          </Stack>
        </CardContent>
      </Card>

      <Card>
        <List>
          {docs.length === 0 && (
            <ListItem><ListItemText secondary="Aucun document déposé." /></ListItem>
          )}
          {docs.map((d) => (
            <ListItem
              key={d.id}
              secondaryAction={
                <Stack direction="row" spacing={0.5} alignItems="center">
                  <Chip size="small" label={d.statut} color={COULEUR[d.statut]} sx={{ mr: 1 }} />

                  <Tooltip title="Télécharger">
                    <IconButton size="small" onClick={() => telecharger(d.id)}>
                      <DownloadIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>

                  {d.statut !== 'VALIDE' && (
                    <Tooltip title={d.statut === 'REFUSE' ? 'Remplacer la pièce refusée' : 'Remplacer'}>
                      <span>
                        <IconButton size="small" disabled={envoi} onClick={() => demanderRemplacement(d.id)}>
                          <AutorenewIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                  )}

                  {d.statut !== 'VALIDE' && (
                    <Tooltip title="Supprimer">
                      <IconButton size="small" color="error" onClick={() => retirer(d.id)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                </Stack>
              }
            >
              <ListItemText
                primary={d.nom}
                secondary={d.statut === 'REFUSE' && d.motifRefus ? `Motif du refus : ${d.motifRefus}` : undefined}
              />
            </ListItem>
          ))}
        </List>
      </Card>
    </Box>
  );
}
