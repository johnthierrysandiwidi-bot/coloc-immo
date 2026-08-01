package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.AnnonceTestSamples.*;
import static bf.colocation.immo.domain.DetailColocationTestSamples.*;
import static bf.colocation.immo.domain.EquipementTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DetailColocationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DetailColocation.class);
        DetailColocation detailColocation1 = getDetailColocationSample1();
        DetailColocation detailColocation2 = new DetailColocation();
        assertThat(detailColocation1).isNotEqualTo(detailColocation2);

        detailColocation2.setId(detailColocation1.getId());
        assertThat(detailColocation1).isEqualTo(detailColocation2);

        detailColocation2 = getDetailColocationSample2();
        assertThat(detailColocation1).isNotEqualTo(detailColocation2);
    }

    @Test
    void annonceTest() {
        DetailColocation detailColocation = getDetailColocationRandomSampleGenerator();
        Annonce annonceBack = getAnnonceRandomSampleGenerator();

        detailColocation.setAnnonce(annonceBack);
        assertThat(detailColocation.getAnnonce()).isEqualTo(annonceBack);

        detailColocation.annonce(null);
        assertThat(detailColocation.getAnnonce()).isNull();
    }

    @Test
    void equipementsTest() {
        DetailColocation detailColocation = getDetailColocationRandomSampleGenerator();
        Equipement equipementBack = getEquipementRandomSampleGenerator();

        detailColocation.addEquipements(equipementBack);
        assertThat(detailColocation.getEquipementses()).containsOnly(equipementBack);

        detailColocation.removeEquipements(equipementBack);
        assertThat(detailColocation.getEquipementses()).doesNotContain(equipementBack);

        detailColocation.equipementses(new HashSet<>(Set.of(equipementBack)));
        assertThat(detailColocation.getEquipementses()).containsOnly(equipementBack);

        detailColocation.setEquipementses(new HashSet<>());
        assertThat(detailColocation.getEquipementses()).doesNotContain(equipementBack);
    }
}
