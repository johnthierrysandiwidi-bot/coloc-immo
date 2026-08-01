package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.TypeDocumentAsserts.*;
import static bf.colocation.immo.domain.TypeDocumentTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeDocumentMapperTest {

    private TypeDocumentMapper typeDocumentMapper;

    @BeforeEach
    void setUp() {
        typeDocumentMapper = new TypeDocumentMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTypeDocumentSample1();
        var actual = typeDocumentMapper.toEntity(typeDocumentMapper.toDto(expected));
        assertTypeDocumentAllPropertiesEquals(expected, actual);
    }
}
