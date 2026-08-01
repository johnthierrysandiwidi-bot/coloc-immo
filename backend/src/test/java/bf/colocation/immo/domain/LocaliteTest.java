package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.LocaliteTestSamples.*;
import static bf.colocation.immo.domain.QuartierTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LocaliteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Localite.class);
        Localite localite1 = getLocaliteSample1();
        Localite localite2 = new Localite();
        assertThat(localite1).isNotEqualTo(localite2);

        localite2.setId(localite1.getId());
        assertThat(localite1).isEqualTo(localite2);

        localite2 = getLocaliteSample2();
        assertThat(localite1).isNotEqualTo(localite2);
    }

    @Test
    void quartiersTest() {
        Localite localite = getLocaliteRandomSampleGenerator();
        Quartier quartierBack = getQuartierRandomSampleGenerator();

        localite.addQuartiers(quartierBack);
        assertThat(localite.getQuartierses()).containsOnly(quartierBack);
        assertThat(quartierBack.getLocalite()).isEqualTo(localite);

        localite.removeQuartiers(quartierBack);
        assertThat(localite.getQuartierses()).doesNotContain(quartierBack);
        assertThat(quartierBack.getLocalite()).isNull();

        localite.quartierses(new HashSet<>(Set.of(quartierBack)));
        assertThat(localite.getQuartierses()).containsOnly(quartierBack);
        assertThat(quartierBack.getLocalite()).isEqualTo(localite);

        localite.setQuartierses(new HashSet<>());
        assertThat(localite.getQuartierses()).doesNotContain(quartierBack);
        assertThat(quartierBack.getLocalite()).isNull();
    }
}
