package bf.colocation.immo.service.mapper;

import static bf.colocation.immo.domain.ProfilProprietaireAsserts.*;
import static bf.colocation.immo.domain.ProfilProprietaireTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfilProprietaireMapperTest {

    private ProfilProprietaireMapper profilProprietaireMapper;

    @BeforeEach
    void setUp() {
        profilProprietaireMapper = new ProfilProprietaireMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getProfilProprietaireSample1();
        var actual = profilProprietaireMapper.toEntity(profilProprietaireMapper.toDto(expected));
        assertProfilProprietaireAllPropertiesEquals(expected, actual);
    }
}
