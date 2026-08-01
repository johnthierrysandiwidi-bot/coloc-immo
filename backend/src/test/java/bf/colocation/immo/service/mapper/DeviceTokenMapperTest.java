package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.DeviceTokenAsserts.*;
import static bf.colocation.immo.domain.DeviceTokenTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeviceTokenMapperTest {

    private DeviceTokenMapper deviceTokenMapper;

    @BeforeEach
    void setUp() {
        deviceTokenMapper = new DeviceTokenMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDeviceTokenSample1();
        var actual = deviceTokenMapper.toEntity(deviceTokenMapper.toDto(expected));
        assertDeviceTokenAllPropertiesEquals(expected, actual);
    }
}
