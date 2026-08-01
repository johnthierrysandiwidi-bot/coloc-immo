package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Alerte;
import bf.colocation.immo.domain.AlerteNotifiee;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.service.dto.AlerteDTO;
import bf.colocation.immo.service.dto.AlerteNotifieeDTO;
import bf.colocation.immo.service.dto.AnnonceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AlerteNotifiee} and its DTO {@link AlerteNotifieeDTO}.
 */
@Mapper(componentModel = "spring")
public interface AlerteNotifieeMapper extends EntityMapper<AlerteNotifieeDTO, AlerteNotifiee> {
    @Mapping(target = "alerte", source = "alerte", qualifiedByName = "alerteTitre")
    @Mapping(target = "annonce", source = "annonce", qualifiedByName = "annonceTitre")
    AlerteNotifieeDTO toDto(AlerteNotifiee s);

    @Named("alerteTitre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titre", source = "titre")
    AlerteDTO toDtoAlerteTitre(Alerte alerte);

    @Named("annonceTitre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titre", source = "titre")
    AnnonceDTO toDtoAnnonceTitre(Annonce annonce);
}
