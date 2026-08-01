package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.ProfilDemarcheurTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfilDemarcheurTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProfilDemarcheur.class);
        ProfilDemarcheur profilDemarcheur1 = getProfilDemarcheurSample1();
        ProfilDemarcheur profilDemarcheur2 = new ProfilDemarcheur();
        assertThat(profilDemarcheur1).isNotEqualTo(profilDemarcheur2);

        profilDemarcheur2.setId(profilDemarcheur1.getId());
        assertThat(profilDemarcheur1).isEqualTo(profilDemarcheur2);

        profilDemarcheur2 = getProfilDemarcheurSample2();
        assertThat(profilDemarcheur1).isNotEqualTo(profilDemarcheur2);
    }
}
