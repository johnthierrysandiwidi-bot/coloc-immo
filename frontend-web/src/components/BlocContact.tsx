import { Box, Button, Card, CardContent, Stack, Typography } from '@mui/material';
import PhoneIcon from '@mui/icons-material/Phone';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';

/**
 * Bloc de contact direct.
 *
 * Affiche les numéros de la plateforme, appelables d'un clic (lien tel:) et
 * joignables sur WhatsApp. Sur un marché où la prise de contact passe d'abord
 * par le téléphone, c'est le moyen le plus direct de joindre l'équipe — plus
 * immédiat qu'un formulaire.
 */

// Numéros au format international, sans espaces pour les liens tel: et wa.me.
const NUMEROS = [
  { affichage: '+226 54 56 40 01', brut: '22654564001' },
  { affichage: '+226 71 49 05 08', brut: '22671490508' },
];

export default function BlocContact() {
  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Besoin d'aide ? Contactez-nous
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Une question sur une annonce ou sur la plateforme ? Appelez-nous
          directement.
        </Typography>

        <Stack spacing={1.5}>
          {NUMEROS.map((n) => (
            <Box key={n.brut} sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
              <Button
                startIcon={<PhoneIcon />}
                href={`tel:+${n.brut}`}
                variant="outlined"
                sx={{ flex: 1, justifyContent: 'flex-start' }}
              >
                {n.affichage}
              </Button>
              <Button
                startIcon={<WhatsAppIcon />}
                href={`https://wa.me/${n.brut}`}
                target="_blank"
                rel="noopener"
                variant="contained"
                color="success"
              >
                WhatsApp
              </Button>
            </Box>
          ))}
        </Stack>
      </CardContent>
    </Card>
  );
}
