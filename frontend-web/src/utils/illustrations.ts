import type { Annonce } from '@/types';

/**
 * Une URL d'image est-elle digne de confiance ?
 * Seuls les fichiers hébergés par la plateforme le sont.
 */
export function imageFiable(url: string | null | undefined): boolean {
  if (!url) return false;
  return (
    url.startsWith('/api/files/') ||
    url.startsWith('/photos/') ||
    url.startsWith('/illustrations/')
  );
}

const P = (n: number) => `/photos/photo-${String(n).padStart(2, '0')}.jpg`;

/**
 * Fonds photographique classé par sujet.
 *
 * Le classement repose sur une analyse d'image (ciel, latérite, contraste) et
 * reste approximatif. La logique est donc conçue pour qu'une erreur soit sans
 * conséquence : la catégorie « terrain » est volontairement resserrée aux photos
 * les plus sûres, et une annonce de terrain puise dans un ensemble « extérieurs »
 * — jamais dans les intérieurs. Ainsi, même mal rangée, une photo d'extérieur sur
 * un terrain reste plausible, alors qu'une salle de bain ne le serait pas.
 */
const TERRAINS_STRICTS = [3, 7, 65, 66, 70];
const EXTERIEURS = [4, 5, 11, 12, 13, 15, 17, 21, 23, 24, 27, 29, 39, 45, 46, 51, 55, 56, 60, 64, 67, 69, 71, 72, 77, 80];
const INTERIEURS = [1, 2, 6, 8, 9, 10, 14, 16, 18, 19, 20, 22, 25, 26, 28, 30, 31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 47, 48, 49, 50, 52, 53, 54, 57, 58, 59, 61, 62, 63, 68, 73, 74, 75, 76, 78, 79, 81];

// Une annonce de terrain ne voit que des vues d'extérieur (parcelles + façades/cours).
const POOL_TERRAIN = [...TERRAINS_STRICTS, ...EXTERIEURS];

const PAR_ANNONCE = 4;

function estTerrain(annonce: Pick<Annonce, 'titre' | 'type'>): boolean {
  const t = `${annonce.titre ?? ''} ${annonce.type ?? ''}`.toLowerCase();
  return t.includes('terrain') || t.includes('parcelle');
}

function choisir(source: number[], n: number, graine: number): string[] {
  const debut = graine % source.length;
  return Array.from({ length: n }, (_, i) => P(source[(debut + i) % source.length]));
}

/** Série d'une annonce : un extérieur d'abord, puis l'intérieur (sauf terrain). */
export function serieIllustrations(annonce: Pick<Annonce, 'id' | 'type' | 'titre'>): string[] {
  const graine = Math.abs(Number(annonce.id) || 0);
  if (estTerrain(annonce)) {
    return choisir(POOL_TERRAIN, Math.min(3, POOL_TERRAIN.length), graine);
  }
  return [
    ...choisir(EXTERIEURS, 1, graine),
    ...choisir(INTERIEURS, PAR_ANNONCE - 1, graine * 3),
  ];
}

/** Visuel principal, utilisé sur les cartes du catalogue. */
export function illustrationParType(annonce: Pick<Annonce, 'id' | 'type' | 'titre'>): string {
  return serieIllustrations(annonce)[0];
}

/** Photo de l'annonce si elle est fiable, sinon une photo du fonds. */
export function visuelAnnonce(
  annonce: Pick<Annonce, 'id' | 'type' | 'titre'>,
  url: string | null | undefined,
): string {
  return imageFiable(url) ? (url as string) : illustrationParType(annonce);
}
