package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FavoriDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FavoriDTO.class);
        FavoriDTO favoriDTO1 = new FavoriDTO();
        favoriDTO1.setId(1L);
        FavoriDTO favoriDTO2 = new FavoriDTO();
        assertThat(favoriDTO1).isNotEqualTo(favoriDTO2);
        favoriDTO2.setId(favoriDTO1.getId());
        assertThat(favoriDTO1).isEqualTo(favoriDTO2);
        favoriDTO2.setId(2L);
        assertThat(favoriDTO1).isNotEqualTo(favoriDTO2);
        favoriDTO1.setId(null);
        assertThat(favoriDTO1).isNotEqualTo(favoriDTO2);
    }
}
