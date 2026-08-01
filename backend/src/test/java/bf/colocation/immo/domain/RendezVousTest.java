package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AnnonceTestSamples.*;
import static bf.colocation.immo.domain.RendezVousTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RendezVousTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RendezVous.class);
        RendezVous rendezVous1 = getRendezVousSample1();
        RendezVous rendezVous2 = new RendezVous();
        assertThat(rendezVous1).isNotEqualTo(rendezVous2);

        rendezVous2.setId(rendezVous1.getId());
        assertThat(rendezVous1).isEqualTo(rendezVous2);

        rendezVous2 = getRendezVousSample2();
        assertThat(rendezVous1).isNotEqualTo(rendezVous2);
    }

    @Test
    void annonceTest() {
        RendezVous rendezVous = getRendezVousRandomSampleGenerator();
        Annonce annonceBack = getAnnonceRandomSampleGenerator();

        rendezVous.setAnnonce(annonceBack);
        assertThat(rendezVous.getAnnonce()).isEqualTo(annonceBack);

        rendezVous.annonce(null);
        assertThat(rendezVous.getAnnonce()).isNull();
    }
}
