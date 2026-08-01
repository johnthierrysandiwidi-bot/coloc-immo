package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.DocumentAsserts.*;
import static bf.colocation.immo.domain.DocumentTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentMapperTest {

    private DocumentMapper documentMapper;

    @BeforeEach
    void setUp() {
        documentMapper = new DocumentMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDocumentSample1();
        var actual = documentMapper.toEntity(documentMapper.toDto(expected));
        assertDocumentAllPropertiesEquals(expected, actual);
    }
}
