import { Box, Button, Card, CardContent, Link, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import PlaceIcon from '@mui/icons-material/Place';

/**
 * Carte de localisation d'un bien.
 *
 * Affiche l'emplacement sur OpenStreetMap. Le choix d'une iframe OSM plutôt que
 * d'une bibliothèque (Leaflet, Google Maps) est délibéré : aucune dépendance à
 * installer, aucune clé d'API à gérer, et un rendu immédiat. Quand les
 * coordonnées manquent, on se rabat sur une recherche par quartier et ville,
 * qui suffit à situer le bien.
 */
export default function CarteLocalisation({
  latitude,
  longitude,
  libelle,
  quartier,
  ville,
}: {
  latitude?: number | null;
  longitude?: number | null;
  libelle?: string;
  quartier?: string;
  ville?: string;
}) {
  const aCoordonnees = latitude != null && longitude != null;

  // Suivi du chargement de l'iframe pour proposer un repli si le réseau traîne.
  const [chargee, setChargee] = useState(false);
  const [attenteDepassee, setAttenteDepassee] = useState(false);
  useEffect(() => {
    const t = setTimeout(() => setAttenteDepassee(true), 6000);
    return () => clearTimeout(t);
  }, []);

  // Fenêtre cadrée autour du point, ou recherche textuelle à défaut.
  const src = aCoordonnees
    ? `https://www.openstreetmap.org/export/embed.html?bbox=${longitude! - 0.008}%2C${
        latitude! - 0.006
      }%2C${longitude! + 0.008}%2C${latitude! + 0.006}&layer=mapnik&marker=${latitude}%2C${longitude}`
    : `https://www.openstreetmap.org/export/embed.html?bbox=-1.60%2C12.30%2C-1.42%2C12.42&layer=mapnik`;

  const lienExterne = aCoordonnees
    ? `https://www.openstreetmap.org/?mlat=${latitude}&mlon=${longitude}#map=16/${latitude}/${longitude}`
    : `https://www.openstreetmap.org/search?query=${encodeURIComponent(
        [quartier, ville, 'Burkina Faso'].filter(Boolean).join(', '),
      )}`;

  return (
    <Card sx={{ mb: 2 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          <PlaceIcon color="primary" fontSize="small" />
          Localisation
        </Typography>

        {!aCoordonnees && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            L'emplacement exact n'a pas été renseigné. La carte situe le quartier
            {quartier ? ` de ${quartier}` : ''}.
          </Typography>
        )}

        <Box sx={{ position: 'relative' }}>
          <Box
            component="iframe"
            title={`Carte — ${libelle ?? 'localisation du bien'}`}
            src={src}
            loading="lazy"
            onLoad={() => setChargee(true)}
            sx={{
              width: '100%',
              height: 260,
              border: 0,
              borderRadius: 1,
              display: 'block',
              bgcolor: 'action.hover',
            }}
          />
          {/* Repli réseau : si la carte n'a pas fini de charger après quelques
              secondes — connexion lente ou coupée — on invite à l'ouvrir dans
              l'application de cartes, qui gère mieux les réseaux instables. */}
          {!chargee && attenteDepassee && (
            <Box
              sx={{
                position: 'absolute',
                inset: 0,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 1,
                bgcolor: 'background.paper',
                borderRadius: 1,
                p: 2,
                textAlign: 'center',
              }}
            >
              <Typography variant="body2" color="text.secondary">
                La carte est longue à charger. Vérifiez votre connexion, ou
                ouvrez la localisation dans votre application de cartes.
              </Typography>
              <Button
                variant="contained"
                size="small"
                startIcon={<PlaceIcon />}
                href={lienExterne}
                target="_blank"
                rel="noopener"
              >
                Ouvrir la carte
              </Button>
            </Box>
          )}
        </Box>

        <Typography variant="caption" sx={{ mt: 1, display: 'block' }}>
          <Link href={lienExterne} target="_blank" rel="noopener">
            Ouvrir dans OpenStreetMap
          </Link>
        </Typography>
      </CardContent>
    </Card>
  );
}
