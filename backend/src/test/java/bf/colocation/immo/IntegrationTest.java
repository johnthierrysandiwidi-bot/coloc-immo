package bf.colocation.immo;

import bf.colocation.immo.config.AsyncSyncConfiguration;
import bf.colocation.immo.config.DatabaseTestcontainer;
import bf.colocation.immo.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        ColocationImmoApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        bf.colocation.immo.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}
