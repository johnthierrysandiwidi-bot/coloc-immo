package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.FavoriAsserts.*;
import static bf.colocation.immo.domain.FavoriTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FavoriMapperTest {

    private FavoriMapper favoriMapper;

    @BeforeEach
    void setUp() {
        favoriMapper = new FavoriMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFavoriSample1();
        var actual = favoriMapper.toEntity(favoriMapper.toDto(expected));
        assertFavoriAllPropertiesEquals(expected, actual);
    }
}
