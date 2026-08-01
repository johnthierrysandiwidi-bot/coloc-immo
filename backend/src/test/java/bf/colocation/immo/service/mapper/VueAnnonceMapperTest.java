package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.VueAnnonceAsserts.*;
import static bf.colocation.immo.domain.VueAnnonceTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VueAnnonceMapperTest {

    private VueAnnonceMapper vueAnnonceMapper;

    @BeforeEach
    void setUp() {
        vueAnnonceMapper = new VueAnnonceMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVueAnnonceSample1();
        var actual = vueAnnonceMapper.toEntity(vueAnnonceMapper.toDto(expected));
        assertVueAnnonceAllPropertiesEquals(expected, actual);
    }
}
