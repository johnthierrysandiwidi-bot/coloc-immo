package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TypeImmobilierDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TypeImmobilierDTO.class);
        TypeImmobilierDTO typeImmobilierDTO1 = new TypeImmobilierDTO();
        typeImmobilierDTO1.setId(1L);
        TypeImmobilierDTO typeImmobilierDTO2 = new TypeImmobilierDTO();
        assertThat(typeImmobilierDTO1).isNotEqualTo(typeImmobilierDTO2);
        typeImmobilierDTO2.setId(typeImmobilierDTO1.getId());
        assertThat(typeImmobilierDTO1).isEqualTo(typeImmobilierDTO2);
        typeImmobilierDTO2.setId(2L);
        assertThat(typeImmobilierDTO1).isNotEqualTo(typeImmobilierDTO2);
        typeImmobilierDTO1.setId(null);
        assertThat(typeImmobilierDTO1).isNotEqualTo(typeImmobilierDTO2);
    }
}
