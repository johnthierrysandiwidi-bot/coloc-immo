import {
  Box, Collapse, Divider, Drawer, List, ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography,
} from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import SearchIcon from '@mui/icons-material/Search';
import FavoriteIcon from '@mui/icons-material/Favorite';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import EventIcon from '@mui/icons-material/Event';
import ChatIcon from '@mui/icons-material/ChatBubbleOutline';
import DescriptionIcon from '@mui/icons-material/Description';
import ApartmentIcon from '@mui/icons-material/Apartment';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import GroupIcon from '@mui/icons-material/Group';
import PersonIcon from '@mui/icons-material/Person';
import DashboardIcon from '@mui/icons-material/Dashboard';
import HolidayVillageIcon from '@mui/icons-material/HolidayVillage';
import CampaignIcon from '@mui/icons-material/Campaign';
import BarChartIcon from '@mui/icons-material/BarChart';
import PaymentsIcon from '@mui/icons-material/Payments';
import LocationCityIcon from '@mui/icons-material/LocationCity';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import MapsHomeWorkIcon from '@mui/icons-material/MapsHomeWork';
import { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';
import { ROLES, type Role } from '@/types';

interface Entree {
  libelle: string;
  chemin: string;
  enfants?: Entree[];
  icone: React.ReactNode;
  roles?: Role[]; // absent = visible par tous les connectés
}

const ENTREES: Entree[] = [
  { libelle: 'Tableau de bord', chemin: '/tableau-de-bord', icone: <DashboardIcon /> },
  { libelle: 'Rechercher', chemin: '/annonces', icone: <SearchIcon /> },
  { libelle: 'Mes favoris', chemin: '/favoris', icone: <FavoriteIcon /> },
  { libelle: 'Mes alertes', chemin: '/alertes', icone: <NotificationsActiveIcon /> },
  { libelle: 'Mes rendez-vous', chemin: '/rendez-vous', icone: <EventIcon /> },
  { libelle: 'Messages', chemin: '/messages', icone: <ChatIcon /> },
  {
    libelle: 'Immobilier',
    chemin: '/mes-biens',
    icone: <MapsHomeWorkIcon />,
    roles: [ROLES.PROPRIETAIRE, ROLES.DEMARCHEUR, ROLES.ADMIN],
    enfants: [
      { libelle: 'Mes biens', chemin: '/mes-biens', icone: <ApartmentIcon /> },
      { libelle: 'Mes annonces', chemin: '/mes-annonces', icone: <HomeIcon /> },
    ],
  },
  {
    libelle: 'Statistiques',
    chemin: '/statistiques',
    icone: <BarChartIcon />,
    roles: [ROLES.PROPRIETAIRE, ROLES.DEMARCHEUR, ROLES.ADMIN],
  },
  {
    libelle: 'Mes documents',
    chemin: '/mes-documents',
    icone: <DescriptionIcon />,
    roles: [ROLES.DEMARCHEUR],
  },
  {
    libelle: 'Validation documents',
    chemin: '/admin/documents',
    icone: <VerifiedUserIcon />,
    roles: [ROLES.ADMIN],
  },
  {
    libelle: 'Utilisateurs',
    chemin: '/admin/utilisateurs',
    icone: <GroupIcon />,
    roles: [ROLES.ADMIN],
  },
  {
    libelle: 'Biens (admin)',
    chemin: '/admin/biens',
    icone: <HolidayVillageIcon />,
    roles: [ROLES.ADMIN],
  },
  {
    libelle: 'Annonces (admin)',
    chemin: '/admin/annonces',
    icone: <HomeIcon />,
    roles: [ROLES.ADMIN],
  },
  {
    libelle: 'Rendez-vous (admin)',
    chemin: '/admin/rendez-vous',
    icone: <EventIcon />,
    roles: [ROLES.ADMIN],
  },
  {
    libelle: 'Villes & quartiers',
    chemin: '/admin/villes-quartiers',
    icone: <LocationCityIcon />,
    roles: [ROLES.ADMIN],
  },
  {
    libelle: 'Diffuser notification',
    chemin: '/admin/notifications',
    icone: <CampaignIcon />,
    roles: [ROLES.ADMIN],
  },
  {
    libelle: 'Paiements',
    chemin: '/admin/paiements',
    icone: <PaymentsIcon />,
    roles: [ROLES.ADMIN],
  },
  { libelle: 'Mon profil', chemin: '/profil', icone: <PersonIcon /> },
];

interface Props {
  largeur: number;
  ouvert: boolean;
  onFermer: () => void;
}

function GroupeMenu({ entree, onFermer }: { entree: Entree; onFermer: () => void }) {
  const location = useLocation();
  const enfantActif = entree.enfants?.some((c) => location.pathname.startsWith(c.chemin)) ?? false;
  const [ouvert, setOuvert] = useState(enfantActif);

  return (
    <>
      <ListItemButton onClick={() => setOuvert((o) => !o)} sx={{ borderRadius: 2, mb: 0.5 }}>
        <ListItemIcon sx={{ minWidth: 40 }}>{entree.icone}</ListItemIcon>
        <ListItemText primary={entree.libelle} primaryTypographyProps={{ fontSize: 14 }} />
        {ouvert ? <ExpandLess fontSize="small" /> : <ExpandMore fontSize="small" />}
      </ListItemButton>
      <Collapse in={ouvert} timeout="auto" unmountOnExit>
        <List disablePadding sx={{ pl: 2 }}>
          {entree.enfants!.map((c) => (
            <ListItemButton
              key={c.chemin}
              component={NavLink}
              to={c.chemin}
              onClick={onFermer}
              sx={{ borderRadius: 2, mb: 0.5, '&.active': { bgcolor: 'action.selected', fontWeight: 600 } }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{c.icone}</ListItemIcon>
              <ListItemText primary={c.libelle} primaryTypographyProps={{ fontSize: 14 }} />
            </ListItemButton>
          ))}
        </List>
      </Collapse>
    </>
  );
}

export default function Sidebar({ largeur, ouvert, onFermer }: Props) {
  const mesRoles = useAppSelector((s) => s.auth.roles);
  const visibles = ENTREES.filter((e) => !e.roles || e.roles.some((r) => mesRoles.includes(r)));

  const contenu = (
    <Box>
      <Toolbar sx={{ px: 2.5 }}>
        <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: '-0.02em' }}>
          Coloc<span style={{ opacity: 0.5 }}>Immo</span>
        </Typography>
      </Toolbar>
      <Divider />
      <List sx={{ px: 1.5, py: 1 }}>
        {visibles.map((e) =>
          e.enfants ? (
            <GroupeMenu key={e.libelle} entree={e} onFermer={onFermer} />
          ) : (
            <ListItemButton
              key={e.chemin}
              component={NavLink}
              to={e.chemin}
              end={e.chemin === '/tableau-de-bord'}
              onClick={onFermer}
              sx={{
                borderRadius: 2,
                mb: 0.5,
                '&.active': { bgcolor: 'action.selected', fontWeight: 600 },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{e.icone}</ListItemIcon>
              <ListItemText primary={e.libelle} primaryTypographyProps={{ fontSize: 14 }} />
            </ListItemButton>
          )
        )}
      </List>
    </Box>
  );

  return (
    <Box component="nav" sx={{ width: { md: largeur }, flexShrink: { md: 0 } }}>
      <Drawer
        variant="temporary"
        open={ouvert}
        onClose={onFermer}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { width: largeur, boxSizing: 'border-box' },
        }}
      >
        {contenu}
      </Drawer>
      <Drawer
        variant="permanent"
        open
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { width: largeur, boxSizing: 'border-box', borderRight: '1px solid', borderColor: 'divider' },
        }}
      >
        {contenu}
      </Drawer>
    </Box>
  );
}
