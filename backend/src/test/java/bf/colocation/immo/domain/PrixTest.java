package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.ImmobilierTestSamples.*;
import static bf.colocation.immo.domain.PrixTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrixTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prix.class);
        Prix prix1 = getPrixSample1();
        Prix prix2 = new Prix();
        assertThat(prix1).isNotEqualTo(prix2);

        prix2.setId(prix1.getId());
        assertThat(prix1).isEqualTo(prix2);

        prix2 = getPrixSample2();
        assertThat(prix1).isNotEqualTo(prix2);
    }

    @Test
    void immobilierTest() {
        Prix prix = getPrixRandomSampleGenerator();
        Immobilier immobilierBack = getImmobilierRandomSampleGenerator();

        prix.setImmobilier(immobilierBack);
        assertThat(prix.getImmobilier()).isEqualTo(immobilierBack);

        prix.immobilier(null);
        assertThat(prix.getImmobilier()).isNull();
    }
}
