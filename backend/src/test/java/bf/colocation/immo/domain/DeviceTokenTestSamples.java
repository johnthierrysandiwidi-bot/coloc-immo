package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DeviceTokenTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static DeviceToken getDeviceTokenSample1() {
        return new DeviceToken().id(1L).token("token1").plateforme("plateforme1");
    }

    public static DeviceToken getDeviceTokenSample2() {
        return new DeviceToken().id(2L).token("token2").plateforme("plateforme2");
    }

    public static DeviceToken getDeviceTokenRandomSampleGenerator() {
        return new DeviceToken()
            .id(longCount.incrementAndGet())
            .token(UUID.randomUUID().toString())
            .plateforme(UUID.randomUUID().toString());
    }
}
