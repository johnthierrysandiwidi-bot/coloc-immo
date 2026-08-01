package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RendezVousDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RendezVousDTO.class);
        RendezVousDTO rendezVousDTO1 = new RendezVousDTO();
        rendezVousDTO1.setId(1L);
        RendezVousDTO rendezVousDTO2 = new RendezVousDTO();
        assertThat(rendezVousDTO1).isNotEqualTo(rendezVousDTO2);
        rendezVousDTO2.setId(rendezVousDTO1.getId());
        assertThat(rendezVousDTO1).isEqualTo(rendezVousDTO2);
        rendezVousDTO2.setId(2L);
        assertThat(rendezVousDTO1).isNotEqualTo(rendezVousDTO2);
        rendezVousDTO1.setId(null);
        assertThat(rendezVousDTO1).isNotEqualTo(rendezVousDTO2);
    }
}
