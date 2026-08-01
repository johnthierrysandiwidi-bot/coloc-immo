package bf.colocation.immo.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ProfilProprietaireTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static ProfilProprietaire getProfilProprietaireSample1() {
        return new ProfilProprietaire().id(1L).raisonSociale("raisonSociale1");
    }

    public static ProfilProprietaire getProfilProprietaireSample2() {
        return new ProfilProprietaire().id(2L).raisonSociale("raisonSociale2");
    }

    public static ProfilProprietaire getProfilProprietaireRandomSampleGenerator() {
        return new ProfilProprietaire().id(longCount.incrementAndGet()).raisonSociale(UUID.randomUUID().toString());
    }
}
