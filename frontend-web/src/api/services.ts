import { api } from './client';
import type {
  Annonce, DocumentDemarcheur, Equipement, FiltresRecherche, ImageBien,
  Immobilier, Localite, NotificationItem, Quartier, RendezVous, TypeImmobilier,
} from '@/types';

export interface Page<T> {
  contenu: T[];
  total: number;
}

/** JHipster renvoie le total dans l'en-tête X-Total-Count. */
async function paginer<T>(url: string, params: Record<string, unknown>): Promise<Page<T>> {
  const res = await api.get<T[]>(url, { params });
  return { contenu: res.data, total: Number(res.headers['x-total-count'] ?? res.data.length) };
}

/** Traduit nos filtres métier en Criteria JHipster (champ.operateur=valeur). */
function versCriteria(f: FiltresRecherche): Record<string, unknown> {
  const p: Record<string, unknown> = {
    page: f.page ?? 0,
    size: f.size ?? 12,
    sort: f.sort ?? 'datePublication,desc',
    'statut.equals': 'PUBLIEE',
  };
  if (f.titre) p['titre.contains'] = f.titre;
  if (f.type) p['type.equals'] = f.type;
  if (f.prixMin != null) p['prix.greaterThanOrEqual'] = f.prixMin;
  if (f.prixMax != null) p['prix.lessThanOrEqual'] = f.prixMax;
  // Les critères portant sur le bien passent par la relation immobilier (Criteria JHipster)
  if (f.typeImmobilierId != null) p['immobilierId.equals'] = undefined;
  return p;
}

export const annoncesApi = {
  rechercher: (f: FiltresRecherche) => paginer<Annonce>('/annonces', versCriteria(f)),
  parId: (id: number) => api.get<Annonce>(`/annonces/${id}`).then((r) => r.data),
  mesAnnonces: (userId: number) =>
    paginer<Annonce>('/annonces', { 'auteurId.equals': userId, size: 100 }),
  creer: (a: Partial<Annonce>) => api.post<Annonce>('/annonces', a).then((r) => r.data),
  publier: (id: number) => api.patch<Annonce>(`/annonces/${id}/publier`).then((r) => r.data),
  depublier: (id: number) => api.patch<Annonce>(`/annonces/${id}/depublier`).then((r) => r.data),
  archiver: (id: number) => api.patch<Annonce>(`/annonces/${id}/archiver`).then((r) => r.data),
  renouveler: (id: number) => api.patch<Annonce>(`/annonces/${id}/renouveler`).then((r) => r.data),
  supprimer: (id: number) => api.delete(`/annonces/${id}`),
  /** Vue admin : toutes les annonces, tous statuts confondus (pas seulement PUBLIEE). */
  toutesAdmin: (params: { titre?: string; type?: string; statut?: string }) =>
    api
      .get<Annonce[]>('/annonces', {
        params: {
          size: 200,
          sort: 'id,desc',
          ...(params.titre ? { 'titre.contains': params.titre } : {}),
          ...(params.type ? { 'type.equals': params.type } : {}),
          ...(params.statut ? { 'statut.equals': params.statut } : {}),
        },
      })
      .then((r) => r.data),
};

export const rendezVousApi = {
  demander: (annonceId: number, dateSouhaitee: string, message?: string) =>
    api.post<RendezVous>('/rendez-vous/demander', { annonceId, dateSouhaitee, message }).then((r) => r.data),
  lister: () => paginer<RendezVous>('/rendez-vous', { size: 100, sort: 'dateHeure,desc' }),
  accepter: (id: number) => api.patch<RendezVous>(`/rendez-vous/${id}/accepter`).then((r) => r.data),
  refuser: (id: number, motif: string) =>
    api.patch<RendezVous>(`/rendez-vous/${id}/refuser`, { motif }).then((r) => r.data),
  reporter: (id: number, nouvelleDate: string) =>
    api.patch<RendezVous>(`/rendez-vous/${id}/reporter`, { nouvelleDate }).then((r) => r.data),
  /** Clôture d'une visite réalisée. Confirmée par le locataire, elle libère les frais. */
  terminer: (id: number) => api.patch<RendezVous>(`/rendez-vous/${id}/terminer`).then((r) => r.data),
  annuler: (id: number, motif?: string) =>
    api.patch<RendezVous>(`/rendez-vous/${id}/annuler`, { motif }).then((r) => r.data),
  /** Vue admin : tous les rendez-vous, tous demandeurs confondus. */
  tousAdmin: (params: { statut?: string }) =>
    api
      .get<RendezVous[]>('/rendez-vous', {
        params: {
          size: 200,
          sort: 'dateHeure,desc',
          ...(params.statut ? { 'statut.equals': params.statut } : {}),
        },
      })
      .then((r) => r.data),
};

