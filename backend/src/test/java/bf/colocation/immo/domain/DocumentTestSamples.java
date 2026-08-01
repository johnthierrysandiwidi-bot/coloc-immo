package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DocumentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Document getDocumentSample1() {
        return new Document().id(1L).nom("nom1").url("url1").motifRefus("motifRefus1");
    }

    public static Document getDocumentSample2() {
        return new Document().id(2L).nom("nom2").url("url2").motifRefus("motifRefus2");
    }

    public static Document getDocumentRandomSampleGenerator() {
        return new Document()
            .id(longCount.incrementAndGet())
            .nom(UUID.randomUUID().toString())
            .url(UUID.randomUUID().toString())
            .motifRefus(UUID.randomUUID().toString());
    }
}
