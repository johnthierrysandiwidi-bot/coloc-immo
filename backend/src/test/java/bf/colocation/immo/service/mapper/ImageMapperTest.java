package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.ImageAsserts.*;
import static bf.colocation.immo.domain.ImageTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageMapperTest {

    private ImageMapper imageMapper;

    @BeforeEach
    void setUp() {
        imageMapper = new ImageMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getImageSample1();
        var actual = imageMapper.toEntity(imageMapper.toDto(expected));
        assertImageAllPropertiesEquals(expected, actual);
    }
}
