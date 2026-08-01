package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VueAnnonceDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VueAnnonceDTO.class);
        VueAnnonceDTO vueAnnonceDTO1 = new VueAnnonceDTO();
        vueAnnonceDTO1.setId(1L);
        VueAnnonceDTO vueAnnonceDTO2 = new VueAnnonceDTO();
        assertThat(vueAnnonceDTO1).isNotEqualTo(vueAnnonceDTO2);
        vueAnnonceDTO2.setId(vueAnnonceDTO1.getId());
        assertThat(vueAnnonceDTO1).isEqualTo(vueAnnonceDTO2);
        vueAnnonceDTO2.setId(2L);
        assertThat(vueAnnonceDTO1).isNotEqualTo(vueAnnonceDTO2);
        vueAnnonceDTO1.setId(null);
        assertThat(vueAnnonceDTO1).isNotEqualTo(vueAnnonceDTO2);
    }
}
