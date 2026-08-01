package bf.colocation.immo.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class AlerteNotifieeTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static AlerteNotifiee getAlerteNotifieeSample1() {
        return new AlerteNotifiee().id(1L);
    }

    public static AlerteNotifiee getAlerteNotifieeSample2() {
        return new AlerteNotifiee().id(2L);
    }

    public static AlerteNotifiee getAlerteNotifieeRandomSampleGenerator() {
        return new AlerteNotifiee().id(longCount.incrementAndGet());
    }
}
