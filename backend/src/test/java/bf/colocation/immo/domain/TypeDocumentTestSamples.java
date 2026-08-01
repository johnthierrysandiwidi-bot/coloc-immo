package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TypeDocumentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static TypeDocument getTypeDocumentSample1() {
        return new TypeDocument().id(1L).nom("nom1").description("description1");
    }

    public static TypeDocument getTypeDocumentSample2() {
        return new TypeDocument().id(2L).nom("nom2").description("description2");
    }

    public static TypeDocument getTypeDocumentRandomSampleGenerator() {
        return new TypeDocument()
            .id(longCount.incrementAndGet())
            .nom(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
