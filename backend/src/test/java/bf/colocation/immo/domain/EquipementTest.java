package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.DetailColocationTestSamples.*;
import static bf.colocation.immo.domain.EquipementTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EquipementTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Equipement.class);
        Equipement equipement1 = getEquipementSample1();
        Equipement equipement2 = new Equipement();
        assertThat(equipement1).isNotEqualTo(equipement2);

        equipement2.setId(equipement1.getId());
        assertThat(equipement1).isEqualTo(equipement2);

        equipement2 = getEquipementSample2();
        assertThat(equipement1).isNotEqualTo(equipement2);
    }

    @Test
    void colocationsTest() {
        Equipement equipement = getEquipementRandomSampleGenerator();
        DetailColocation detailColocationBack = getDetailColocationRandomSampleGenerator();

        equipement.addColocations(detailColocationBack);
        assertThat(equipement.getColocationses()).containsOnly(detailColocationBack);
        assertThat(detailColocationBack.getEquipementses()).containsOnly(equipement);

        equipement.removeColocations(detailColocationBack);
        assertThat(equipement.getColocationses()).doesNotContain(detailColocationBack);
        assertThat(detailColocationBack.getEquipementses()).doesNotContain(equipement);

        equipement.colocationses(new HashSet<>(Set.of(detailColocationBack)));
        assertThat(equipement.getColocationses()).containsOnly(detailColocationBack);
        assertThat(detailColocationBack.getEquipementses()).containsOnly(equipement);

        equipement.setColocationses(new HashSet<>());
        assertThat(equipement.getColocationses()).doesNotContain(detailColocationBack);
        assertThat(detailColocationBack.getEquipementses()).doesNotContain(equipement);
    }
}
