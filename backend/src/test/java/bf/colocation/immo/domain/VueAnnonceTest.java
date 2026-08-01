package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AnnonceTestSamples.*;
import static bf.colocation.immo.domain.VueAnnonceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VueAnnonceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VueAnnonce.class);
        VueAnnonce vueAnnonce1 = getVueAnnonceSample1();
        VueAnnonce vueAnnonce2 = new VueAnnonce();
        assertThat(vueAnnonce1).isNotEqualTo(vueAnnonce2);

        vueAnnonce2.setId(vueAnnonce1.getId());
        assertThat(vueAnnonce1).isEqualTo(vueAnnonce2);

        vueAnnonce2 = getVueAnnonceSample2();
        assertThat(vueAnnonce1).isNotEqualTo(vueAnnonce2);
    }

    @Test
    void annonceTest() {
        VueAnnonce vueAnnonce = getVueAnnonceRandomSampleGenerator();
        Annonce annonceBack = getAnnonceRandomSampleGenerator();

        vueAnnonce.setAnnonce(annonceBack);
        assertThat(vueAnnonce.getAnnonce()).isEqualTo(annonceBack);

        vueAnnonce.annonce(null);
        assertThat(vueAnnonce.getAnnonce()).isNull();
    }
}
