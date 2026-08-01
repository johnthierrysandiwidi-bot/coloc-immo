package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.PrixAsserts.*;
import static bf.colocation.immo.domain.PrixTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrixMapperTest {

    private PrixMapper prixMapper;

    @BeforeEach
    void setUp() {
        prixMapper = new PrixMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPrixSample1();
        var actual = prixMapper.toEntity(prixMapper.toDto(expected));
        assertPrixAllPropertiesEquals(expected, actual);
    }
}
