package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AnnonceTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Annonce getAnnonceSample1() {
        return new Annonce().id(1L).titre("titre1").contenu("contenu1").nombreVues(1);
    }

    public static Annonce getAnnonceSample2() {
        return new Annonce().id(2L).titre("titre2").contenu("contenu2").nombreVues(2);
    }

    public static Annonce getAnnonceRandomSampleGenerator() {
        return new Annonce()
            .id(longCount.incrementAndGet())
            .titre(UUID.randomUUID().toString())
            .contenu(UUID.randomUUID().toString())
            .nombreVues(intCount.incrementAndGet());
    }
}
