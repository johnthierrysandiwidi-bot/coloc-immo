import { Button } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';

/**
 * Bouton de retour.
 *
 * Aucune page n'en proposait : depuis le détail d'une annonce, il fallait se
 * rabattre sur le bouton du navigateur — geste peu naturel, et inexistant pour
 * un visiteur arrivé par un lien direct. On revient à la page précédente quand
 * il y en a une, sinon vers une destination sûre.
 */
export default function BoutonRetour({
  vers,
  libelle = 'Retour',
}: {
  /** Destination de repli quand l'historique est vide (lien partagé, onglet neuf). */
  vers?: string;
  libelle?: string;
}) {
  const navigate = useNavigate();

  const revenir = () => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate(vers ?? '/');
    }
  };

  return (
    <Button
      onClick={revenir}
      startIcon={<ArrowBackIcon />}
      sx={{ alignSelf: 'flex-start', mb: 1 }}
    >
      {libelle}
    </Button>
  );
}
