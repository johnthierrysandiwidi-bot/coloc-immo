package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class QuartierDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(QuartierDTO.class);
        QuartierDTO quartierDTO1 = new QuartierDTO();
        quartierDTO1.setId(1L);
        QuartierDTO quartierDTO2 = new QuartierDTO();
        assertThat(quartierDTO1).isNotEqualTo(quartierDTO2);
        quartierDTO2.setId(quartierDTO1.getId());
        assertThat(quartierDTO1).isEqualTo(quartierDTO2);
        quartierDTO2.setId(2L);
        assertThat(quartierDTO1).isNotEqualTo(quartierDTO2);
        quartierDTO1.setId(null);
        assertThat(quartierDTO1).isNotEqualTo(quartierDTO2);
    }
}
