package bf.colocation.immo.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class FavoriTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Favori getFavoriSample1() {
        return new Favori().id(1L);
    }

    public static Favori getFavoriSample2() {
        return new Favori().id(2L);
    }

    public static Favori getFavoriRandomSampleGenerator() {
        return new Favori().id(longCount.incrementAndGet());
    }
}
