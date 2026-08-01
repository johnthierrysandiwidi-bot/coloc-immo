package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.QuartierAsserts.*;
import static bf.colocation.immo.domain.QuartierTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuartierMapperTest {

    private QuartierMapper quartierMapper;

    @BeforeEach
    void setUp() {
        quartierMapper = new QuartierMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getQuartierSample1();
        var actual = quartierMapper.toEntity(quartierMapper.toDto(expected));
        assertQuartierAllPropertiesEquals(expected, actual);
    }
}
