package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DetailColocationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static DetailColocation getDetailColocationSample1() {
        return new DetailColocation().id(1L).nombrePlaces(1).placesRestantes(1).ageMin(1).ageMax(1).reglesDeVie("reglesDeVie1");
    }

    public static DetailColocation getDetailColocationSample2() {
        return new DetailColocation().id(2L).nombrePlaces(2).placesRestantes(2).ageMin(2).ageMax(2).reglesDeVie("reglesDeVie2");
    }

    public static DetailColocation getDetailColocationRandomSampleGenerator() {
        return new DetailColocation()
            .id(longCount.incrementAndGet())
            .nombrePlaces(intCount.incrementAndGet())
            .placesRestantes(intCount.incrementAndGet())
            .ageMin(intCount.incrementAndGet())
            .ageMax(intCount.incrementAndGet())
            .reglesDeVie(UUID.randomUUID().toString());
    }
}
