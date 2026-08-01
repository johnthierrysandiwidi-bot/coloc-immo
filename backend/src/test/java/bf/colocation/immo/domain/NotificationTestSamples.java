package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class NotificationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Notification getNotificationSample1() {
        return new Notification().id(1L).titre("titre1").message("message1").lien("lien1");
    }

    public static Notification getNotificationSample2() {
        return new Notification().id(2L).titre("titre2").message("message2").lien("lien2");
    }

    public static Notification getNotificationRandomSampleGenerator() {
        return new Notification()
            .id(longCount.incrementAndGet())
            .titre(UUID.randomUUID().toString())
            .message(UUID.randomUUID().toString())
            .lien(UUID.randomUUID().toString());
    }
}
