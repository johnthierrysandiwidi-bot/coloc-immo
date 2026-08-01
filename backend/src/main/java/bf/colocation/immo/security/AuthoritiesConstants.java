package bf.colocation.immo.security;

/**
 * Constantes des rôles Spring Security de la plateforme.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String PROPRIETAIRE = "ROLE_PROPRIETAIRE";

    public static final String DEMARCHEUR = "ROLE_DEMARCHEUR";

    public static final String UTILISATEUR = "ROLE_UTILISATEUR";

    /** Conservé : JHipster s'en sert en interne (création de compte, /api/account). */
    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    private AuthoritiesConstants() {}
}
