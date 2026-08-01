package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProfilProprietaireDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProfilProprietaireDTO.class);
        ProfilProprietaireDTO profilProprietaireDTO1 = new ProfilProprietaireDTO();
        profilProprietaireDTO1.setId(1L);
        ProfilProprietaireDTO profilProprietaireDTO2 = new ProfilProprietaireDTO();
        assertThat(profilProprietaireDTO1).isNotEqualTo(profilProprietaireDTO2);
        profilProprietaireDTO2.setId(profilProprietaireDTO1.getId());
        assertThat(profilProprietaireDTO1).isEqualTo(profilProprietaireDTO2);
        profilProprietaireDTO2.setId(2L);
        assertThat(profilProprietaireDTO1).isNotEqualTo(profilProprietaireDTO2);
        profilProprietaireDTO1.setId(null);
        assertThat(profilProprietaireDTO1).isNotEqualTo(profilProprietaireDTO2);
    }
}
