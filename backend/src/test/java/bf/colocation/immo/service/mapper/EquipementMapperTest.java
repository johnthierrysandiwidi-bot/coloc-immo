package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.EquipementAsserts.*;
import static bf.colocation.immo.domain.EquipementTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EquipementMapperTest {

    private EquipementMapper equipementMapper;

    @BeforeEach
    void setUp() {
        equipementMapper = new EquipementMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEquipementSample1();
        var actual = equipementMapper.toEntity(equipementMapper.toDto(expected));
        assertEquipementAllPropertiesEquals(expected, actual);
    }
}
