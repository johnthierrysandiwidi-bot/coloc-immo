package bf.colocation.immo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeviceTokenDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeviceTokenDTO.class);
        DeviceTokenDTO deviceTokenDTO1 = new DeviceTokenDTO();
        deviceTokenDTO1.setId(1L);
        DeviceTokenDTO deviceTokenDTO2 = new DeviceTokenDTO();
        assertThat(deviceTokenDTO1).isNotEqualTo(deviceTokenDTO2);
        deviceTokenDTO2.setId(deviceTokenDTO1.getId());
        assertThat(deviceTokenDTO1).isEqualTo(deviceTokenDTO2);
        deviceTokenDTO2.setId(2L);
        assertThat(deviceTokenDTO1).isNotEqualTo(deviceTokenDTO2);
        deviceTokenDTO1.setId(null);
        assertThat(deviceTokenDTO1).isNotEqualTo(deviceTokenDTO2);
    }
}
