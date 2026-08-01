package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.AlerteAsserts.*;
import static bf.colocation.immo.domain.AlerteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlerteMapperTest {

    private AlerteMapper alerteMapper;

    @BeforeEach
    void setUp() {
        alerteMapper = new AlerteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAlerteSample1();
        var actual = alerteMapper.toEntity(alerteMapper.toDto(expected));
        assertAlerteAllPropertiesEquals(expected, actual);
    }
}
