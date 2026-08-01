package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.dto.RendezVousDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link RendezVous} and its DTO {@link RendezVousDTO}.
 */
@Mapper(componentModel = "spring", uses = { AnnonceMapper.class })
public interface RendezVousMapper extends EntityMapper<RendezVousDTO, RendezVous> {
    @Mapping(target = "annonce", source = "annonce")
    @Mapping(target = "demandeur", source = "demandeur", qualifiedByName = "demandeurLogin")
    RendezVousDTO toDto(RendezVous s);


    @Named("demandeurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
