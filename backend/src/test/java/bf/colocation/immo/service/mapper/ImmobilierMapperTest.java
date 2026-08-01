package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.ImmobilierAsserts.*;
import static bf.colocation.immo.domain.ImmobilierTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImmobilierMapperTest {

    private ImmobilierMapper immobilierMapper;

    @BeforeEach
    void setUp() {
        immobilierMapper = new ImmobilierMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getImmobilierSample1();
        var actual = immobilierMapper.toEntity(immobilierMapper.toDto(expected));
        assertImmobilierAllPropertiesEquals(expected, actual);
    }
}
