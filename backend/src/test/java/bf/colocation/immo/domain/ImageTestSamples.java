package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ImageTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Image getImageSample1() {
        return new Image().id(1L).nom("nom1").url("url1").ordre(1);
    }

    public static Image getImageSample2() {
        return new Image().id(2L).nom("nom2").url("url2").ordre(2);
    }

    public static Image getImageRandomSampleGenerator() {
        return new Image()
            .id(longCount.incrementAndGet())
            .nom(UUID.randomUUID().toString())
            .url(UUID.randomUUID().toString())
            .ordre(intCount.incrementAndGet());
    }
}
