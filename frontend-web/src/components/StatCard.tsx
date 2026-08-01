import { Card, CardContent, Stack, Typography, Box } from '@mui/material';

interface Props {
  libelle: string;
  valeur: number | string;
  icone?: React.ReactNode;
  couleur?: string;
}

export default function StatCard({ libelle, valeur, icone, couleur = 'primary.main' }: Props) {
  return (
    <Card>
      <CardContent>
        <Stack direction="row" alignItems="center" spacing={2}>
          {icone && (
            <Box
              sx={{
                width: 44,
                height: 44,
                borderRadius: 2,
                bgcolor: couleur,
                color: '#fff',
                display: 'grid',
                placeItems: 'center',
              }}
            >
              {icone}
            </Box>
          )}
          <Box>
            <Typography variant="h5">{valeur}</Typography>
            <Typography variant="caption" color="text.secondary">
              {libelle}
            </Typography>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}
