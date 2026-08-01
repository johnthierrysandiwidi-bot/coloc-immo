import { createTheme } from '@mui/material/styles';

/**
 * Système de design ColocImmo.
 *
 * Identité : plateforme immobilière de confiance à Ouagadougou.
 * - Vert profond « bosquet » comme couleur de confiance et de stabilité.
 * - Ocre chaud inspiré de la latérite sahélienne, en accent mesuré.
 * - Fond crème tiède plutôt qu'un gris froid, pour l'accueil.
 * Toute l'apparence dérive de ce thème : aucune retouche page par page.
 */

const FAMILLE_TITRE = '"Poppins", "Inter", system-ui, sans-serif';
const FAMILLE_CORPS = '"Inter", system-ui, -apple-system, sans-serif';

const typography = {
  fontFamily: FAMILLE_CORPS,
  h1: { fontFamily: FAMILLE_TITRE, fontWeight: 800, letterSpacing: '-0.03em', lineHeight: 1.05 },
  h2: { fontFamily: FAMILLE_TITRE, fontWeight: 800, letterSpacing: '-0.025em', lineHeight: 1.1 },
  h3: { fontFamily: FAMILLE_TITRE, fontWeight: 700, letterSpacing: '-0.02em' },
  h4: { fontFamily: FAMILLE_TITRE, fontWeight: 700, letterSpacing: '-0.02em' },
  h5: { fontFamily: FAMILLE_TITRE, fontWeight: 600, letterSpacing: '-0.01em' },
  h6: { fontFamily: FAMILLE_TITRE, fontWeight: 600 },
  subtitle1: { fontWeight: 500 },
  button: { textTransform: 'none' as const, fontWeight: 600, letterSpacing: '0.01em' },
};

const shape = { borderRadius: 14 };

// Ombres douces et intentionnelles, remplaçant les bordures plates.
const ombreDouce = '0 1px 2px rgba(16,24,40,0.04), 0 4px 16px rgba(16,24,40,0.06)';
const ombreCarte = '0 1px 3px rgba(16,24,40,0.06), 0 8px 24px rgba(16,24,40,0.05)';

const composants = (mode: 'light' | 'dark') => ({
  MuiCssBaseline: {
    styleOverrides: {
      body: { scrollbarWidth: 'thin' as const },
    },
  },
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: 16,
        border: mode === 'light' ? '1px solid rgba(16,24,40,0.06)' : '1px solid rgba(255,255,255,0.08)',
        boxShadow: ombreCarte,
        transition: 'transform .18s ease, box-shadow .18s ease',
      },
    },
  },
  MuiButton: {
    defaultProps: { disableElevation: true },
    styleOverrides: {
      root: { borderRadius: 12, paddingInline: 18, paddingBlock: 9 },
      containedPrimary: { boxShadow: ombreDouce },
      sizeLarge: { paddingInline: 26, paddingBlock: 13, fontSize: '1rem' },
    },
  },
  MuiTextField: {
    defaultProps: { variant: 'outlined' as const },
  },
  MuiOutlinedInput: {
    styleOverrides: { root: { borderRadius: 12 } },
  },
  MuiPaper: {
    styleOverrides: { rounded: { borderRadius: 16 } },
  },
  MuiChip: {
    styleOverrides: { root: { fontWeight: 600, borderRadius: 8 } },
  },
  MuiAppBar: {
    styleOverrides: { root: { boxShadow: ombreDouce } },
  },
  MuiTableHead: {
    styleOverrides: {
      root: { '& .MuiTableCell-head': { fontWeight: 700, letterSpacing: '0.01em' } },
    },
  },
});

export const themeClair = createTheme({
  typography,
  shape,
  palette: {
    mode: 'light',
    primary:   { main: '#0f5c43', light: '#2f7d62', dark: '#0a4433', contrastText: '#ffffff' },
    secondary: { main: '#c26a2e', light: '#d98a52', dark: '#9a5122', contrastText: '#ffffff' },
    success:   { main: '#1a7f52' },
    warning:   { main: '#c98a2b' },
    error:     { main: '#c0392b' },
    info:      { main: '#2a6f97' },
    background: { default: '#f6f4ee', paper: '#ffffff' },
    text: { primary: '#1b2420', secondary: '#5a6560' },
    divider: 'rgba(16,24,40,0.08)',
  },
  components: composants('light'),
});

export const themeSombre = createTheme({
  typography,
  shape,
  palette: {
    mode: 'dark',
    primary:   { main: '#4cb98c', light: '#6fcaa4', dark: '#2f8c64', contrastText: '#08120d' },
    secondary: { main: '#e0975a', light: '#e9b183', dark: '#b6753f' },
    success:   { main: '#4cb98c' },
    warning:   { main: '#e0b05a' },
    error:     { main: '#e57367' },
    info:      { main: '#6aa9cf' },
    background: { default: '#101512', paper: '#181f1b' },
    text: { primary: '#eef2ef', secondary: '#9fada7' },
    divider: 'rgba(255,255,255,0.09)',
  },
  components: composants('dark'),
});
