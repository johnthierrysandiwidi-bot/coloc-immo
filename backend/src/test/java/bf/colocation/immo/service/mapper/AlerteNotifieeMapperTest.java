package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.AlerteNotifieeAsserts.*;
import static bf.colocation.immo.domain.AlerteNotifieeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlerteNotifieeMapperTest {

    private AlerteNotifieeMapper alerteNotifieeMapper;

    @BeforeEach
    void setUp() {
        alerteNotifieeMapper = new AlerteNotifieeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAlerteNotifieeSample1();
        var actual = alerteNotifieeMapper.toEntity(alerteNotifieeMapper.toDto(expected));
        assertAlerteNotifieeAllPropertiesEquals(expected, actual);
    }
}
