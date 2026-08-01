package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AnnonceTestSamples.*;
import static bf.colocation.immo.domain.DetailColocationTestSamples.*;
import static bf.colocation.immo.domain.FavoriTestSamples.*;
import static bf.colocation.immo.domain.ImmobilierTestSamples.*;
import static bf.colocation.immo.domain.RendezVousTestSamples.*;
import static bf.colocation.immo.domain.VueAnnonceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AnnonceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Annonce.class);
        Annonce annonce1 = getAnnonceSample1();
        Annonce annonce2 = new Annonce();
        assertThat(annonce1).isNotEqualTo(annonce2);

        annonce2.setId(annonce1.getId());
        assertThat(annonce1).isEqualTo(annonce2);

        annonce2 = getAnnonceSample2();
        assertThat(annonce1).isNotEqualTo(annonce2);
    }

    @Test
    void immobilierTest() {
        Annonce annonce = getAnnonceRandomSampleGenerator();
        Immobilier immobilierBack = getImmobilierRandomSampleGenerator();

        annonce.setImmobilier(immobilierBack);
        assertThat(annonce.getImmobilier()).isEqualTo(immobilierBack);

        annonce.immobilier(null);
        assertThat(annonce.getImmobilier()).isNull();
    }

    @Test
    void detailColocationTest() {
        Annonce annonce = getAnnonceRandomSampleGenerator();
        DetailColocation detailColocationBack = getDetailColocationRandomSampleGenerator();

        annonce.setDetailColocation(detailColocationBack);
        assertThat(annonce.getDetailColocation()).isEqualTo(detailColocationBack);
        assertThat(detailColocationBack.getAnnonce()).isEqualTo(annonce);

        annonce.detailColocation(null);
        assertThat(annonce.getDetailColocation()).isNull();
        assertThat(detailColocationBack.getAnnonce()).isNull();
    }

    @Test
    void vuesTest() {
        Annonce annonce = getAnnonceRandomSampleGenerator();
        VueAnnonce vueAnnonceBack = getVueAnnonceRandomSampleGenerator();

        annonce.addVues(vueAnnonceBack);
        assertThat(annonce.getVueses()).containsOnly(vueAnnonceBack);
        assertThat(vueAnnonceBack.getAnnonce()).isEqualTo(annonce);

        annonce.removeVues(vueAnnonceBack);
        assertThat(annonce.getVueses()).doesNotContain(vueAnnonceBack);
        assertThat(vueAnnonceBack.getAnnonce()).isNull();

        annonce.vueses(new HashSet<>(Set.of(vueAnnonceBack)));
        assertThat(annonce.getVueses()).containsOnly(vueAnnonceBack);
        assertThat(vueAnnonceBack.getAnnonce()).isEqualTo(annonce);

        annonce.setVueses(new HashSet<>());
        assertThat(annonce.getVueses()).doesNotContain(vueAnnonceBack);
        assertThat(vueAnnonceBack.getAnnonce()).isNull();
    }

    @Test
    void rendezVousTest() {
        Annonce annonce = getAnnonceRandomSampleGenerator();
        RendezVous rendezVousBack = getRendezVousRandomSampleGenerator();

        annonce.addRendezVous(rendezVousBack);
        assertThat(annonce.getRendezVouses()).containsOnly(rendezVousBack);
        assertThat(rendezVousBack.getAnnonce()).isEqualTo(annonce);

        annonce.removeRendezVous(rendezVousBack);
        assertThat(annonce.getRendezVouses()).doesNotContain(rendezVousBack);
        assertThat(rendezVousBack.getAnnonce()).isNull();

        annonce.rendezVouses(new HashSet<>(Set.of(rendezVousBack)));
        assertThat(annonce.getRendezVouses()).containsOnly(rendezVousBack);
        assertThat(rendezVousBack.getAnnonce()).isEqualTo(annonce);

        annonce.setRendezVouses(new HashSet<>());
        assertThat(annonce.getRendezVouses()).doesNotContain(rendezVousBack);
        assertThat(rendezVousBack.getAnnonce()).isNull();
    }

    @Test
    void favorisTest() {
        Annonce annonce = getAnnonceRandomSampleGenerator();
        Favori favoriBack = getFavoriRandomSampleGenerator();

        annonce.addFavoris(favoriBack);
        assertThat(annonce.getFavorises()).containsOnly(favoriBack);
        assertThat(favoriBack.getAnnonce()).isEqualTo(annonce);

        annonce.removeFavoris(favoriBack);
        assertThat(annonce.getFavorises()).doesNotContain(favoriBack);
        assertThat(favoriBack.getAnnonce()).isNull();

        annonce.favorises(new HashSet<>(Set.of(favoriBack)));
        assertThat(annonce.getFavorises()).containsOnly(favoriBack);
        assertThat(favoriBack.getAnnonce()).isEqualTo(annonce);

        annonce.setFavorises(new HashSet<>());
        assertThat(annonce.getFavorises()).doesNotContain(favoriBack);
        assertThat(favoriBack.getAnnonce()).isNull();
    }
}
