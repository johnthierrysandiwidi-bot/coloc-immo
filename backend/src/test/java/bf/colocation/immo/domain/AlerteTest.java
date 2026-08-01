package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AlerteNotifieeTestSamples.*;
import static bf.colocation.immo.domain.AlerteTestSamples.*;
import static bf.colocation.immo.domain.LocaliteTestSamples.*;
import static bf.colocation.immo.domain.QuartierTestSamples.*;
import static bf.colocation.immo.domain.TypeImmobilierTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AlerteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Alerte.class);
        Alerte alerte1 = getAlerteSample1();
        Alerte alerte2 = new Alerte();
        assertThat(alerte1).isNotEqualTo(alerte2);

        alerte2.setId(alerte1.getId());
        assertThat(alerte1).isEqualTo(alerte2);

        alerte2 = getAlerteSample2();
        assertThat(alerte1).isNotEqualTo(alerte2);
    }

    @Test
    void localiteTest() {
        Alerte alerte = getAlerteRandomSampleGenerator();
        Localite localiteBack = getLocaliteRandomSampleGenerator();

        alerte.setLocalite(localiteBack);
        assertThat(alerte.getLocalite()).isEqualTo(localiteBack);

        alerte.localite(null);
        assertThat(alerte.getLocalite()).isNull();
    }

    @Test
    void quartierTest() {
        Alerte alerte = getAlerteRandomSampleGenerator();
        Quartier quartierBack = getQuartierRandomSampleGenerator();

        alerte.setQuartier(quartierBack);
        assertThat(alerte.getQuartier()).isEqualTo(quartierBack);

        alerte.quartier(null);
        assertThat(alerte.getQuartier()).isNull();
    }

    @Test
    void typeImmobilierTest() {
        Alerte alerte = getAlerteRandomSampleGenerator();
        TypeImmobilier typeImmobilierBack = getTypeImmobilierRandomSampleGenerator();

        alerte.setTypeImmobilier(typeImmobilierBack);
        assertThat(alerte.getTypeImmobilier()).isEqualTo(typeImmobilierBack);

        alerte.typeImmobilier(null);
        assertThat(alerte.getTypeImmobilier()).isNull();
    }

    @Test
    void notifieesTest() {
        Alerte alerte = getAlerteRandomSampleGenerator();
        AlerteNotifiee alerteNotifieeBack = getAlerteNotifieeRandomSampleGenerator();

        alerte.addNotifiees(alerteNotifieeBack);
        assertThat(alerte.getNotifieeses()).containsOnly(alerteNotifieeBack);
        assertThat(alerteNotifieeBack.getAlerte()).isEqualTo(alerte);

        alerte.removeNotifiees(alerteNotifieeBack);
        assertThat(alerte.getNotifieeses()).doesNotContain(alerteNotifieeBack);
        assertThat(alerteNotifieeBack.getAlerte()).isNull();

        alerte.notifieeses(new HashSet<>(Set.of(alerteNotifieeBack)));
        assertThat(alerte.getNotifieeses()).containsOnly(alerteNotifieeBack);
        assertThat(alerteNotifieeBack.getAlerte()).isEqualTo(alerte);

        alerte.setNotifieeses(new HashSet<>());
        assertThat(alerte.getNotifieeses()).doesNotContain(alerteNotifieeBack);
        assertThat(alerteNotifieeBack.getAlerte()).isNull();
    }
}
