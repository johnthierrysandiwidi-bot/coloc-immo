package bf.colocation.immo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés spécifiques à Colocation Immo.
 *
 * ATTENTION : ignoreUnknownFields = false. Toute propriété ajoutée sous "application:"
 * dans application.yml doit être déclarée ici, sinon l'application refuse de démarrer.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Liquibase liquibase = new Liquibase();
    private final Security security = new Security();
    private final Storage storage = new Storage();

    public Liquibase getLiquibase() {
        return liquibase;
    }

    public Security getSecurity() {
        return security;
    }

    public Storage getStorage() {
        return storage;
    }

    /** Bloc JHipster — consommé par LiquibaseConfiguration. Ne pas retirer. */
    public static class Liquibase {

        private Boolean asyncStart = true;

        public Boolean getAsyncStart() {
            return asyncStart;
        }

        public void setAsyncStart(Boolean asyncStart) {
            this.asyncStart = asyncStart;
        }
    }

    public static class Security {

        private long refreshTokenValidityInDays = 7;

        /** Active les comptes dès l'inscription : sans SMTP, un compte inactif ne peut jamais se connecter. */
        private boolean autoActivateAccounts = true;

        public long getRefreshTokenValidityInDays() {
            return refreshTokenValidityInDays;
        }

        public void setRefreshTokenValidityInDays(long refreshTokenValidityInDays) {
            this.refreshTokenValidityInDays = refreshTokenValidityInDays;
        }

        public boolean isAutoActivateAccounts() {
            return autoActivateAccounts;
        }

        public void setAutoActivateAccounts(boolean autoActivateAccounts) {
            this.autoActivateAccounts = autoActivateAccounts;
        }
    }

    public static class Storage {

        private String location = "./uploads";

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
