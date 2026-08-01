package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AlerteNotifieeTestSamples.*;
import static bf.colocation.immo.domain.AlerteTestSamples.*;
import static bf.colocation.immo.domain.AnnonceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlerteNotifieeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlerteNotifiee.class);
        AlerteNotifiee alerteNotifiee1 = getAlerteNotifieeSample1();
        AlerteNotifiee alerteNotifiee2 = new AlerteNotifiee();
        assertThat(alerteNotifiee1).isNotEqualTo(alerteNotifiee2);

        alerteNotifiee2.setId(alerteNotifiee1.getId());
        assertThat(alerteNotifiee1).isEqualTo(alerteNotifiee2);

        alerteNotifiee2 = getAlerteNotifieeSample2();
        assertThat(alerteNotifiee1).isNotEqualTo(alerteNotifiee2);
    }

    @Test
    void alerteTest() {
        AlerteNotifiee alerteNotifiee = getAlerteNotifieeRandomSampleGenerator();
        Alerte alerteBack = getAlerteRandomSampleGenerator();

        alerteNotifiee.setAlerte(alerteBack);
        assertThat(alerteNotifiee.getAlerte()).isEqualTo(alerteBack);

        alerteNotifiee.alerte(null);
        assertThat(alerteNotifiee.getAlerte()).isNull();
    }

    @Test
    void annonceTest() {
        AlerteNotifiee alerteNotifiee = getAlerteNotifieeRandomSampleGenerator();
        Annonce annonceBack = getAnnonceRandomSampleGenerator();

        alerteNotifiee.setAnnonce(annonceBack);
        assertThat(alerteNotifiee.getAnnonce()).isEqualTo(annonceBack);

        alerteNotifiee.annonce(null);
        assertThat(alerteNotifiee.getAnnonce()).isNull();
    }
}
