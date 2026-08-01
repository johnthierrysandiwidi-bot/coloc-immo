package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AnnonceTestSamples.*;
import static bf.colocation.immo.domain.ImageTestSamples.*;
import static bf.colocation.immo.domain.ImmobilierTestSamples.*;
import static bf.colocation.immo.domain.LocaliteTestSamples.*;
import static bf.colocation.immo.domain.PrixTestSamples.*;
import static bf.colocation.immo.domain.QuartierTestSamples.*;
import static bf.colocation.immo.domain.TypeImmobilierTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ImmobilierTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Immobilier.class);
        Immobilier immobilier1 = getImmobilierSample1();
        Immobilier immobilier2 = new Immobilier();
        assertThat(immobilier1).isNotEqualTo(immobilier2);

        immobilier2.setId(immobilier1.getId());
        assertThat(immobilier1).isEqualTo(immobilier2);

        immobilier2 = getImmobilierSample2();
        assertThat(immobilier1).isNotEqualTo(immobilier2);
    }

    @Test
    void prixTest() {
        Immobilier immobilier = getImmobilierRandomSampleGenerator();
        Prix prixBack = getPrixRandomSampleGenerator();

        immobilier.addPrix(prixBack);
        assertThat(immobilier.getPrixes()).containsOnly(prixBack);
        assertThat(prixBack.getImmobilier()).isEqualTo(immobilier);

        immobilier.removePrix(prixBack);
        assertThat(immobilier.getPrixes()).doesNotContain(prixBack);
        assertThat(prixBack.getImmobilier()).isNull();

        immobilier.prixes(new HashSet<>(Set.of(prixBack)));
        assertThat(immobilier.getPrixes()).containsOnly(prixBack);
        assertThat(prixBack.getImmobilier()).isEqualTo(immobilier);

        immobilier.setPrixes(new HashSet<>());
        assertThat(immobilier.getPrixes()).doesNotContain(prixBack);
        assertThat(prixBack.getImmobilier()).isNull();
    }

    @Test
    void imagesTest() {
        Immobilier immobilier = getImmobilierRandomSampleGenerator();
        Image imageBack = getImageRandomSampleGenerator();

        immobilier.addImages(imageBack);
        assertThat(immobilier.getImageses()).containsOnly(imageBack);
        assertThat(imageBack.getImmobilier()).isEqualTo(immobilier);

        immobilier.removeImages(imageBack);
        assertThat(immobilier.getImageses()).doesNotContain(imageBack);
        assertThat(imageBack.getImmobilier()).isNull();

        immobilier.imageses(new HashSet<>(Set.of(imageBack)));
        assertThat(immobilier.getImageses()).containsOnly(imageBack);
        assertThat(imageBack.getImmobilier()).isEqualTo(immobilier);

        immobilier.setImageses(new HashSet<>());
        assertThat(immobilier.getImageses()).doesNotContain(imageBack);
        assertThat(imageBack.getImmobilier()).isNull();
    }

    @Test
    void localiteTest() {
        Immobilier immobilier = getImmobilierRandomSampleGenerator();
        Localite localiteBack = getLocaliteRandomSampleGenerator();

        immobilier.setLocalite(localiteBack);
        assertThat(immobilier.getLocalite()).isEqualTo(localiteBack);

        immobilier.localite(null);
        assertThat(immobilier.getLocalite()).isNull();
    }

    @Test
    void quartierTest() {
        Immobilier immobilier = getImmobilierRandomSampleGenerator();
        Quartier quartierBack = getQuartierRandomSampleGenerator();

        immobilier.setQuartier(quartierBack);
        assertThat(immobilier.getQuartier()).isEqualTo(quartierBack);

        immobilier.quartier(null);
        assertThat(immobilier.getQuartier()).isNull();
    }

    @Test
    void typeImmobilierTest() {
        Immobilier immobilier = getImmobilierRandomSampleGenerator();
        TypeImmobilier typeImmobilierBack = getTypeImmobilierRandomSampleGenerator();

        immobilier.setTypeImmobilier(typeImmobilierBack);
        assertThat(immobilier.getTypeImmobilier()).isEqualTo(typeImmobilierBack);

        immobilier.typeImmobilier(null);
        assertThat(immobilier.getTypeImmobilier()).isNull();
    }

    @Test
    void annoncesTest() {
        Immobilier immobilier = getImmobilierRandomSampleGenerator();
        Annonce annonceBack = getAnnonceRandomSampleGenerator();

        immobilier.addAnnonces(annonceBack);
        assertThat(immobilier.getAnnonceses()).containsOnly(annonceBack);
        assertThat(annonceBack.getImmobilier()).isEqualTo(immobilier);

        immobilier.removeAnnonces(annonceBack);
        assertThat(immobilier.getAnnonceses()).doesNotContain(annonceBack);
        assertThat(annonceBack.getImmobilier()).isNull();

        immobilier.annonceses(new HashSet<>(Set.of(annonceBack)));
        assertThat(immobilier.getAnnonceses()).containsOnly(annonceBack);
        assertThat(annonceBack.getImmobilier()).isEqualTo(immobilier);

        immobilier.setAnnonceses(new HashSet<>());
        assertThat(immobilier.getAnnonceses()).doesNotContain(annonceBack);
        assertThat(annonceBack.getImmobilier()).isNull();
    }
}
