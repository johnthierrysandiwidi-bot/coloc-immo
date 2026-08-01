package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.TypeImmobilierAsserts.*;
import static bf.colocation.immo.domain.TypeImmobilierTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeImmobilierMapperTest {

    private TypeImmobilierMapper typeImmobilierMapper;

    @BeforeEach
    void setUp() {
        typeImmobilierMapper = new TypeImmobilierMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTypeImmobilierSample1();
        var actual = typeImmobilierMapper.toEntity(typeImmobilierMapper.toDto(expected));
        assertTypeImmobilierAllPropertiesEquals(expected, actual);
    }
}
