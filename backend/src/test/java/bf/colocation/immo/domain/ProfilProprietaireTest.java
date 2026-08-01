package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.ProfilProprietaireTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfilProprietaireTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProfilProprietaire.class);
        ProfilProprietaire profilProprietaire1 = getProfilProprietaireSample1();
        ProfilProprietaire profilProprietaire2 = new ProfilProprietaire();
        assertThat(profilProprietaire1).isNotEqualTo(profilProprietaire2);

        profilProprietaire2.setId(profilProprietaire1.getId());
        assertThat(profilProprietaire1).isEqualTo(profilProprietaire2);

        profilProprietaire2 = getProfilProprietaireSample2();
        assertThat(profilProprietaire1).isNotEqualTo(profilProprietaire2);
    }
}
