package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.LocaliteTestSamples.*;
import static bf.colocation.immo.domain.QuartierTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class QuartierTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Quartier.class);
        Quartier quartier1 = getQuartierSample1();
        Quartier quartier2 = new Quartier();
        assertThat(quartier1).isNotEqualTo(quartier2);

        quartier2.setId(quartier1.getId());
        assertThat(quartier1).isEqualTo(quartier2);

        quartier2 = getQuartierSample2();
        assertThat(quartier1).isNotEqualTo(quartier2);
    }

    @Test
    void localiteTest() {
        Quartier quartier = getQuartierRandomSampleGenerator();
        Localite localiteBack = getLocaliteRandomSampleGenerator();

        quartier.setLocalite(localiteBack);
        assertThat(quartier.getLocalite()).isEqualTo(localiteBack);

        quartier.localite(null);
        assertThat(quartier.getLocalite()).isNull();
    }
}
