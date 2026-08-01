package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ImmobilierTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Immobilier getImmobilierSample1() {
        return new Immobilier()
            .id(1L)
            .nom("nom1")
            .description("description1")
            .adresse("adresse1")
            .nombrePieces(1)
            .nombreChambres(1)
            .nombreSallesBain(1)
            .nombreSalons(1);
    }

    public static Immobilier getImmobilierSample2() {
        return new Immobilier()
            .id(2L)
            .nom("nom2")
            .description("description2")
            .adresse("adresse2")
            .nombrePieces(2)
            .nombreChambres(2)
            .nombreSallesBain(2)
            .nombreSalons(2);
    }

    public static Immobilier getImmobilierRandomSampleGenerator() {
        return new Immobilier()
            .id(longCount.incrementAndGet())
            .nom(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .adresse(UUID.randomUUID().toString())
            .nombrePieces(intCount.incrementAndGet())
            .nombreChambres(intCount.incrementAndGet())
            .nombreSallesBain(intCount.incrementAndGet())
            .nombreSalons(intCount.incrementAndGet());
    }
}
