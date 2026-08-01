package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.TypeImmobilierTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TypeImmobilierTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TypeImmobilier.class);
        TypeImmobilier typeImmobilier1 = getTypeImmobilierSample1();
        TypeImmobilier typeImmobilier2 = new TypeImmobilier();
        assertThat(typeImmobilier1).isNotEqualTo(typeImmobilier2);

        typeImmobilier2.setId(typeImmobilier1.getId());
        assertThat(typeImmobilier1).isEqualTo(typeImmobilier2);

        typeImmobilier2 = getTypeImmobilierSample2();
        assertThat(typeImmobilier1).isNotEqualTo(typeImmobilier2);
    }
}
