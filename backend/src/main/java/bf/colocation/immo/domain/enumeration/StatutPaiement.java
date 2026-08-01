package bf.colocation.immo.domain.enumeration;

/**
 * Cycle de vie d'un paiement de frais de visite (séquestre).
 */
public enum StatutPaiement {
    /** Créé, en attente de règlement par le payeur. */
    EN_ATTENTE,
    /** Réglé et conservé en séquestre jusqu'à la visite. */
    EN_SEQUESTRE,
    /** Visite honorée : fonds libérés au démarcheur. */
    LIBERE,
    /** Visite annulée par le démarcheur : fonds remboursés au payeur. */
    REMBOURSE,
}
