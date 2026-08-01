package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class VueAnnonceTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static VueAnnonce getVueAnnonceSample1() {
        return new VueAnnonce().id(1L).adresseIp("adresseIp1");
    }

    public static VueAnnonce getVueAnnonceSample2() {
        return new VueAnnonce().id(2L).adresseIp("adresseIp2");
    }

    public static VueAnnonce getVueAnnonceRandomSampleGenerator() {
        return new VueAnnonce().id(longCount.incrementAndGet()).adresseIp(UUID.randomUUID().toString());
    }
}
