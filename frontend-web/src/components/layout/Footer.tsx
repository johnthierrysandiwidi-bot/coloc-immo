import { Box, Link, Stack, Typography } from '@mui/material';
import PhoneIcon from '@mui/icons-material/Phone';

// Numéros de contact de la plateforme (Burkina Faso, indicatif +226).
const CONTACTS = ['+226 54 56 40 01', '+226 71 49 05 08'];

export default function Footer() {
  return (
    <Box
      component="footer"
      sx={{ px: 3, py: 2.5, borderTop: '1px solid', borderColor: 'divider', textAlign: 'center' }}
    >
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={{ xs: 0.5, sm: 3 }}
        justifyContent="center"
        alignItems="center"
        sx={{ mb: 1 }}
      >
        {CONTACTS.map((numero) => (
          <Link
            key={numero}
            href={`tel:${numero.replace(/\s/g, '')}`}
            underline="hover"
            sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, color: 'text.secondary' }}
          >
            <PhoneIcon sx={{ fontSize: 16 }} />
            <Typography variant="body2">{numero}</Typography>
          </Link>
        ))}
      </Stack>
      <Typography variant="caption" color="text.secondary">
        ColocImmo — Ouagadougou, Burkina Faso — {new Date().getFullYear()}
      </Typography>
    </Box>
  );
}
