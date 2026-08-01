package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.DeviceTokenTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeviceTokenTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeviceToken.class);
        DeviceToken deviceToken1 = getDeviceTokenSample1();
        DeviceToken deviceToken2 = new DeviceToken();
        assertThat(deviceToken1).isNotEqualTo(deviceToken2);

        deviceToken2.setId(deviceToken1.getId());
        assertThat(deviceToken1).isEqualTo(deviceToken2);

        deviceToken2 = getDeviceTokenSample2();
        assertThat(deviceToken1).isNotEqualTo(deviceToken2);
    }
}