export const documentsApi = {
  mesDocuments: (userId: number) =>
    api.get<DocumentDemarcheur[]>('/documents', { params: { 'demarcheurId.equals': userId, size: 100 } }).then((r) => r.data),
  remplacer: (id: number, url: string, nom?: string) =>
    api.put<DocumentDemarcheur>(`/documents/${id}/remplacer`, { url, nom }).then((r) => r.data),
  retirer: (id: number) => api.delete(`/documents/${id}/retirer`),
  telecharger: (id: number) => api.get<string>(`/documents/${id}/telecharger`).then((r) => r.data),
  enAttente: () =>
    api.get<DocumentDemarcheur[]>('/documents', { params: { 'statut.equals': 'EN_ATTENTE', size: 100 } }).then((r) => r.data),
  typesDocument: () =>
    api.get<{ id: number; nom: string }[]>('/type-documents', { params: { size: 50 } }).then((r) => r.data),

  televerser: async (fichier: File, nom: string, typeDocumentId: number, demarcheurId: number) => {
    const form = new FormData();
    form.append('file', fichier);
    const { data } = await api.post<{ url: string }>('/files/documents', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return api
      .post<DocumentDemarcheur>('/documents', {
        nom,
        url: data.url,
        statut: 'EN_ATTENTE',
        dateAjout: new Date().toISOString(),
        typeDocument: { id: typeDocumentId },
        demarcheur: { id: demarcheurId },
      })
      .then((r) => r.data);
  },
  valider: (id: number) => api.patch<DocumentDemarcheur>(`/documents/${id}/valider`).then((r) => r.data),
  refuser: (id: number, motif: string) =>
    api.patch<DocumentDemarcheur>(`/documents/${id}/refuser`, { motif }).then((r) => r.data),
};

export const favorisApi = {
  mesFavoris: () => api.get('/favoris', { params: { size: 100 } }).then((r) => r.data),
  ajouter: (annonceId: number, utilisateurId: number) =>
    api.post('/favoris', {
      dateAjout: new Date().toISOString(),
      annonce: { id: annonceId },
      utilisateur: { id: utilisateurId },
    }),
  retirer: (id: number) => api.delete(`/favoris/${id}`),
};

export const alertesApi = {
  mesAlertes: () => api.get('/alertes', { params: { size: 100 } }).then((r) => r.data),
  creer: (a: Record<string, unknown>) => api.post('/alertes', a).then((r) => r.data),
  supprimer: (id: number) => api.delete(`/alertes/${id}`),
};

export const notificationsApi = {
  mesNotifications: () =>
    api.get<NotificationItem[]>('/notifications', { params: { size: 50, sort: 'dateCreation,desc' } }).then((r) => r.data),
  marquerLue: (n: NotificationItem) => api.put(`/notifications/${n.id}`, { ...n, lue: true }),
  marquerToutesLues: () => api.patch('/notifications/tout-lire'),
  supprimer: (id: number) => api.delete(`/notifications/${id}`),
};

export interface PointPrix {
  prix: number;
  charges?: number;
  periodicite?: string;
  dateEffet: string;
  description?: string;
}

export const referentielApi = {
  localites: () => api.get<Localite[]>('/localites', { params: { size: 200 } }).then((r) => r.data),
  quartiers: () => api.get<Quartier[]>('/quartiers', { params: { size: 500 } }).then((r) => r.data),
  typesImmobilier: () => api.get<TypeImmobilier[]>('/type-immobiliers', { params: { size: 100 } }).then((r) => r.data),
  equipements: () => api.get<Equipement[]>('/equipements', { params: { size: 100 } }).then((r) => r.data),

  creerLocalite: (nom: string) => api.post<Localite>('/localites', { nom }).then((r) => r.data),
  modifierLocalite: (id: number, nom: string) => api.put<Localite>(`/localites/${id}`, { id, nom }).then((r) => r.data),
  supprimerLocalite: (id: number) => api.delete(`/localites/${id}`),

  creerQuartier: (nom: string, localiteId: number) =>
    api.post<Quartier>('/quartiers', { nom, localite: { id: localiteId } }).then((r) => r.data),
  modifierQuartier: (id: number, nom: string, localiteId: number) =>
    api.put<Quartier>(`/quartiers/${id}`, { id, nom, localite: { id: localiteId } }).then((r) => r.data),
  supprimerQuartier: (id: number) => api.delete(`/quartiers/${id}`),
};

export const statistiquesApi = {
  administrateur: () => api.get<Record<string, unknown>>('/statistiques/administrateur').then((r) => r.data),
  bailleur: () => api.get<Record<string, unknown>>('/statistiques/bailleur').then((r) => r.data),
  utilisateur: () => api.get<Record<string, unknown>>('/statistiques/utilisateur').then((r) => r.data),
};

export const adminApi = {
  utilisateurs: () => api.get('/admin/users', { params: { size: 200 } }).then((r) => r.data),
  suspendre: (id: number) => api.patch(`/admin/utilisateurs/${id}/suspendre`),
  reactiver: (id: number) => api.patch(`/admin/utilisateurs/${id}/reactiver`),
  attribuerRole: (id: number, role: string) => api.patch(`/admin/utilisateurs/${id}/role`, { role }),
  notifier: (id: number, titre: string, message: string) =>
    api.post(`/admin/utilisateurs/${id}/notifier`, { titre, message }),
  /**
   * Diffusion de masse. Il n'existe pas d'endpoint de diffusion côté serveur :
   * on cible la liste d'utilisateurs concernés puis on notifie un par un.
   * Renvoie le nombre de destinataires effectivement notifiés.
   */
  diffuser: async (
    cible: 'TOUS' | 'ROLE_PROPRIETAIRE' | 'ROLE_DEMARCHEUR',
    titre: string,
    message: string,
  ): Promise<number> => {
    const utilisateurs = await api.get<{ id: number; activated: boolean; authorities: string[] }[]>('/admin/users', {
      params: { size: 500 },
    }).then((r) => r.data);
    const destinataires = utilisateurs.filter(
      (u) => u.activated && (cible === 'TOUS' || (u.authorities ?? []).includes(cible)),
    );
    await Promise.all(destinataires.map((u) => api.post(`/admin/utilisateurs/${u.id}/notifier`, { titre, message })));
    return destinataires.length;
  },
};

export const biensApi = {
  /**
   * Les biens de l'utilisateur : ceux qu'il possède ET ceux qu'il gère comme démarcheur.
   * Les critères JHipster se combinent en ET : impossible d'exprimer un OU en une requête.
   * On fait donc deux appels et on fusionne, en dédoublonnant.
   */
  mesBiens: async (userId: number) => {
    const [possedes, geres] = await Promise.all([
      api.get('/immobiliers', { params: { 'proprietaireId.equals': userId, size: 100 } }).then((r) => r.data),
      api.get('/immobiliers', { params: { 'demarcheurId.equals': userId, size: 100 } }).then((r) => r.data),
    ]);
    const parId = new Map<number, Immobilier>();
    [...possedes, ...geres].forEach((b: Immobilier) => parId.set(b.id, b));
    return [...parId.values()];
  },
  parId: (id: number) => api.get(`/immobiliers/${id}`).then((r) => r.data),
  historiquePrix: (id: number) =>
    api.get<PointPrix[]>(`/immobiliers/${id}/historique-prix`).then((r) => r.data).catch(() => []),
  creer: (b: Record<string, unknown>) => api.post('/immobiliers', b).then((r) => r.data),
  modifier: (id: number, b: Record<string, unknown>) => api.put(`/immobiliers/${id}`, b).then((r) => r.data),
  supprimer: (id: number) => api.delete(`/immobiliers/${id}`),
  /** Démarcheurs vérifiés pouvant être mandatés sur un bien. */
  demarcheursDisponibles: () =>
    api.get<{ id: number; login: string }[]>('/immobiliers/demarcheurs-disponibles').then((r) => r.data),
  /** Confie un bien à un démarcheur vérifié. */
  mandater: (bienId: number, demarcheurId: number) =>
    api.patch(`/immobiliers/${bienId}/demarcheur/${demarcheurId}`).then((r) => r.data),
  /** Retire le mandat en cours sur un bien. */
  retirerMandat: (bienId: number) =>
    api.delete(`/immobiliers/${bienId}/demarcheur`).then((r) => r.data),
  ajouterImage: (immobilierId: number, url: string, principale: boolean, ordre: number) =>
    api.post<ImageBien>('/images', { url, principale, ordre, immobilier: { id: immobilierId } }).then((r) => r.data),
  /** Photos d'un bien, triées par ordre croissant. */
  imagesParBien: (immobilierId: number) =>
    api
      .get<ImageBien[]>('/images', { params: { 'immobilierId.equals': immobilierId, size: 100, sort: 'ordre,asc' } })
      .then((r) => r.data),
  supprimerImage: (id: number) => api.delete(`/images/${id}`),
  /** Met à jour ordre et/ou statut « principale » d'une photo (JSON Merge Patch). */
  modifierImage: (id: number, patch: { ordre?: number; principale?: boolean }) =>
    api
      .patch<ImageBien>(`/images/${id}`, { id, ...patch }, { headers: { 'Content-Type': 'application/merge-patch+json' } })
      .then((r) => r.data),
  /** Définit une photo comme principale (dé-marque les autres du même bien côté serveur). */
  definirPrincipale: (id: number) => api.patch(`/images/${id}/principale`),
  /** Réordonne en une requête : liste { id, ordre }. */
  reordonnerImages: (ordres: { id: number; ordre: number }[]) =>
    api.patch('/images/reordonner', ordres),
  /** Vue admin : tous les biens, tous propriétaires confondus. */
  tousAdmin: (params: { nom?: string; statut?: string }) =>
    api
      .get('/immobiliers', {
        params: {
          size: 200,
          sort: 'id,desc',
          ...(params.nom ? { 'nom.contains': params.nom } : {}),
          ...(params.statut ? { 'statut.equals': params.statut } : {}),
        },
      })
      .then((r) => r.data),
  changerStatut: (id: number, statut: string) =>
    api
      .patch(`/immobiliers/${id}`, { id, statut }, { headers: { 'Content-Type': 'application/merge-patch+json' } })
      .then((r) => r.data),
};

export const colocationApi = {
  creerDetail: (d: Record<string, unknown>) => api.post('/detail-colocations', d).then((r) => r.data),
};

export const vueApi = {
  enregistrer: (annonceId: number) => api.post(`/annonces/${annonceId}/vue`).catch(() => undefined),
};

export const uploadApi = {
  image: async (fichier: File) => {
    const form = new FormData();
    form.append('file', fichier);
    const { data } = await api.post<{ url: string }>('/files/images', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data.url;
  },
};

export const compteApi = {
  monCompte: () => api.get<Record<string, unknown>>('/account').then((r) => r.data),
  enregistrer: (compte: Record<string, unknown>) => api.post('/account', compte),
  changerMotDePasse: (currentPassword: string, newPassword: string) =>
    api.post('/account/change-password', { currentPassword, newPassword }),
  /** Désactive le compte de l'utilisateur connecté. */
  desactiverMonCompte: () => api.post('/account/deactivate'),
};

// --- Paiement (frais de visite, séquestre) — module V2, passerelle simulée ---
export interface Paiement {
  id: number;
  reference: string;
  montant: number;
  statut: 'EN_ATTENTE' | 'EN_SEQUESTRE' | 'LIBERE' | 'REMBOURSE';
  moyen?: 'ORANGE_MONEY' | 'MOOV_MONEY' | 'CARTE';
  dateCreation?: string;
  dateSequestre?: string;
  dateDenouement?: string;
  rendezVousId?: number;
  annonceTitre?: string;
  payeurLogin?: string;
}

export const paiementApi = {
  fraisDeVisite: () => api.get<number>('/paiements/frais-de-visite').then((r) => r.data),
  initier: (rendezVousId: number) =>
    api.post<Paiement>(`/paiements/rendez-vous/${rendezVousId}/initier`).then((r) => r.data),
  simuler: (id: number, moyen: string) =>
    api.post<Paiement>(`/paiements/${id}/simuler-reglement`, null, { params: { moyen } }).then((r) => r.data),
  pourRendezVous: (rendezVousId: number) =>
    api.get<Paiement>(`/paiements/rendez-vous/${rendezVousId}`).then((r) => r.data).catch(() => null),
  // Admin
  tous: () => api.get<Paiement[]>('/paiements').then((r) => r.data),
  liberer: (id: number) => api.post<Paiement>(`/paiements/${id}/liberer`).then((r) => r.data),
  rembourser: (id: number) => api.post<Paiement>(`/paiements/${id}/rembourser`).then((r) => r.data),
};

/**
 * Ouvre un fichier protégé (pièce justificative) dans un nouvel onglet.
 *
 * Un simple <a href> ne peut pas fonctionner : le jeton JWT vit dans localStorage et
 * n'est pas envoyé par le navigateur sur une navigation classique. On récupère donc le
 * fichier via le client authentifié, puis on l'ouvre depuis une URL blob temporaire.
 */
export async function ouvrirFichierProtege(url: string): Promise<void> {
  const chemin = url.startsWith('/api/') ? url.slice(4) : url;
  const { data } = await api.get<Blob>(chemin, { responseType: 'blob' });
  const blobUrl = URL.createObjectURL(data);
  const onglet = window.open(blobUrl, '_blank', 'noopener');
  if (!onglet) URL.revokeObjectURL(blobUrl);
  else setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
}

/**
 * Message lisible à partir d'une erreur HTTP.
 *
 * Beaucoup de gestionnaires de clic appelaient l'API sans `try/catch` : en cas de
 * refus du serveur, rien ne s'affichait et le bouton paraissait sans effet. Le cas
 * est devenu fréquent depuis que l'API renvoie 403 lorsqu'une ressource ne vous
 * appartient pas. Cette fonction donne une phrase exploitable pour chaque situation.
 */
export function messageErreur(e: unknown, defaut = "L'opération a échoué."): string {
  const err = e as {
    response?: { status?: number; data?: { detail?: string; title?: string; message?: string } };
  };
  const donnees = err?.response?.data;
  if (donnees?.detail) return donnees.detail;
  if (donnees?.title) return donnees.title;
  switch (err?.response?.status) {
    case 400:
      return 'Demande invalide. Vérifiez les informations saisies.';
    case 401:
      return 'Votre session a expiré. Reconnectez-vous.';
    case 403:
      return "Vous n'êtes pas autorisé à effectuer cette action.";
    case 404:
      return "L'élément est introuvable. Il a peut-être été supprimé.";
    case 409:
      return "L'opération est impossible dans l'état actuel.";
    case undefined:
      return 'Serveur injoignable. Vérifiez votre connexion.';
    default:
      return defaut;
  }
}

/** Avis et réputation des démarcheurs. */
export interface AvisItem {
  id: number;
  note: number;
  commentaire?: string;
  dateCreation: string;
  auteurLogin?: string;
  rendezVousId?: number;
}
export interface Reputation {
  demarcheurId: number;
  moyenne: number | null;
  nombreAvis: number;
  avis: AvisItem[];
}

export const avisApi = {
  /** Réputation publique d'un démarcheur. */
  reputation: (demarcheurId: number) =>
    api.get<Reputation>(`/demarcheurs/${demarcheurId}/reputation`).then((r) => r.data),
  /** Dépose un avis à l'issue d'une visite effectuée. */
  deposer: (rendezVousId: number, note: number, commentaire: string) =>
    api.post<AvisItem>(`/rendez-vous/${rendezVousId}/avis`, { note, commentaire }).then((r) => r.data),
  /** Identifiants des rendez-vous terminés que l'utilisateur peut encore noter. */
  aNoter: () => api.get<number[]>('/mes-visites-a-noter').then((r) => r.data),
};

// ---- Messagerie interne ----
export interface ConversationItem {
  id: number;
  dernierMessageLe?: string;
  annonceId?: number;
  annonceTitre?: string;
  interlocuteurId?: number;
  interlocuteurLogin?: string;
}

export interface MessageItem {
  id: number;
  contenu: string;
  dateEnvoi: string;
  lu: boolean;
  expediteurId: number;
  expediteurLogin: string;
}

export const messagerieApi = {
  conversations: () => api.get<ConversationItem[]>('/conversations').then((r) => r.data),
  ouvrirPourAnnonce: (annonceId: number) =>
    api.post<ConversationItem>(`/conversations/pour-annonce/${annonceId}`).then((r) => r.data),
  messages: (conversationId: number) =>
    api.get<MessageItem[]>(`/conversations/${conversationId}/messages`).then((r) => r.data),
  envoyer: (conversationId: number, contenu: string) =>
    api.post<MessageItem>(`/conversations/${conversationId}/messages`, { contenu }).then((r) => r.data),
  nonLus: () => api.get<{ nombre: number }>('/messages/non-lus').then((r) => r.data.nombre),
};
