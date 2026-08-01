package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfilDemarcheurDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProfilDemarcheurDTO.class);
        ProfilDemarcheurDTO profilDemarcheurDTO1 = new ProfilDemarcheurDTO();
        profilDemarcheurDTO1.setId(1L);
        ProfilDemarcheurDTO profilDemarcheurDTO2 = new ProfilDemarcheurDTO();
        assertThat(profilDemarcheurDTO1).isNotEqualTo(profilDemarcheurDTO2);
        profilDemarcheurDTO2.setId(profilDemarcheurDTO1.getId());
        assertThat(profilDemarcheurDTO1).isEqualTo(profilDemarcheurDTO2);
        profilDemarcheurDTO2.setId(2L);
        assertThat(profilDemarcheurDTO1).isNotEqualTo(profilDemarcheurDTO2);
        profilDemarcheurDTO1.setId(null);
        assertThat(profilDemarcheurDTO1).isNotEqualTo(profilDemarcheurDTO2);
    }
}
