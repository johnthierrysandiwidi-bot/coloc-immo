package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Image;
import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.service.dto.ImageDTO;
import bf.colocation.immo.service.dto.ImmobilierDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Image} and its DTO {@link ImageDTO}.
 */
@Mapper(componentModel = "spring")
public interface ImageMapper extends EntityMapper<ImageDTO, Image> {
    @Mapping(target = "immobilier", source = "immobilier", qualifiedByName = "immobilierNom")
    ImageDTO toDto(Image s);

    @Named("immobilierNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    ImmobilierDTO toDtoImmobilierNom(Immobilier immobilier);
}
