package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.domain.Prix;
import bf.colocation.immo.service.dto.ImmobilierDTO;
import bf.colocation.immo.service.dto.PrixDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Prix} and its DTO {@link PrixDTO}.
 */
@Mapper(componentModel = "spring")
public interface PrixMapper extends EntityMapper<PrixDTO, Prix> {
    @Mapping(target = "immobilier", source = "immobilier", qualifiedByName = "immobilierNom")
    PrixDTO toDto(Prix s);

    @Named("immobilierNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    ImmobilierDTO toDtoImmobilierNom(Immobilier immobilier);
}
