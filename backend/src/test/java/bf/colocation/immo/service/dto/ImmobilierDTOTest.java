package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ImmobilierDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ImmobilierDTO.class);
        ImmobilierDTO immobilierDTO1 = new ImmobilierDTO();
        immobilierDTO1.setId(1L);
        ImmobilierDTO immobilierDTO2 = new ImmobilierDTO();
        assertThat(immobilierDTO1).isNotEqualTo(immobilierDTO2);
        immobilierDTO2.setId(immobilierDTO1.getId());
        assertThat(immobilierDTO1).isEqualTo(immobilierDTO2);
        immobilierDTO2.setId(2L);
        assertThat(immobilierDTO1).isNotEqualTo(immobilierDTO2);
        immobilierDTO1.setId(null);
        assertThat(immobilierDTO1).isNotEqualTo(immobilierDTO2);
    }
}
