package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlerteNotifieeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlerteNotifieeDTO.class);
        AlerteNotifieeDTO alerteNotifieeDTO1 = new AlerteNotifieeDTO();
        alerteNotifieeDTO1.setId(1L);
        AlerteNotifieeDTO alerteNotifieeDTO2 = new AlerteNotifieeDTO();
        assertThat(alerteNotifieeDTO1).isNotEqualTo(alerteNotifieeDTO2);
        alerteNotifieeDTO2.setId(alerteNotifieeDTO1.getId());
        assertThat(alerteNotifieeDTO1).isEqualTo(alerteNotifieeDTO2);
        alerteNotifieeDTO2.setId(2L);
        assertThat(alerteNotifieeDTO1).isNotEqualTo(alerteNotifieeDTO2);
        alerteNotifieeDTO1.setId(null);
        assertThat(alerteNotifieeDTO1).isNotEqualTo(alerteNotifieeDTO2);
    }
}
