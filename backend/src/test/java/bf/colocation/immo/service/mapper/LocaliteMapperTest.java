package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.LocaliteAsserts.*;
import static bf.colocation.immo.domain.LocaliteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocaliteMapperTest {

    private LocaliteMapper localiteMapper;

    @BeforeEach
    void setUp() {
        localiteMapper = new LocaliteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getLocaliteSample1();
        var actual = localiteMapper.toEntity(localiteMapper.toDto(expected));
        assertLocaliteAllPropertiesEquals(expected, actual);
    }
}
