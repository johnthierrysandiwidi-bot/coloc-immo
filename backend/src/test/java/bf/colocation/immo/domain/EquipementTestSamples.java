package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EquipementTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Equipement getEquipementSample1() {
        return new Equipement().id(1L).nom("nom1").icone("icone1");
    }

    public static Equipement getEquipementSample2() {
        return new Equipement().id(2L).nom("nom2").icone("icone2");
    }

    public static Equipement getEquipementRandomSampleGenerator() {
        return new Equipement().id(longCount.incrementAndGet()).nom(UUID.randomUUID().toString()).icone(UUID.randomUUID().toString());
    }
}
