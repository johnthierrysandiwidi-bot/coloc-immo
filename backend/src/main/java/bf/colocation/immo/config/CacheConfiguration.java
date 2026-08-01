package bf.colocation.immo.config;

import java.time.Duration;
import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        var ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Object.class,
                Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries())
            )
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build()
        );
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, bf.colocation.immo.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, bf.colocation.immo.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, bf.colocation.immo.domain.User.class.getName());
            createCache(cm, bf.colocation.immo.domain.Authority.class.getName());
            createCache(cm, bf.colocation.immo.domain.User.class.getName() + ".authorities");
            createCache(cm, bf.colocation.immo.domain.ProfilProprietaire.class.getName());
            createCache(cm, bf.colocation.immo.domain.ProfilDemarcheur.class.getName());
            createCache(cm, bf.colocation.immo.domain.Localite.class.getName());
            createCache(cm, bf.colocation.immo.domain.Localite.class.getName() + ".quartierses");
            createCache(cm, bf.colocation.immo.domain.Quartier.class.getName());
            createCache(cm, bf.colocation.immo.domain.TypeImmobilier.class.getName());
            createCache(cm, bf.colocation.immo.domain.Immobilier.class.getName());
            createCache(cm, bf.colocation.immo.domain.Immobilier.class.getName() + ".prixes");
            createCache(cm, bf.colocation.immo.domain.Immobilier.class.getName() + ".imageses");
            createCache(cm, bf.colocation.immo.domain.Immobilier.class.getName() + ".annonceses");
            createCache(cm, bf.colocation.immo.domain.Prix.class.getName());
            createCache(cm, bf.colocation.immo.domain.Image.class.getName());
            createCache(cm, bf.colocation.immo.domain.Annonce.class.getName());
            createCache(cm, bf.colocation.immo.domain.Annonce.class.getName() + ".vueses");
            createCache(cm, bf.colocation.immo.domain.Annonce.class.getName() + ".rendezVouses");
            createCache(cm, bf.colocation.immo.domain.Annonce.class.getName() + ".favorises");
            createCache(cm, bf.colocation.immo.domain.VueAnnonce.class.getName());
            createCache(cm, bf.colocation.immo.domain.DetailColocation.class.getName());
            createCache(cm, bf.colocation.immo.domain.DetailColocation.class.getName() + ".equipementses");
            createCache(cm, bf.colocation.immo.domain.Equipement.class.getName());
            createCache(cm, bf.colocation.immo.domain.Equipement.class.getName() + ".colocationses");
            createCache(cm, bf.colocation.immo.domain.TypeDocument.class.getName());
            createCache(cm, bf.colocation.immo.domain.Document.class.getName());
            createCache(cm, bf.colocation.immo.domain.RendezVous.class.getName());
            createCache(cm, bf.colocation.immo.domain.Favori.class.getName());
            createCache(cm, bf.colocation.immo.domain.Alerte.class.getName());
            createCache(cm, bf.colocation.immo.domain.Alerte.class.getName() + ".notifieeses");
            createCache(cm, bf.colocation.immo.domain.AlerteNotifiee.class.getName());
            createCache(cm, bf.colocation.immo.domain.Notification.class.getName());
            createCache(cm, bf.colocation.immo.domain.DeviceToken.class.getName());
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
