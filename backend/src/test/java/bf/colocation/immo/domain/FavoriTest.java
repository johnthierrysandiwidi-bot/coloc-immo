package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AnnonceTestSamples.*;
import static bf.colocation.immo.domain.FavoriTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FavoriTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Favori.class);
        Favori favori1 = getFavoriSample1();
        Favori favori2 = new Favori();
        assertThat(favori1).isNotEqualTo(favori2);

        favori2.setId(favori1.getId());
        assertThat(favori1).isEqualTo(favori2);

        favori2 = getFavoriSample2();
        assertThat(favori1).isNotEqualTo(favori2);
    }

    @Test
    void annonceTest() {
        Favori favori = getFavoriRandomSampleGenerator();
        Annonce annonceBack = getAnnonceRandomSampleGenerator();

        favori.setAnnonce(annonceBack);
        assertThat(favori.getAnnonce()).isEqualTo(annonceBack);

        favori.annonce(null);
        assertThat(favori.getAnnonce()).isNull();
    }
}
