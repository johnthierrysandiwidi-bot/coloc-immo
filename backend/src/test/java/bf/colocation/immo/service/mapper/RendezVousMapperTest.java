package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.RendezVousAsserts.*;
import static bf.colocation.immo.domain.RendezVousTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RendezVousMapperTest {

    private RendezVousMapper rendezVousMapper;

    @BeforeEach
    void setUp() {
        rendezVousMapper = new RendezVousMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRendezVousSample1();
        var actual = rendezVousMapper.toEntity(rendezVousMapper.toDto(expected));
        assertRendezVousAllPropertiesEquals(expected, actual);
    }
}
