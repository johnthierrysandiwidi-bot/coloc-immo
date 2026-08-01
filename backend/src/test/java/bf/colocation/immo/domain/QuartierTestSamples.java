package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class QuartierTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Quartier getQuartierSample1() {
        return new Quartier().id(1L).nom("nom1").description("description1");
    }

    public static Quartier getQuartierSample2() {
        return new Quartier().id(2L).nom("nom2").description("description2");
    }

    public static Quartier getQuartierRandomSampleGenerator() {
        return new Quartier().id(longCount.incrementAndGet()).nom(UUID.randomUUID().toString()).description(UUID.randomUUID().toString());
    }
}
