package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PrixTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Prix getPrixSample1() {
        return new Prix().id(1L).description("description1");
    }

    public static Prix getPrixSample2() {
        return new Prix().id(2L).description("description2");
    }

    public static Prix getPrixRandomSampleGenerator() {
        return new Prix().id(longCount.incrementAndGet()).description(UUID.randomUUID().toString());
    }
}
