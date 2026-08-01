export const ROLES = {
  ADMIN: 'ROLE_ADMIN',
  PROPRIETAIRE: 'ROLE_PROPRIETAIRE',
  DEMARCHEUR: 'ROLE_DEMARCHEUR',
  UTILISATEUR: 'ROLE_UTILISATEUR',
} as const;

export type Role = (typeof ROLES)[keyof typeof ROLES];

export type TypeAnnonce = 'VENTE' | 'LOCATION' | 'COLOCATION';
export type StatutAnnonce = 'BROUILLON' | 'PUBLIEE' | 'SUSPENDUE' | 'EXPIREE' | 'CLOTUREE';
export type StatutValidation = 'EN_ATTENTE' | 'VALIDE' | 'REFUSE';
export type StatutRendezVous = 'DEMANDE' | 'ACCEPTE' | 'REFUSE' | 'REPORTE' | 'ANNULE' | 'TERMINE';
export type SexeRecherche = 'HOMME' | 'FEMME' | 'INDIFFERENT';
export type StatutImmobilier =
  | 'BROUILLON'
  | 'DISPONIBLE'
  | 'PARTIELLEMENT_LOUE'
  | 'LOUE'
  | 'VENDU'
  | 'INDISPONIBLE'
  | 'ARCHIVE';

/** Statuts que le propriétaire peut appliquer en un clic depuis « Mes biens ». */
export const STATUTS_BIEN_PROPRIETAIRE: StatutImmobilier[] = [
  'DISPONIBLE', 'LOUE', 'VENDU', 'INDISPONIBLE',
];

export const LIBELLE_STATUT_BIEN: Record<StatutImmobilier, string> = {
  BROUILLON: 'Brouillon',
  DISPONIBLE: 'Disponible',
  PARTIELLEMENT_LOUE: 'Partiellement loué',
  LOUE: 'Loué',
  VENDU: 'Vendu',
  INDISPONIBLE: 'Indisponible',
  ARCHIVE: 'Archivé',
};

export const COULEUR_STATUT_BIEN: Record<
  StatutImmobilier,
  'success' | 'info' | 'default' | 'warning' | 'error'
> = {
  BROUILLON: 'default',
  DISPONIBLE: 'success',
  PARTIELLEMENT_LOUE: 'warning',
  LOUE: 'info',
  VENDU: 'default',
  INDISPONIBLE: 'warning',
  ARCHIVE: 'error',
};

export interface Localite { id: number; nom: string; }
export interface Quartier { id: number; nom: string; localite?: Localite; }
export interface TypeImmobilier { id: number; nom: string; }
export interface Equipement { id: number; nom: string; }
export interface ImageBien { id: number; url: string; principale?: boolean; ordre?: number; }

export interface Immobilier {
  id: number;
  nom: string;
  description?: string;
  adresse?: string;
  latitude?: number | null;
  longitude?: number | null;
  surface?: number;
  nombrePieces?: number;
  nombreChambres?: number;
  nombreSallesBain?: number;
  nombreSalons?: number;
  garage?: boolean;
  piscine?: boolean;
  meuble?: boolean;
  statut?: StatutImmobilier;
  localite?: Localite;
  quartier?: Quartier;
  typeImmobilier?: TypeImmobilier;
  images?: ImageBien[];
  proprietaire?: { id: number; login: string };
  demarcheur?: { id: number; login: string };
}

export interface DetailColocation {
  id?: number;
  nombrePlaces: number;
  placesRestantes: number;
  sexeRecherche: SexeRecherche;
  ageMin?: number;
  ageMax?: number;
  loyer: number;
  caution?: number;
  charges?: number;
  reglesDeVie?: string;
  equipements?: Equipement[];
}

export interface Annonce {
  id: number;
  titre: string;
  contenu?: string;
  type: TypeAnnonce;
  prix: number;
  nombreVues?: number;
  datePublication?: string;
  dateExpiration?: string;
  statut: StatutAnnonce;
  immobilier?: Immobilier;
  auteur?: { id: number; login: string };
  detailColocation?: DetailColocation;
  /** Photos remontées par le backend (PhotoAnnonceService), principale en premier. */
  photoUrl?: string;
  photos?: string[];
}

export interface DocumentDemarcheur {
  id: number;
  nom: string;
  url: string;
  statut: StatutValidation;
  motifRefus?: string;
  dateAjout?: string;
  demarcheur?: { id: number; login: string };
}

export interface RendezVous {
  id: number;
  dateHeure: string;
  dateReportee?: string;
  contenu?: string;
  motif?: string;
  statut: StatutRendezVous;
  annonce?: Annonce;
  demandeur?: { id: number; login: string };
}

export interface NotificationItem {
  id: number;
  type: string;
  titre: string;
  message: string;
  lien?: string;
  lue: boolean;
  dateCreation: string;
}

/** Filtres de recherche avancée (EF-10), mappés sur les Criteria JHipster. */
export interface FiltresRecherche {
  titre?: string;
  type?: TypeAnnonce;
  prixMin?: number;
  prixMax?: number;
  localiteId?: number;
  quartierId?: number;
  typeImmobilierId?: number;
  nombreChambresMin?: number;
  page?: number;
  size?: number;
  sort?: string;
}
