package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.domain.Quartier;
import bf.colocation.immo.service.dto.LocaliteDTO;
import bf.colocation.immo.service.dto.QuartierDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Quartier} and its DTO {@link QuartierDTO}.
 */
@Mapper(componentModel = "spring")
public interface QuartierMapper extends EntityMapper<QuartierDTO, Quartier> {
    @Mapping(target = "localite", source = "localite", qualifiedByName = "localiteNom")
    QuartierDTO toDto(Quartier s);

    @Named("localiteNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    LocaliteDTO toDtoLocaliteNom(Localite localite);
}
