package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DetailColocationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DetailColocationDTO.class);
        DetailColocationDTO detailColocationDTO1 = new DetailColocationDTO();
        detailColocationDTO1.setId(1L);
        DetailColocationDTO detailColocationDTO2 = new DetailColocationDTO();
        assertThat(detailColocationDTO1).isNotEqualTo(detailColocationDTO2);
        detailColocationDTO2.setId(detailColocationDTO1.getId());
        assertThat(detailColocationDTO1).isEqualTo(detailColocationDTO2);
        detailColocationDTO2.setId(2L);
        assertThat(detailColocationDTO1).isNotEqualTo(detailColocationDTO2);
        detailColocationDTO1.setId(null);
        assertThat(detailColocationDTO1).isNotEqualTo(detailColocationDTO2);
    }
}
