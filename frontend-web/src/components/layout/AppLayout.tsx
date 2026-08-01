import { useState } from 'react';
import { Box, Toolbar } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import Footer from './Footer';

const LARGEUR_SIDEBAR = 260;

interface Props {
  modeSombre: boolean;
  basculerTheme: () => void;
}

export default function AppLayout({ modeSombre, basculerTheme }: Props) {
  const [ouvert, setOuvert] = useState(false);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Navbar
        largeurSidebar={LARGEUR_SIDEBAR}
        onOuvrirMenu={() => setOuvert(!ouvert)}
        modeSombre={modeSombre}
        basculerTheme={basculerTheme}
      />
      <Sidebar largeur={LARGEUR_SIDEBAR} ouvert={ouvert} onFermer={() => setOuvert(false)} />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${LARGEUR_SIDEBAR}px)` },
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Toolbar />
        <Box sx={{ p: { xs: 2, md: 3 }, flexGrow: 1 }}>
          <Outlet />
        </Box>
        <Footer />
      </Box>
    </Box>
  );
}
