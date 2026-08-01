package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class LocaliteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Localite getLocaliteSample1() {
        return new Localite().id(1L).nom("nom1").description("description1");
    }

    public static Localite getLocaliteSample2() {
        return new Localite().id(2L).nom("nom2").description("description2");
    }

    public static Localite getLocaliteRandomSampleGenerator() {
        return new Localite().id(longCount.incrementAndGet()).nom(UUID.randomUUID().toString()).description(UUID.randomUUID().toString());
    }
}
