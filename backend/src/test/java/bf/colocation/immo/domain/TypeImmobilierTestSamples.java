package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TypeImmobilierTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static TypeImmobilier getTypeImmobilierSample1() {
        return new TypeImmobilier().id(1L).nom("nom1").description("description1");
    }

    public static TypeImmobilier getTypeImmobilierSample2() {
        return new TypeImmobilier().id(2L).nom("nom2").description("description2");
    }

    public static TypeImmobilier getTypeImmobilierRandomSampleGenerator() {
        return new TypeImmobilier()
            .id(longCount.incrementAndGet())
            .nom(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
