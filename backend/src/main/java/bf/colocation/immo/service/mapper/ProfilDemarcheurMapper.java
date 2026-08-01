package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.ProfilDemarcheur;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.ProfilDemarcheurDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ProfilDemarcheur} and its DTO {@link ProfilDemarcheurDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProfilDemarcheurMapper extends EntityMapper<ProfilDemarcheurDTO, ProfilDemarcheur> {
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "userLogin")
    @Mapping(target = "validePar", source = "validePar", qualifiedByName = "userLogin")
    ProfilDemarcheurDTO toDto(ProfilDemarcheur s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
