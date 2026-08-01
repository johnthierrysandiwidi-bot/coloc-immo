package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrixDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PrixDTO.class);
        PrixDTO prixDTO1 = new PrixDTO();
        prixDTO1.setId(1L);
        PrixDTO prixDTO2 = new PrixDTO();
        assertThat(prixDTO1).isNotEqualTo(prixDTO2);
        prixDTO2.setId(prixDTO1.getId());
        assertThat(prixDTO1).isEqualTo(prixDTO2);
        prixDTO2.setId(2L);
        assertThat(prixDTO1).isNotEqualTo(prixDTO2);
        prixDTO1.setId(null);
        assertThat(prixDTO1).isNotEqualTo(prixDTO2);
    }
}
