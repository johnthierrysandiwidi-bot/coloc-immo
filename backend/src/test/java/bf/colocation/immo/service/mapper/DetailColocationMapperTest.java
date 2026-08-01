package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.DetailColocationAsserts.*;
import static bf.colocation.immo.domain.DetailColocationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DetailColocationMapperTest {

    private DetailColocationMapper detailColocationMapper;

    @BeforeEach
    void setUp() {
        detailColocationMapper = new DetailColocationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDetailColocationSample1();
        var actual = detailColocationMapper.toEntity(detailColocationMapper.toDto(expected));
        assertDetailColocationAllPropertiesEquals(expected, actual);
    }
}
