package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AnnonceDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnnonceDTO.class);
        AnnonceDTO annonceDTO1 = new AnnonceDTO();
        annonceDTO1.setId(1L);
        AnnonceDTO annonceDTO2 = new AnnonceDTO();
        assertThat(annonceDTO1).isNotEqualTo(annonceDTO2);
        annonceDTO2.setId(annonceDTO1.getId());
        assertThat(annonceDTO1).isEqualTo(annonceDTO2);
        annonceDTO2.setId(2L);
        assertThat(annonceDTO1).isNotEqualTo(annonceDTO2);
        annonceDTO1.setId(null);
        assertThat(annonceDTO1).isNotEqualTo(annonceDTO2);
    }
}
