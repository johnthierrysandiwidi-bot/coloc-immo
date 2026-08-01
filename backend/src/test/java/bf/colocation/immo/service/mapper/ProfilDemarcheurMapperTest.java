package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.ProfilDemarcheurAsserts.*;
import static bf.colocation.immo.domain.ProfilDemarcheurTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfilDemarcheurMapperTest {

    private ProfilDemarcheurMapper profilDemarcheurMapper;

    @BeforeEach
    void setUp() {
        profilDemarcheurMapper = new ProfilDemarcheurMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getProfilDemarcheurSample1();
        var actual = profilDemarcheurMapper.toEntity(profilDemarcheurMapper.toDto(expected));
        assertProfilDemarcheurAllPropertiesEquals(expected, actual);
    }
}
