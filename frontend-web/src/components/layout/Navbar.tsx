import { AppBar, Box, IconButton, Toolbar, Typography, Badge, Menu, MenuItem, Avatar } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { deconnexion } from '@/features/auth/authSlice';
import { notificationsApi } from '@/api/services';

interface Props {
  largeurSidebar: number;
  onOuvrirMenu: () => void;
  modeSombre: boolean;
  basculerTheme: () => void;
}

export default function Navbar({ largeurSidebar, onOuvrirMenu, modeSombre, basculerTheme }: Props) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { login } = useAppSelector((s) => s.auth);
  const [nonLues, setNonLues] = useState(0);
  const [ancre, setAncre] = useState<null | HTMLElement>(null);

  useEffect(() => {
    if (!login) return;
    notificationsApi
      .mesNotifications()
      .then((ns) => setNonLues(ns.filter((n) => !n.lue).length))
      .catch(() => setNonLues(0));
  }, [login]);

  return (
    <AppBar
      position="fixed"
      elevation={0}
      color="inherit"
      sx={{
        width: { md: `calc(100% - ${largeurSidebar}px)` },
        ml: { md: `${largeurSidebar}px` },
        borderBottom: '1px solid',
        borderColor: 'divider',
      }}
    >
      <Toolbar>
        <IconButton edge="start" onClick={onOuvrirMenu} sx={{ mr: 2, display: { md: 'none' } }}>
          <MenuIcon />
        </IconButton>

        <Box sx={{ flexGrow: 1 }} />

        <IconButton onClick={basculerTheme} aria-label="Basculer le thème">
          {modeSombre ? <LightModeIcon /> : <DarkModeIcon />}
        </IconButton>

        {login && (
          <>
            <IconButton onClick={() => navigate('/notifications')} aria-label="Notifications">
              <Badge badgeContent={nonLues} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>

            <IconButton onClick={(e) => setAncre(e.currentTarget)}>
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main', fontSize: 14 }}>
                {login.slice(0, 2).toUpperCase()}
              </Avatar>
            </IconButton>
            <Menu anchorEl={ancre} open={Boolean(ancre)} onClose={() => setAncre(null)}>
              <MenuItem disabled>
                <Typography variant="body2">{login}</Typography>
              </MenuItem>
              <MenuItem
                onClick={() => {
                  setAncre(null);
                  dispatch(deconnexion()).then(() => navigate('/login'));
                }}
              >
                Se déconnecter
              </MenuItem>
            </Menu>
          </>
        )}
      </Toolbar>
    </AppBar>
  );
}
