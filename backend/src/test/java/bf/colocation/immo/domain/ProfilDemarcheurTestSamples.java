package bf.colocation.immo.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ProfilDemarcheurTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static ProfilDemarcheur getProfilDemarcheurSample1() {
        return new ProfilDemarcheur().id(1L);
    }

    public static ProfilDemarcheur getProfilDemarcheurSample2() {
        return new ProfilDemarcheur().id(2L);
    }

    public static ProfilDemarcheur getProfilDemarcheurRandomSampleGenerator() {
        return new ProfilDemarcheur().id(longCount.incrementAndGet());
    }
}
